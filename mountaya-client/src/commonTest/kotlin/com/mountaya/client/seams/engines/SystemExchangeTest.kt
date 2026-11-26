package com.mountaya.client

import com.mountaya.client.StubServer.Ending
import com.mountaya.client.StubServer.Step
import io.ktor.client.plugins.HttpPlainText
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.pluginOrNull
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * The platform's exchange, over the platform's own stack and a local server answering its
 * requests: what an attempt carries out, what its answer carries back, and when its call is let
 * go. The same tests run on the jvm, through OkHttp, and on iOS, through URLSession.
 */
class SystemExchangeTest {
    /**
     * The language is the client's to write, `*` where it names none, and the platform's stack sends it as
     * written: URLSession writes the device's languages only where an attempt names none, which none does.
     */
    @Test
    fun anAttemptLeavesAsTheClientWroteItItsLanguageIncluded() =
        exchanged(Step.Answer(200), Step.Answer(200)) { server, exchange ->
            val body = """{"name":"Tour du Mont Blanc"}""".encodeToByteArray()
            exchange
                .send(
                    ExchangeRequest(
                        "POST",
                        server.url("/v1/itineraries?limit=2"),
                        mapOf(
                            "Accept" to "application/json",
                            "Accept-Language" to "*",
                            "Content-Type" to "application/json",
                            "X-API-Key" to "pk_a",
                        ),
                        body,
                    ),
                ).body
                .close()
            exchange
                .send(
                    ExchangeRequest("GET", server.url(), mapOf("Accept-Language" to "fr-CH, fr;q=0.9")),
                ).body
                .close()

            val seen = server.seen
            assertEquals(2, seen.size)
            assertEquals("POST", seen[0].method)
            assertEquals("/v1/itineraries?limit=2", seen[0].target)
            assertEquals("application/json", seen[0].header("accept"))
            assertEquals("application/json", seen[0].header("content-type"))
            assertEquals("pk_a", seen[0].header("x-api-key"))
            assertEquals("*", seen[0].header("accept-language"))
            assertContentEquals(body, seen[0].body)
            assertEquals("GET", seen[1].method)
            assertEquals("fr-CH, fr;q=0.9", seen[1].header("accept-language"))
            assertEquals(0, seen[1].body.size)
            // None of Ktor's transformations writes a header of its own, and the agent is Ktor's.
            assertNull(seen[0].header("accept-charset"))
            assertNull(seen[1].header("accept-charset"))
            assertEquals("ktor-client", seen[0].header("user-agent"))
        }

    @Test
    fun anAddressLeavesExactlyAsTheClientEncodedIt() =
        exchanged(Step.Answer(204)) { server, exchange ->
            val target = "/v1/things/a%2Fb?q=caf%C3%A9&ids=1%2C2&tag=a%20b%26c&empty="
            exchange.send(ExchangeRequest("GET", server.url(target))).body.close()

            assertEquals(target, server.seen.single().target)
        }

    @Test
    fun anAnswerArrivesWithItsStatusAndHeadersAndItsBodyAChunkAtATime() =
        exchanged(
            Step.Answer(
                201,
                mapOf("Location" to "/v1/itineraries/it", "X-Request-Id" to "req_1"),
                listOf("{\"data\":", "{}}"),
            ),
        ) { server, exchange ->
            val response = exchange.send(ExchangeRequest("POST", server.url()))

            assertEquals(201, response.status)
            assertEquals("/v1/itineraries/it", response.header("location"))
            assertEquals("req_1", response.header("x-request-id"))
            assertEquals("""{"data":{}}""", response.body.readAll().decodeToString())
        }

    @Test
    fun noCookieIsKeptOrSentTheOneTheClientWritesAside() =
        exchanged(
            Step.Answer(200, mapOf("Set-Cookie" to "session=abc; Path=/")),
            Step.Answer(200),
            Step.Answer(200),
        ) { server, exchange ->
            exchange.send(ExchangeRequest("GET", server.url())).body.close()
            exchange.send(ExchangeRequest("GET", server.url())).body.close()
            exchange.send(ExchangeRequest("GET", server.url(), mapOf("Cookie" to "visitor=v1"))).body.close()

            val seen = server.seen
            assertEquals(3, seen.size)
            assertNull(seen[1].header("cookie"))
            assertEquals("visitor=v1", seen[2].header("cookie"))
        }

    @Test
    fun aRedirectIsAnsweredAsItArrivedAndNothingIsSentToWhereItPoints() =
        exchanged(Step.Answer(302, mapOf("Location" to "/elsewhere")), Step.Answer(200)) { server, exchange ->
            val response = exchange.send(ExchangeRequest("GET", server.url("/v1/things")))
            response.body.close()

            assertEquals(302, response.status)
            assertEquals("/elsewhere", response.header("location"))
            assertEquals(listOf("/v1/things"), server.seen.map { it.target })
        }

