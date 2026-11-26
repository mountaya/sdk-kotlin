package com.mountaya.client

import com.mountaya.client.generated.OperationTable
import com.mountaya.client.generated.RestSchema
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * `transport.events.reconnect`: a printed stream read through the transport as its method reads it,
 * each open doing what the case's word for it says, on the test's virtual time and a wall clock that
 * reads it, answered with the wait before each reopen, the position each reopen sent, and whether and
 * with what the reader stopped.
 *
 * An open that stays open does so with the comment the API writes every 15 seconds, as the API does,
 * which is what keeps the fixture's 240 seconds open within the 45-second silence window (the fixture
 * states none). The case ends where the reader opens the stream once more than the case scripts, or
 * where it stops on its own. An open answered 429 or 503 is one coded as the contract codes it, with
 * the `Retry-After` the open names.
 *
 * A case naming the user the reader reads the stream as is read both ways the contract gives it one,
 * and the two must answer alike: through a session source whose snapshot names it, and through a plain
 * token and the stream's `readingAsUser`. One that states when its token expires is read once, under a
 * minted session naming the case's user, or the user every runner's session names where the case
 * names none; a case stating no user is read under a plain token, with no user told. A case naming
 * `tokenUserId` and `toldUserId` in its place is read once, through a session source whose snapshot
 * names the first, the stream told the second by `readingAsUser`.
 */
object TransportEventsReconnectRunner : ConformanceRunner {
    /** The user the minted session of a case that names none is minted for. */
    private const val STANDING_USER = "7a4e1c98-3b52-4f60-9d81-c05e2f7a83b1"

