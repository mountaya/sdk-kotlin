package com.mountaya.client

import com.mountaya.client.StubServer.Step
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import okhttp3.Authenticator
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.asResponseBody
import okio.Buffer
import okio.Source
import okio.Timeout
import okio.buffer
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/** The jvm's and android's engine: OkHttp, configured to keep nothing, follow nothing and time nothing out. */
class SystemEngineOkHttpTest {
    @Test
    fun theClientKeepsNothingForARequestAndItsOwnTimeoutsStandOutOfTheWay() {
        val client = systemOkHttpClient()

        assertSame(CookieJar.NO_COOKIES, client.cookieJar)
        assertNull(client.cache)
        assertSame(Authenticator.NONE, client.authenticator)
        assertSame(Authenticator.NONE, client.proxyAuthenticator)
        assertFalse(client.followRedirects)
        assertFalse(client.followSslRedirects)
        assertEquals(0, client.connectTimeoutMillis)
        assertEquals(0, client.readTimeoutMillis)
        assertEquals(0, client.writeTimeoutMillis)
        assertEquals(0, client.callTimeoutMillis)
    }

    @Test
    fun asManyCallsRunAtOnceToOneHostAsToAllOnThreadsThatKeepNoProcessAlive() {
        val dispatcher = systemOkHttpClient().dispatcher
        val daemon = CompletableFuture<Boolean>()
        dispatcher.executorService.execute { daemon.complete(Thread.currentThread().isDaemon) }

        assertEquals(dispatcher.maxRequests, dispatcher.maxRequestsPerHost)
        assertTrue(daemon.get(10, TimeUnit.SECONDS))
        dispatcher.executorService.shutdown()
    }

    @Test
    fun aCallOkHttpReportsCancelledIsTheCoroutinesCancellationAtTheAnswerAndInTheBody() =
        runTest {
            val server = StubServer.start(Step.Answer(200), Step.Answer(200, chunks = listOf("{\"data\": {}}")))
            val cancelsAtOnce =
                exchange {
                    addInterceptor { chain ->
                        chain.call().cancel()
                        chain.proceed(chain.request())
                    }
                }
            val cancelsInTheBody =
                exchange {
                    addNetworkInterceptor { chain ->
                        val response = chain.proceed(chain.request())
                        response.newBuilder().body(CancelledAfterOne().buffer().asResponseBody(null, -1)).build()
                    }
                }
            try {
                assertFailsWith<CancellationException> { cancelsAtOnce.send(ExchangeRequest("GET", server.url())) }
                assertFailsWith<CancellationException> {
                    cancelsInTheBody.send(ExchangeRequest("GET", server.url())).body.readAll()
                }
            } finally {
                for (exchange in listOf(cancelsAtOnce, cancelsInTheBody)) {
                    exchange.loader.client.close()
                    exchange.loader.client.engine
                        .close()
                }
                server.close()
            }
        }

    /**
     * OkHttp sends printable ASCII alone in a header, so a tag in Latin-1, which the contract lets a header
     * carry, fails as the attempt leaves: the client refuses it itself, before any request reaches the server,
     * and never repeats it as a request that never arrived.
     */
    @Test
    fun aHeaderOkHttpCannotSendIsRefusedBeforeAnyRequestReachesTheServerAndNeverRepeated() =
        runTest {
            val server = StubServer.start(Step.Answer(200))
            val exchange = exchange {}
            try {
                val tag = mapOf("If-Match" to "\"café\"")
                assertFailsWith<IllegalArgumentException>("OkHttp itself refuses the tag") {
                    exchange.send(ExchangeRequest("PATCH", server.url(), tag))
                }
                val transport =
                    Transport(
                        Transport.Configuration(
                            Credential.Publishable("pk_test_abc"),
                            null,
                            BaseUrls(rest = server.url()),
                            RetryPolicy.standard,
                            30.seconds,
                            null,
                        ),
                        exchange,
                        Scripted.clock(this),
                        Scripted.Random(0.0),
                        StandardTestDispatcher(testScheduler),
                    )
                val facts =
                    OperationFacts(OperationDeclaration("writeThing", "PATCH", isConditional = true), "/v1/things")
                val call =
                    OperationCall(
                        facts,
                        facts.path,
                        emptyList(),
                        emptyList(),
                        RequestBody.Empty,
                        null,
                        "\"café\"",
                        RequestOptions(),
                    )

                assertFailsWith<ConfigurationException> {
                    transport.send(call, JsonElement.serializer(), JsonObject.serializer().nullable)
                }
                assertEquals(0, server.seen.size)
            } finally {
                exchange.loader.client.close()
                exchange.loader.client.engine
                    .close()
                server.close()
            }
        }

    @Test
    fun onlyOkHttpsOwnCancellationIsReadAsOne() {
        val cancelled = IOException("Canceled")

        assertTrue(isPlatformCancellation(cancelled))
        assertTrue(isPlatformCancellation(IOException("unexpected end of stream", cancelled)))
        assertTrue(SystemExchange.reading(cancelled) is CancellationException)
        assertFalse(isPlatformCancellation(IOException("Socket closed")))
        assertFalse(isPlatformCancellation(SocketTimeoutException("timeout")))
        assertFalse(SystemExchange.reading(SocketTimeoutException("timeout")) is CancellationException)
    }

    /** An exchange over the system's OkHttp client with [configure] written over it. */
    private fun exchange(configure: OkHttpClient.Builder.() -> Unit): SystemExchange {
        val okHttp = systemOkHttpClient().newBuilder().apply(configure).build()
        val client =
            HttpClient(OkHttp.create { preconfigured = okHttp }) {
                followRedirects = false
                useDefaultTransformers = false
                expectSuccess = false
            }
        return SystemExchange(SystemExchange.Loader(client))
    }

    /** A body that answers one byte, then fails as OkHttp fails a call it cancelled. */
    private class CancelledAfterOne : Source {
        private var answered = false

        override fun read(
            sink: Buffer,
            byteCount: Long,
        ): Long {
            if (answered) throw IOException("Canceled")
            answered = true
            sink.writeUtf8("{")
            return 1
        }

        override fun timeout(): Timeout = Timeout.NONE

        override fun close() = Unit
    }
}
