package com.mountaya.client

import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * Scripted is what the public runners put behind a client in place of the network and the
 * platform: an exchange that answers from a script on the test's virtual time, a wall clock that
 * reads that time, and draws that are the fixture's own. Everything here is written against the
 * published seams, so a consumer's test can do the same. It is the conformance module's own,
 * held here for the runners and the tests that need the transport's internals beside it.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal object Scripted {
    /** Reply is how the exchange answers one attempt. */
    sealed interface Reply {
        /** An answer that arrives [after] the attempt was sent, its body whole. */
        class Answer(
            val status: Int,
            val headers: Map<String, String> = emptyMap(),
            val body: ByteArray = ByteArray(0),
            val after: Duration = Duration.ZERO,
        ) : Reply

        /** A request that never arrived, the connection dropping [after] it was sent. */
        class Drop(
            val after: Duration = Duration.ZERO,
        ) : Reply

        /** An attempt nothing ever answers. */
        object Never : Reply

        /** An answer whose body is read as [body] says, from the moment it arrives [after] the attempt. */
        class Stream(
            val status: Int,
            val body: BodyScript,
            val headers: Map<String, String> = emptyMap(),
            val after: Duration = Duration.ZERO,
        ) : Reply
    }

    /** BodyScript is a body that arrives a chunk at a time, each at its offset from the answer, and then ends as [end] says. */
    class BodyScript(
        val chunks: List<Pair<Duration, ByteArray>>,
        val end: End,
    ) {
        /** End is how a body ends once its chunks are read. */
        sealed interface End {
            /** A body the far side closes at [at], from the answer. */
            class Closes(
                val at: Duration,
            ) : End

            /** A body that breaks off at [at], from the answer. */
            class Drops(
                val at: Duration,
            ) : End

            /** A body that delivers nothing more and never ends. */
            object StaysOpen : End
        }
    }

    /** Dropped is the failure of a connection the script drops. */
    class Dropped : Exception("the scripted connection dropped")

    /** Exchange answers each attempt with the next reply of its script, and records what each attempt sent and when. */
    class Exchange(
        private val scope: TestScope,
        replies: List<Reply>,
        private val exhausted: Reply = Reply.Never,
        private val onExhausted: (() -> Unit)? = null,
    ) : HttpExchange {
        /** Sent is one attempt as it left the client, [at] its offset from the test's start. */
        class Sent(
            val request: ExchangeRequest,
            val at: Duration,
        )

        private val replies = ArrayDeque(replies)
        val sent = mutableListOf<Sent>()

        /** When each attempt last delivered anything or ended, by attempt. */
        private val activity = mutableMapOf<Int, Duration>()

        /** When attempt [attempt] last delivered anything or ended, from the test's start. */
        fun lastActivity(attempt: Int): Duration? = activity[attempt]

        private fun now(): Duration = scope.testScheduler.currentTime.milliseconds

        private fun record(attempt: Int) {
            activity[attempt] = now()
        }

        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
            val attempt = sent.size
            sent += Sent(request, now())
            val reply =
                replies.removeFirstOrNull() ?: run {
                    onExhausted?.invoke()
                    exhausted
                }
            return when (reply) {
                is Reply.Answer -> {
                    delay(reply.after)
                    record(attempt)
                    ExchangeResponse(reply.status, reply.headers, ExchangeBody(reply.body))
                }

                is Reply.Drop -> {
                    delay(reply.after)
                    record(attempt)
                    throw Dropped()
                }

                is Reply.Never -> {
                    try {
                        awaitCancellation()
                    } finally {
                        record(attempt)
                    }
                }

                is Reply.Stream -> {
                    delay(reply.after)
                    record(attempt)
                    ExchangeResponse(reply.status, reply.headers, body(reply.body, attempt))
                }
            }
        }

        private fun body(
            script: BodyScript,
            attempt: Int,
        ): ExchangeBody {
            val opened = now()
            val chunks = ArrayDeque(script.chunks)
            var isOver = false
            return ExchangeBody {
                if (isOver) return@ExchangeBody null
                val chunk = chunks.removeFirstOrNull()
                if (chunk != null) {
                    try {
                        delay(opened + chunk.first - now())
                    } finally {
                        record(attempt)
                    }
                    return@ExchangeBody chunk.second
                }
                when (val end = script.end) {
                    is BodyScript.End.Closes -> {
                        delay(opened + end.at - now())
                        isOver = true
                        record(attempt)
                        null
                    }

                    is BodyScript.End.Drops -> {
                        delay(opened + end.at - now())
                        isOver = true
                        record(attempt)
                        throw Dropped()
                    }

                    is BodyScript.End.StaysOpen -> {
                        try {
                            awaitCancellation()
                        } finally {
                            record(attempt)
                        }
                    }
                }
            }
        }
    }

    /**
     * Random draws the fixture's own number before every wait, and keys from a source that may be
     * missing. A key is counted by [countedKey], never read off the platform's random source, so a
     * run draws the same keys every time.
     */
    class Random(
        private val draw: Double = 0.0,
        private val keys: Boolean = true,
    ) : RandomSource {
        private var drawn = 0L

        override fun unitDraw(): Double = draw

        override fun cryptographicUuid(): Uuid? = if (keys) countedKey(++drawn) else null
    }

    /**
     * The [n]th key a scripted source draws, from 1: a version 4 UUID in its lowercase canonical form,
     * which `drawnKeyPattern` matches, holding [n] before its version digit and in its last group, so
     * no two draws of one source are alike. The first is `00000000-0001-4000-8000-000000000001`.
     */
    fun countedKey(n: Long): Uuid = Uuid.fromLongs((n shl 16) or 0x4000L, Long.MIN_VALUE or n)

    /** The wall clock of a test: the epoch, plus the virtual time the test has run. */
    fun clock(scope: TestScope): Clock =
        object : Clock {
            override fun now(): Instant = Instant.fromEpochMilliseconds(scope.testScheduler.currentTime)
        }

    /** The virtual time the test has run. */
    fun elapsed(scope: TestScope): Duration = scope.testScheduler.currentTime.milliseconds

    /** A publishable key alone. */
    val publishable: Credential = Credential.Publishable("pk_test_abc")

    /** A publishable key and a session token, which every stream's open and every Data API call takes. */
    val session: Credential =
        Credential.Publishable(
            "pk_test_abc",
            com.mountaya.client.SessionToken
                .Value("sess_abc"),
        )

    /** A policy that repeats nothing, so a runner reads the error the first answer raises. */
    val noRetries: RetryPolicy = RetryPolicy(maxRetries = 0, baseDelay = Duration.ZERO, maxDelay = Duration.ZERO)

    /** The fixtures' policy: two attempts after the first, and no wait of its own. */
    val twoQuickRetries: RetryPolicy = RetryPolicy(maxRetries = 2, baseDelay = Duration.ZERO, maxDelay = Duration.ZERO)

    /**
     * A client sending through [exchange] on [scope]'s time, reading [clock] as its wall clock, drawing
     * what [random] draws, and telling [reports] of every attempt. It reads and decodes each answer on a dispatcher of [scope]'s own
     * scheduler, so the read and the decode run on the test's virtual time, as every wait does.
     */
    fun client(
        scope: TestScope,
        exchange: HttpExchange,
        credential: Credential = publishable,
        retry: RetryPolicy = RetryPolicy.standard,
        timeout: Duration = 30.seconds,
        random: RandomSource = Random(),
        reports: MutableList<ResponseReport>? = null,
        clock: Clock = clock(scope),
    ): Mountaya =
        Mountaya(
            credential = credential,
            retry = retry,
            timeout = timeout,
            onResponse = reports?.let { list -> { report: ResponseReport -> list += report } },
            exchange = exchange,
            clock = clock,
            random = random,
            dispatcher = StandardTestDispatcher(scope.testScheduler),
        )

    /** A failure envelope with one entry coded [code], or none where it is null. */
    fun errorBody(
        code: String?,
        message: String = "scripted",
    ): ByteArray =
        if (code == null) {
            """{"errors":[]}""".encodeToByteArray()
        } else {
            """{"errors":[{"extensions":{"code":"$code"},"message":"$message"}]}""".encodeToByteArray()
        }

    /** An envelope whose `data` is null. */
    val emptySuccess: ByteArray = """{"data":null}""".encodeToByteArray()

    /** A call's failure named as the contract names it, or `aborted` for the caller's own cancellation. */
    fun className(error: Throwable): String =
        when (error) {
            is CancellationException -> "aborted"
            is MountayaException -> error.name
            else -> error::class.simpleName ?: "Throwable"
        }

    /** A duration as a fixture's number of milliseconds. */
    fun milliseconds(duration: Duration): FixtureValue =
        FixtureValue.Number(duration.toDouble(DurationUnit.MILLISECONDS))

    /** A whole number as a fixture's number. */
    fun number(value: Number): FixtureValue = FixtureValue.Number(value.toDouble())

    /** A string, or null, as a fixture's value. */
    fun text(value: String?): FixtureValue = value?.let { FixtureValue.Text(it) } ?: FixtureValue.Null

    /** An object of the members given, in order. */
    fun objectOf(vararg members: Pair<String, FixtureValue>): FixtureValue = FixtureValue.Object(members.toList())

    /** The headers a case writes under [key], each a string. */
    fun headers(
        input: FixtureValue.Object,
        key: String = "headers",
    ): Map<String, String> =
        input.objectOrNull(key)?.entries.orEmpty().associate { (name, value) ->
            name to ((value as? FixtureValue.Text)?.value ?: throw AssertionError("the header `$name` is no string"))
        }

    /** Whether the case's [key] is true, false where it is absent. */
    fun flag(
        input: FixtureValue.Object,
        key: String,
    ): Boolean = (input[key] as? FixtureValue.Bool)?.value ?: false

    /** The origin a scripted client sends to, under which a runner writes a path. */
    val origin: Url = Url("https://api.mountaya.com")
}
