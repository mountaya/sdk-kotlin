package com.mountaya.client

import com.mountaya.client.generated.OperationTable
import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class EventStreamReaderTest {
    private val delta = """{"text":"The"}"""
    private val message = """{"content":"The second day is the long one.","message_index":0}"""

    private fun record(
        event: String,
        data: String,
        id: String? = null,
    ): String = "event: $event\n" + (id?.let { "id: $it\n" } ?: "") + "data: $data\n\n"

    private fun stream(
        text: String,
        at: Duration = Duration.ZERO,
        closes: Boolean = true,
    ): Scripted.Reply =
        Scripted.Reply.Stream(
            200,
            Scripted.BodyScript(
                listOf(at to text.encodeToByteArray()),
                if (closes) Scripted.BodyScript.End.Closes(at) else Scripted.BodyScript.End.StaysOpen,
            ),
        )

    private val notFound =
        Scripted.Reply.Answer(
            404,
            body = """{"errors":[{"message":"gone","extensions":{"code":"NOT_FOUND"}}]}""".encodeToByteArray(),
        )
    private val unauthorized = Scripted.Reply.Answer(401)

    /** Reads an agent run's stream off [replies], answering the ids of the frames handed on and the failure it ended with. */
    private suspend fun TestScope.read(
        replies: List<Scripted.Reply>,
        draw: Double = 0.0,
        credential: Credential = Scripted.session,
    ): Triple<List<String?>, MountayaException?, Scripted.Exchange> {
        val exchange = Scripted.Exchange(this, replies)
        val transport = Streams.transport(this, exchange, credential, draw = draw)
        val ids = mutableListOf<String?>()
        val failure =
            try {
                Streams.frames("agent", transport).collect { ids += (it as EventFrame<*>).id }
                null
            } catch (error: MountayaException) {
                error
            }
        return Triple(ids, failure, exchange)
    }

    @Test
    fun anOpenAsksForTheStreamWithTheCredentialAndAReopenResumesFromTheLastId() =
        runTest {
            val (ids, failure, exchange) =
                read(
                    listOf(
                        stream(record("message-delta", delta) + record("message", message, "1-0")),
                        notFound,
                    ),
                )

            assertEquals(listOf(null, "1-0"), ids)
            assertEquals("NotFoundError", failure?.name)
            assertEquals(
                "text/event-stream",
                exchange.sent
                    .first()
                    .request
                    .header("Accept"),
            )
            assertEquals(
                "sess_abc",
                exchange.sent
                    .first()
                    .request
                    .header("X-Session-Token"),
            )
            assertNull(
                exchange.sent
                    .first()
                    .request
                    .header("Last-Event-ID"),
            )
            assertEquals(
                "1-0",
                exchange.sent
                    .last()
                    .request
                    .header("Last-Event-ID"),
            )
        }

    @Test
    fun nothingAfterDoneIsReadAndAnUnknownEventIsPassedOverWithoutMovingThePosition() =
        runTest {
            val text =
                record("brand-new", "{}", "0-9") + record("done", """{"reason":"completed"}""", "1-0") +
                    record("message", message, "2-0")
            val (ids, failure, exchange) = read(listOf(stream(text, closes = false)))

            assertEquals(listOf("1-0"), ids)
            assertNull(failure)
            assertEquals(1, exchange.sent.size)
        }

    @Test
    fun opensWithoutProgressWaitOnTheScheduleAndTheReaderStopsPastSixWithTheLastFailure() =
        runTest {
            val (ids, failure, exchange) = read(List(7) { Scripted.Reply.Drop() }, draw = 1.0)
            val waits = exchange.sent.map { it.at }.zipWithNext { earlier, later -> later - earlier }

            assertEquals(emptyList(), ids)
            assertEquals("ConnectionError", failure?.name)
            assertEquals(listOf(500.milliseconds, 1.seconds, 2.seconds, 4.seconds, 8.seconds, 8.seconds), waits)
        }

    /** An open answered [status] coded [code], naming [retryAfter] where one is given. */
    private fun refused(
        status: Int,
        code: String,
        retryAfter: String? = null,
    ): Scripted.Reply =
        Scripted.Reply.Answer(
            status,
            retryAfter?.let { mapOf("Retry-After" to it) }.orEmpty(),
            """{"errors":[{"message":"later","extensions":{"code":"$code"}}]}""".encodeToByteArray(),
        )

    /** An agent run's open answered 503, naming a wait of 30 seconds. */
    private val unavailable: Scripted.Reply get() = refused(503, "SERVICE_UNAVAILABLE", "30")

    private val readerUser = "7a4e1c98-3b52-4f60-9d81-c05e2f7a83b1"
    private val peerUser = "2b8d4f19-6a05-4e73-8c1f-9d3b7e05a24c"

    /** What an editor stream's open begins with, the roster and lock snapshots, then the arrival of [member], and the stream then closes. */
    private fun arrival(member: String): Scripted.Reply =
        stream(
            record("presence.snapshot", """{"members":[]}""") + record("lock.snapshot", """{"locks":[]}""") +
                record("presence.join", """{"user_id":"$member","connected_at":"2026-05-12T09:14:07Z"}"""),
        )

    /** A session source whose one token names [user]. */
    private class Naming(
        user: String,
    ) : SessionTokenSource {
        override val snapshot: StateFlow<SessionSnapshot> =
            MutableStateFlow(SessionSnapshot(token = "sess_abc", userId = user))
    }

    private val named: Credential = Credential.Publishable("pk_test_abc", SessionToken.Source(Naming(readerUser)))

    /**
     * Reads a collaboration stream of [operation] as a printed method does, read as [user] where one is
     * given, until it ends, each wait drawn at its ceiling, and returns when each open was sent and what the
     * stream ended with.
     */
    private suspend fun TestScope.feed(
        replies: List<Scripted.Reply>,
        operation: OperationFacts = OperationTable.Rest.getItineraryEvents,
        credential: Credential = Scripted.session,
        user: String? = null,
    ): Pair<List<Duration>, MountayaException?> {
        val exchange = Scripted.Exchange(this, replies)
        val transport = Streams.transport(this, exchange, credential, draw = 1.0)
        val call =
            OperationCall(
                operation,
                "/v1/collab/streams",
                emptyList(),
                emptyList(),
                RequestBody.Empty,
                options = RequestOptions(),
            )
        val events = transport.stream(call, RestSchema.CollabStreamEvent.serializer())
        val failure =
            try {
                (if (user == null) events else events.readingAsUser(user)).collect()
                null
            } catch (error: MountayaException) {
                error
            }
        return exchange.sent.map { it.at } to failure
    }

    @Test
    fun anOpenAnswered429Or503NamingAWaitReopensAfterItWithTheDrawZeroSecondsToo() =
        runTest {
            val (_, failure, exchange) =
                read(
                    listOf(
                        refused(429, "TOO_MANY_REQUESTS", "2"),
                        refused(503, "SERVICE_UNAVAILABLE", "0"),
                        notFound,
                    ),
                    draw = 0.5,
                )

            assertEquals(listOf(Duration.ZERO, 2500.milliseconds, 3.seconds), exchange.sent.map { it.at })
            assertEquals("NotFoundError", failure?.name)
        }

    @Test
    fun anAgentRunsOpensAnswered503AreNotAmongTheSixAndItsCeilingStillDoubles() =
        runTest {
            val (_, failure, exchange) = read(List(8) { refused(503, "SERVICE_UNAVAILABLE") } + notFound, draw = 1.0)
            val waits = exchange.sent.map { it.at }.zipWithNext { earlier, later -> later - earlier }

            assertEquals(
                listOf(500.milliseconds, 1.seconds, 2.seconds, 4.seconds, 8.seconds, 8.seconds, 8.seconds, 8.seconds),
                waits,
            )
            assertEquals("NotFoundError", failure?.name)
        }

    @Test
    fun anAgentRunsOpensAnswered503AreHeldToTwoMinutesPastWhichItReportsThe503() =
        runTest {
            val (_, failure, exchange) = read(List(5) { unavailable } + notFound, draw = 1.0)

            assertEquals(listOf(0, 31, 62, 93, 124).map { it.seconds }, exchange.sent.map { it.at })
            assertEquals("ServerError", failure?.name)
        }

    @Test
    fun progressAndAReconnectEachStartAnAgentRunsTwoMinutesAgain() =
        runTest {
            val progress = record("message-delta", delta) + record("message", message)
            val reconnect = record("reconnect", """{"reason":"credential-expired"}""")
            for (between in listOf(progress, reconnect)) {
                val (_, failure, exchange) =
                    read(List(3) { unavailable } + stream(between) + List(4) { unavailable } + notFound, draw = 1.0)

                val opened = exchange.sent.first().at
                assertEquals(
                    listOf(0, 31, 62, 93, 93, 124, 155, 186, 217).map { it.seconds },
                    exchange.sent.map {
                        it.at -
                            opened
                    },
                )
                assertEquals("NotFoundError", failure?.name)
            }
        }

    @Test
    fun onAnEditorStreamTheReadersOwnJoinIsNoProgressItsUserNamedOrTold() =
        runTest {
            val replies = listOf(arrival(readerUser), arrival(readerUser), notFound)
            val byToken = feed(replies, credential = named)
            val told = feed(replies, user = readerUser)
            val tokenFirst = feed(replies, credential = named, user = peerUser)

            for ((sent, failure) in listOf(byToken, told, tokenFirst)) {
                val opened = sent.first()
                assertEquals(listOf(Duration.ZERO, 500.milliseconds, 1500.milliseconds), sent.map { it - opened })
                assertEquals("NotFoundError", failure?.name)
            }
        }

    @Test
    fun anotherMembersJoinIsProgressAndSoIsEveryJoinAReaderThatNamesNoUserReads() =
        runTest {
            val peer = feed(listOf(arrival(peerUser), notFound), credential = named)
            val unnamed = feed(listOf(arrival(readerUser), notFound))
            val list = feed(listOf(arrival(readerUser), notFound), OperationTable.Rest.getOrganizationEvents, named)

            for ((sent, failure) in listOf(peer, unnamed, list)) {
                val opened = sent.first()
                assertEquals(listOf(Duration.ZERO, Duration.ZERO), sent.map { it - opened })
                assertEquals("NotFoundError", failure?.name)
            }
        }

    @Test
    fun aStreamThatDeliversNothingFor45SecondsIsGivenUpAndOpenedAgain() =
        runTest {
            val beat =
                Scripted.Reply.Stream(
                    200,
                    Scripted.BodyScript(
                        listOf(10.seconds to ": beat\n\n".encodeToByteArray()),
                        Scripted.BodyScript.End.StaysOpen,
                    ),
                )
            val (_, _, exchange) = read(listOf(beat, unauthorized))

            assertEquals(listOf(Duration.ZERO, 55.seconds), exchange.sent.map { it.at })
        }

    @Test
    fun aReconnectIsFollowedAtOnceWithThePositionAndIsHandedOnToNobody() =
        runTest {
            val text = record("message", message, "1-0") + record("reconnect", """{"reason":"credential-expired"}""")
            val (ids, _, exchange) = read(listOf(stream(text, closes = false), unauthorized), draw = 1.0)

            assertEquals(listOf("1-0"), ids)
            assertEquals(listOf(Duration.ZERO, Duration.ZERO), exchange.sent.map { it.at })
            assertEquals(
                "1-0",
                exchange.sent
                    .last()
                    .request
                    .header("Last-Event-ID"),
            )
        }

    @Test
    fun aCredentialRefusedLocallyEndsTheStreamAndNothingIsOpened() =
        runTest {
            val (_, failure, exchange) = read(emptyList(), credential = Credential.Publishable("pk_test_abc"))

            assertEquals("ConfigurationError", failure?.name)
            assertEquals(0, exchange.sent.size)
            // A second collection opens afresh, and is refused alike: the stream is cold.
            val again = Scripted.Exchange(this, emptyList())
            assertFailsWith<ConfigurationException> {
                Streams.frames("agent", Streams.transport(this, again, Credential.Publishable("pk_test_abc"))).collect()
            }
        }

    @Test
    fun aCollectorThatStopsEarlyClosesTheStreamAndAFunctionTokenIsReadAtEveryOpen() =
        runTest {
            var reads = 0
            val credential =
                Credential.Publishable(
                    "pk_test_abc",
                    SessionToken.Function {
                        reads += 1
                        "sess_read"
                    },
                )
            val exchange =
                Scripted.Exchange(
                    this,
                    listOf(
                        stream(record("message", message, "1-0") + record("message", message, "2-0"), closes = false),
                    ),
                )
            val frames = Streams.frames("agent", Streams.transport(this, exchange, credential)).take(1).toList()

            assertEquals(1, frames.size)
            assertEquals(1, exchange.sent.size)
            assertEquals(1, reads)
        }

    /**
     * The collector goes as the answer arrives: the exchange answered, and the open's race ends cancelled with
     * the answer in hand. The answer is let go, so the platform's call ends and its connection with it, rather
     * than held open until the server ends it, an open stream the API counts against the user.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun anAnswerThatArrivesAsTheCollectorGoesIsLetGoAndSoIsARefusalsBody() =
        runTest {
            val closes = mutableListOf<Int>()
            lateinit var collecting: Job
            for (status in listOf(200, 404)) {
                val answering =
                    object : HttpExchange {
                        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
                            // Gone between the answer and the end of the race, which suspends no more.
                            if (status == 200) collecting.cancel()
                            return ExchangeResponse(
                                status,
                                mapOf("Content-Type" to "text/event-stream"),
                                ExchangeBody(onClose = { closes += status }) { null },
                            )
                        }
                    }
                val transport = Streams.transport(this, answering, Scripted.session, draw = 0.0)
                // A refusal ends the stream with its error, which is not what is read here.
                collecting = launch { runCatching { Streams.frames("agent", transport).collect() } }
                runCurrent()
                collecting.cancel()
                advanceUntilIdle()
            }

            assertEquals(200, closes.first(), "the answer the race dropped is let go")
            assertTrue(404 in closes, "a refusal's body is let go once read")
        }
}