    @Test
    fun theClientInstallsNothingThatKeepsARequestFollowsARedirectRepeatsOneOrTimesItOut() {
        val client = SystemExchange.systemClient()
        try {
            assertNull(client.pluginOrNull(HttpRedirect))
            assertNull(client.pluginOrNull(HttpCookies))
            assertNull(client.pluginOrNull(HttpCache))
            assertNull(client.pluginOrNull(HttpRequestRetry))
            assertNull(client.pluginOrNull(HttpTimeout))
            assertNull(client.pluginOrNull(HttpPlainText))
        } finally {
            client.close()
            client.engine.close()
        }
    }

    @Test
    fun aRequestThatNeverArrivedThrowsThePlatformsFailureAndNeverACancellation() =
        exchanged(Step.Answer(200, chunks = listOf("{"), ending = Ending.BreaksOff)) { server, exchange ->
            val unreachable = StubServer.unreachable()
            val refused = assertFails { exchange.send(ExchangeRequest("GET", unreachable)) }
            assertFalse(refused is CancellationException, "$refused")

            // A body that breaks off at once may be reported before its head, so the failure is
            // read wherever it lands.
            val broken = assertFails { exchange.send(ExchangeRequest("GET", server.url())).body.readAll() }
            assertFalse(broken is CancellationException, "$broken")
        }

    @Test
    fun aTaskCancelledBeforeItSendsSendsNothingAndOneCancelledWaitingIsLetGo() =
        exchanged(Step.Silent) { server, exchange ->
            val early =
                async {
                    coroutineContext.job.cancel()
                    exchange.send(ExchangeRequest("GET", server.url()))
                }
            assertFailsWith<CancellationException> { early.await() }
            assertTrue(server.seen.isEmpty())

            val waiting = async { exchange.send(ExchangeRequest("GET", server.url())) }
            server.awaitSeen(1)
            waiting.cancel()

            assertFailsWith<CancellationException> { waiting.await() }
            server.awaitStops(1)
            exchange.awaitNoCallInFlight()
        }

    @Test
    fun aBodyReadItsCallerGivesUpThrowsCancellationAndItsStreamIsLetGo() =
        exchanged(Step.Answer(200, chunks = listOf("data: one\n\n"), ending = Ending.StaysOpen)) { server, exchange ->
            val response = exchange.send(ExchangeRequest("GET", server.url()))

            assertEquals("data: one\n\n", response.body.next()?.decodeToString())
            val reading = async(start = CoroutineStart.UNDISPATCHED) { response.body.next() }
            reading.cancel()

            assertFailsWith<CancellationException> { reading.await() }
            server.awaitStops(1)
            exchange.awaitNoCallInFlight()
        }

    @Test
    fun aStreamNobodyReadsAnyLongerLetsItsConnectionGo() =
        exchanged(Step.Answer(200, chunks = listOf("data: one\n\n"), ending = Ending.StaysOpen)) { server, exchange ->
            val response = exchange.send(ExchangeRequest("GET", server.url()))
            assertEquals("data: one\n\n", response.body.next()?.decodeToString())
            response.body.close()

            server.awaitStops(1)
            exchange.awaitNoCallInFlight()
        }

    @Test
    fun everyExchangeBuiltPlainlySharesTheOneClientOfTheProcess() =
        exchanged { _, exchange ->
            assertSame(SystemExchange().loader, SystemExchange().loader)
            assertNotSame(exchange.loader, SystemExchange().loader)
        }

    /**
     * Runs [block] against a server answering [steps] and an exchange over a platform client of its
     * own, so a test never shares a connection with another, and lets both go after.
     */
    private fun exchanged(
        vararg steps: Step,
        block: suspend TestScope.(StubServer, SystemExchange) -> Unit,
    ): TestResult =
        runTest {
            val server = StubServer.start(*steps)
            val loader = SystemExchange.Loader(SystemExchange.systemClient())
            try {
                block(server, SystemExchange(loader))
            } finally {
                loader.client.close()
                loader.client.engine.close()
                server.close()
            }
        }

    /** Waits, in real time, until the exchange has no call in flight. */
    private suspend fun SystemExchange.awaitNoCallInFlight() {
        StubServer.realTime {
            while (loader.calls.children.any()) delay(10)
        }
    }

    /** Returns what [block] throws, failing where it throws nothing. */
    private suspend fun assertFails(block: suspend () -> Unit): Throwable {
        try {
            block()
        } catch (failure: Throwable) {
            return failure
        }
        throw AssertionError("It threw nothing.")
    }
}
