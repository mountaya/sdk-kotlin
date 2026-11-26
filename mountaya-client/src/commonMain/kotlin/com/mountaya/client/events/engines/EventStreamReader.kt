package com.mountaya.client

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * EventStreamReader reads one operation's event stream through the transport, by the rules of
 * `sdk/SPEC.md` section 12: when it is over, when it is opened again, after how long, and from
 * which record.
 *
 * Its [frames] are cold: collecting them opens the stream, a second collection opens a second one,
 * and a collector that is cancelled or stops early closes it. A chunk is read only once the
 * collector has taken the frame before it, so the time a collector holds a frame is its own: the
 * silence window runs only while the reader waits on the network. An exception the collector
 * throws reaches it back through `emit`, and the reader, held to Flow's exception transparency,
 * never catches it.
 *
 * It reads the stream by the rules of the stream its operation opens, an editor stream's apart from
 * a list stream's though both carry one vocabulary, and as the user its session token names, where
 * the client is told one, or else as [member], the user its caller says it reads the stream as: an
 * editor stream's `presence.join` of that user is no progress. A reader told no user reads every
 * `presence.join` as progress.
 */
internal class EventStreamReader<Event>(
    private val transport: Transport,
    private val call: Transport.Call,
    private val reading: StreamedEvents<Event>,
    /** The user the caller says it reads the stream as, or null where it says none. */
    private val member: String? = null,
    /**
     * Told how each open ended: the failure it failed with, or null for a close and for a
     * `reconnect`, whether the reader reopens after it or stops. Nothing it does changes the stream.
     */
    private val observer: ((MountayaException?) -> Unit)? = null,
) {
    private val rules: EventStreamRules get() = reading.rules

    /** The cold stream of frames, each collection one reading of the stream from its first open. */
    fun frames(): Flow<EventFrame<Event>> = flow { Reading(this).run() }

    /** Opened is how one open ended. */
    private sealed interface Opened {
        class Answered(
            val response: ExchangeResponse,
        ) : Opened

        class Refused(
            val answer: Transport.Answer,
        ) : Opened

        class Dropped(
            val cause: Throwable,
        ) : Opened
    }

    /** Decoded is a frame of a chunk and, for a record, the event and the part its stream's reading gave it, or null. */
    private class Decoded<Event>(
        val framed: EventFramer.Framed,
        val read: Pair<Event, EventRole>?,
    ) {
        operator fun component1(): EventFramer.Framed = framed

        operator fun component2(): Pair<Event, EventRole>? = read
    }

    /** Ended is how one open's reading ended. */
    private sealed interface Ended {
        /** The run's `done`: nothing more is read. */
        object Done : Ended

        /** A `reconnect`: the stream is opened again at once. */
        object Reconnect : Ended

        /** A close, where [failure] is null, or a drop carrying its failure. */
        class Closed(
            val failure: MountayaException?,
        ) : Ended
    }

    /**
     * Reading is one collection's state: the position it resumes from, the count of its opens since
     * the last that made progress, and the wait the last open left.
     */
    private inner class Reading(
        private val collector: FlowCollector<EventFrame<Event>>,
    ) {
        private var position: String? = null
        private var count = ReconnectSchedule.Count()
        private var nextWait: Duration? = null
        private var opens = 0

        suspend fun run() {
            while (true) {
                nextWait?.let {
                    nextWait = null
                    waitBeforeRetry(it)
                }
                currentCoroutineContext().ensureActive()
                val attempt = opens
                opens += 1
                val opened = open()
                val response =
                    when (opened) {
                        is Deadline.Raced.Expired -> {
                            if (!failed(ConnectionException())) return
                            continue
                        }

                        is Deadline.Raced.Finished -> {
                            when (val value = opened.value) {
                                is Opened.Dropped -> {
                                    if (!failed(ConnectionException(cause = value.cause))) return
                                    continue
                                }

                                is Opened.Refused -> {
                                    transport.report(value.answer, call, attempt)
                                    val error =
                                        withContext(transport.dispatcher) { transport.failure(value.answer, call) }
                                    observer?.invoke(error)
                                    if (OpenFailure.ends(error, rules)) throw error
                                    if (!failed(error, observed = true)) return
                                    continue
                                }

                                is Opened.Answered -> {
                                    value.response
                                }
                            }
                        }
                    }
                transport.report(Transport.Answer(response.status, response.headers, ByteArray(0)), call, attempt)
                val ended = response.body.use { body -> read(body, expiry(), user() ?: member) }
                when (ended) {
                    is Ended.Done -> {
                        return
                    }

                    is Ended.Reconnect -> {
                        reopenAtOnce()
                    }

                    is Ended.Closed -> {
                        if (!closed(ended.failure)) return
                    }
                }
            }
        }

        /**
         * Opens the stream within one attempt's budget, resuming from the position. A credential
         * refused locally ends the stream, since opening again repeats it: it is thrown at once.
         *
         * The answer is let go wherever the open does not hand it on: the budget run out as it arrived,
         * the collector gone as it arrived, which ends the race cancelled with the answer in hand, and a
         * refusal's body read, so the platform's call and its connection end with it rather than stay open
         * until the server ends them, each one an open stream the API counts.
         */
        private suspend fun open(): Deadline.Raced<Opened> {
            var answered: ExchangeResponse? = null
            var handedOn = false
            try {
                val raced =
                    try {
                        Deadline.run(call.budget) {
                            val request = transport.request(call, position)
                            try {
                                val response = transport.exchange.send(request)
                                answered = response
                                if (response.status !in 200..299) {
                                    val bytes = withContext(transport.dispatcher) { response.body.readAll() }
                                    Opened.Refused(Transport.Answer(response.status, response.headers, bytes))
                                } else {
                                    Opened.Answered(response)
                                }
                            } catch (cancelled: CancellationException) {
                                throw cancelled
                            } catch (failure: Exception) {
                                currentCoroutineContext().ensureActive()
                                Opened.Dropped(failure)
                            }
                        }
                    } catch (refusal: MountayaException) {
                        observer?.invoke(refusal)
                        throw refusal
                    }
                handedOn = raced is Deadline.Raced.Finished && raced.value is Opened.Answered
                return raced
            } finally {
                if (!handedOn) answered?.body?.close()
            }
        }

        /**
         * Reads one open's body to its end, handing on each frame, the silence window around every
         * read. [ownMember] is the user the open reads the stream as, or null where it can name none.
         */
        private suspend fun read(
            body: ExchangeBody,
            tokenExpiry: Instant?,
            ownMember: String?,
        ): Ended {
            val framer = EventFramer()
            var frames = 0
            val leadingSnapshots = mutableSetOf<String?>()
            var pastLeadingSnapshots = false
            while (true) {
                val chunk =
                    try {
                        Deadline.run(EventStreamRules.stallWindow) { body.next() }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (failure: Exception) {
                        currentCoroutineContext().ensureActive()
                        return closedOrDropped(ConnectionException(cause = failure), frames, tokenExpiry)
                    }
                val bytes =
                    when (chunk) {
                        is Deadline.Raced.Expired -> return closedOrDropped(ConnectionException(), frames, tokenExpiry)
                        is Deadline.Raced.Finished -> chunk.value ?: return closedOrDropped(null, frames, tokenExpiry)
                    }
                // The chunk is framed and its records' payloads decoded on the client's dispatcher, in one hop, so a
                // stream collected on the main thread parses nothing there; each frame is then handed on from the
                // collector's own coroutine, as a flow's emissions must be.
                val decoded =
                    withContext(transport.dispatcher) {
                        framer.feed(bytes).map { framed ->
                            Decoded(framed, (framed as? EventFramer.Framed.Record)?.let { reading.read(it.record) })
                        }
                    }
                for ((framed, read) in decoded) {
                    when (framed) {
                        is EventFramer.Framed.Position -> {
                            position = framed.id
                        }

                        is EventFramer.Framed.Record -> {
                            val record = framed.record
                            if (read == null) {
                                frames += 1
                                continue
                            }
                            val (event, role) = read
                            if (role.isReconnect) {
                                observer?.invoke(null)
                                return Ended.Reconnect
                            }
                            // The reader's own member's arrival, sent as its first stream opens, is no
                            // progress wherever it comes, and is handed on all the same.
                            if (!isOwnArrival(event, ownMember)) {
                                if (role.isOpeningSnapshot && !pastLeadingSnapshots &&
                                    record.event !in leadingSnapshots
                                ) {
                                    leadingSnapshots += record.event
                                } else {
                                    pastLeadingSnapshots = true
                                    frames += 1
                                }
                            }
                            if (role.movesPosition) record.id?.let { position = it }
                            collector.emit(EventFrame(record.id, event))
                            if (role.endsStream) return Ended.Done
                        }
                    }
                }
            }
        }

        /**
         * Reads the end of an open stream: a close at or after its token's expiry, or one that made
         * progress, is followed at once; anything else is an open without progress.
         */
        private fun closedOrDropped(
            failure: MountayaException?,
            frames: Int,
            tokenExpiry: Instant?,
        ): Ended {
            if (failure == null && tokenExpiry != null && transport.clock.now() >= tokenExpiry) {
                // The last record before a close can be lost on its way, so a close at or after the
                // token's expiry is read as the `reconnect` the API writes when a credential ends.
                observer?.invoke(null)
                return Ended.Reconnect
            }
            if (frames > rules.framesBeforeProgress) {
                observer?.invoke(failure)
                return Ended.Reconnect
            }
            return Ended.Closed(failure)
        }

        /** Reads a close or a drop without progress, and returns whether the reader goes on. */
        private fun closed(failure: MountayaException?): Boolean {
            observer?.invoke(failure)
            return failed(failure, observed = true)
        }

        /**
         * Follows an open that made progress, or that ended with its credential, at once, and resets
         * the count of opens without progress and the time the opens answered 503 are held to.
         */
        private fun reopenAtOnce() {
            count = ReconnectSchedule.Count()
            nextWait = null
        }

        /**
         * Reads an open without progress: it waits on the schedule, or the wait its answer names,
         * before the next, or stops where the schedule says, throwing the failure the open failed with,
         * or ending where it closed. The time an agent run's opens answered 503 are held to is read on
         * the client's wall clock. Returns whether the reader goes on.
         */
        private fun failed(
            failure: MountayaException?,
            observed: Boolean = false,
        ): Boolean {
            if (!observed) observer?.invoke(failure)
            val draw = transport.random.unitDraw()
            val next = ReconnectSchedule.next(failure, count, transport.clock.now(), rules, draw)
            count = next.count
            val wait = next.wait
            if (wait == null) {
                if (failure != null) throw failure
                return false
            }
            nextWait = wait
            return true
        }

        /**
         * Whether [event] is the `presence.join` of [ownMember], the user the open reads the stream as,
         * on a stream whose rules read it as no progress. Users are compared exactly.
         */
        private fun isOwnArrival(
            event: Event,
            ownMember: String?,
        ): Boolean {
            if (rules.ownArrivalIsProgress || ownMember == null) return false
            return reading.arrivingMember(event) == ownMember
        }

        /** The user the session token an open is made with names, where the client is told one: a minted session's, or a source's. */
        private fun user(): String? {
            val credential = transport.configuration.credential as? Credential.Publishable ?: return null
            return when (val token = credential.sessionToken) {
                is SessionToken.Minted -> token.session.user_id
                is SessionToken.Source -> token.source.snapshot.value.userId
                else -> null
            }
        }

        /** When the session token an open is made with expires, where the client is told. */
        private fun expiry(): Instant? {
            val credential = transport.configuration.credential as? Credential.Publishable ?: return null
            return when (val token = credential.sessionToken) {
                is SessionToken.Minted -> token.session.expires_at
                is SessionToken.Source -> token.source.snapshot.value.expiresAt
                else -> null
            }
        }
    }
}