    /** What a named open's `then` may say. */
    private val ends = setOf("drops", "closes", "unauthorized", "rate-limited", "unavailable")

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val stream = input.text("stream")
        holdConstants(case, stream)
        val replies = replies(case, stream)
        val draw = input.number("random")
        val expiresAt = input.numberOrNull("tokenExpiresAtMs")
        val named = input["userId"]
        if (named != null && named !is FixtureValue.Null && named !is FixtureValue.Text) {
            throw AssertionError("a case's `userId` is a string or null")
        }
        val user = (named as? FixtureValue.Text)?.value
        val tokenUser = input.textOrNull("tokenUserId")
        val toldUser = input.textOrNull("toldUserId")
        if ((tokenUser == null) != (toldUser == null)) {
            throw AssertionError("a case names the user its token names and the one it is told together")
        }
        if (tokenUser != null && toldUser != null) {
            if (named != null || expiresAt != null) throw AssertionError("a case names its users one way")
            val read = read(scope, stream, replies, SessionToken.Source(Named(tokenUser)), toldUser, draw)
            return ConformanceOutcome.Value(read)
        }
        if (expiresAt != null) {
            if (named is FixtureValue.Null) throw AssertionError("a case that states its token's expiry names a user")
            val session =
                RestSchema.Session(
                    expires_at = Instant.fromEpochMilliseconds(expiresAt.toLong()),
                    token = "sess_abc",
                    user_id = user ?: STANDING_USER,
                )
            return ConformanceOutcome.Value(read(scope, stream, replies, SessionToken.Minted(session), null, draw))
        }
        if (user == null) {
            return ConformanceOutcome.Value(read(scope, stream, replies, SessionToken.Value("sess_abc"), null, draw))
        }
        val bySource = read(scope, stream, replies, SessionToken.Source(Named(user)), null, draw)
        val byStream = read(scope, stream, replies, SessionToken.Value("sess_abc"), user, draw)
        if (rendered(bySource) != rendered(byStream)) {
            throw AssertionError(
                "a reader its session names the user to answered ${rendered(bySource)}, and one readingAsUser names " +
                    "it to ${rendered(byStream)}",
            )
        }
        return ConformanceOutcome.Value(bySource)
    }

    /** Holds the fixture's constant and its stream's schedule to the reader's own. */
    private fun holdConstants(
        case: ConformanceCase,
        stream: String,
    ) {
        val constants = case.root("constants")
        if (constants.keys != listOf("retryAfterJitterMs")) {
            throw AssertionError("the fixture declares constants ${constants.keys} this runner does not read")
        }
        if (constants.number("retryAfterJitterMs").milliseconds != retryAfterJitter) {
            throw AssertionError("the fixture's draw above a named wait is not the reader's")
        }
        val schedule = case.root("schedules").objectOrNull(stream) ?: throw AssertionError("no schedule for `$stream`")
        val rules =
            when (stream) {
                "agent" -> EventStreamRules.agentRun
                "collaboration" -> CollaborationFeed.rules(OperationTable.Rest.getItineraryEvents.operationId)
                else -> throw AssertionError("no stream is called `$stream`")
            }
        if (schedule.number("baseMs").milliseconds != rules.baseWait ||
            schedule.number("maxMs").milliseconds != rules.maxWait ||
            schedule.numberOrNull("maxReopens")?.toInt() != rules.maxReopens ||
            schedule.numberOrNull("maxUnavailableMs")?.milliseconds != rules.maxUnavailable
        ) {
            throw AssertionError("the fixture's schedule is not the stream's")
        }
    }

    /** Each open of the case as the scripted exchange answers it. */
    private fun replies(
        case: ConformanceCase,
        stream: String,
    ): List<Scripted.Reply> {
        val table = case.root("opens").objectOrNull(stream) ?: throw AssertionError("no opens for `$stream`")
        return case.fields.items("opens").map { word ->
            val name = (word as? FixtureValue.Text)?.value
            val open =
                name?.let(table::objectOrNull) ?: throw AssertionError("no open of the $stream stream is called $word")
            val then = open.text("then")
            if (then !in ends) throw AssertionError("no open ends as `$then`")
            val retryAfter = open.textOrNull("retryAfterHeader")?.let { mapOf("Retry-After" to it) }.orEmpty()
            when (then) {
                "unauthorized" -> Scripted.Reply.Answer(401, body = Scripted.errorBody("UNAUTHORIZED"))
                "rate-limited" -> Scripted.Reply.Answer(429, retryAfter, Scripted.errorBody("TOO_MANY_REQUESTS"))
                "unavailable" -> Scripted.Reply.Answer(503, retryAfter, Scripted.errorBody("SERVICE_UNAVAILABLE"))
                else -> stays(open, then)
            }
        }
    }

    /** An open answered with its frames, which then drops or closes, the API's comment written every 15 seconds while it stays. */
    private fun stays(
        open: FixtureValue.Object,
        then: String,
    ): Scripted.Reply {
        val text = open.items("frames").joinToString("") { Streams.record(it as FixtureValue.Object) }
        val chunks = mutableListOf<Pair<Duration, ByteArray>>()
        if (text.isNotEmpty()) chunks += Duration.ZERO to text.encodeToByteArray()
        val lasts = (open.numberOrNull("forMs") ?: 0.0).milliseconds
        var beat = 15.seconds
        while (beat < lasts) {
            chunks += beat to Streams.heartbeat
            beat += 15.seconds
        }
        val end = if (then == "drops") Scripted.BodyScript.End.Drops(lasts) else Scripted.BodyScript.End.Closes(lasts)
        return Scripted.Reply.Stream(200, Scripted.BodyScript(chunks, end))
    }

    /**
     * Reads the case's stream once, under [token], told the user [told] where it is not null, and
     * returns what the case expects of it.
     */
    private suspend fun read(
        scope: TestScope,
        stream: String,
        replies: List<Scripted.Reply>,
        token: SessionToken,
        told: String?,
        draw: Double,
    ): FixtureValue {
        var reader: Job? = null
        var exhausted = false
        val exchange =
            Scripted.Exchange(scope, replies, exhausted = Scripted.Reply.Never) {
                exhausted = true
                reader?.cancel()
            }
        val transport = Streams.transport(scope, exchange, Credential.Publishable("pk_test_abc", token), draw = draw)
        val frames: Flow<*> =
            if (stream == "agent") {
                Streams.agentRun(transport)
            } else {
                Streams.itineraryEditors(transport).let { editors ->
                    if (told ==
                        null
                    ) {
                        editors
                    } else {
                        editors.readingAsUser(told)
                    }
                }
            }
        var report: String? = null
        reader =
            scope.launch {
                try {
                    frames.collect()
                } catch (error: MountayaException) {
                    report = error.name
                }
            }
        reader.join()
        val stopped = !exhausted
        val waits =
            (1 until exchange.sent.size).map { index ->
                val ended =
                    exchange.lastActivity(index - 1)
                        ?: throw AssertionError("open ${index - 1} never ended and was opened again")
                Scripted.milliseconds(exchange.sent[index].at - ended)
            }
        return Scripted.objectOf(
            "reports" to (if (stopped) Scripted.text(report) else FixtureValue.Null),
            "resumedFrom" to
                FixtureValue.Array(exchange.sent.drop(1).map { Scripted.text(it.request.header("Last-Event-ID")) }),
            "stopped" to FixtureValue.Bool(stopped),
            "waitsMs" to FixtureValue.Array(waits),
        )
    }

    /** Returns [value] written out, so two answers compare by what they hold. */
    private fun rendered(value: FixtureValue): String =
        when (value) {
            is FixtureValue.Null -> {
                "null"
            }

            is FixtureValue.Bool -> {
                value.value.toString()
            }

            is FixtureValue.Number -> {
                value.value.toString()
            }

            is FixtureValue.Text -> {
                "\"${value.value}\""
            }

            is FixtureValue.Array -> {
                value.items.joinToString(",", "[", "]") { rendered(it) }
            }

            is FixtureValue.Object -> {
                value.entries.joinToString(
                    ",",
                    "{",
                    "}",
                ) { (key, item) -> "$key:${rendered(item)}" }
            }
        }

    /** Named is a session source whose snapshot holds a token minted for [user], with no expiry it knows. */
    private class Named(
        user: String,
    ) : SessionTokenSource {
        override val snapshot: StateFlow<SessionSnapshot> =
            MutableStateFlow(SessionSnapshot(token = "sess_abc", userId = user))
    }
}
