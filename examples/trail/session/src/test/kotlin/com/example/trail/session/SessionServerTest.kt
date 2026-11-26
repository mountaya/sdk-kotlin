package com.example.trail.session

import com.mountaya.client.Credential
import com.mountaya.client.ExchangeBody
import com.mountaya.client.ExchangeRequest
import com.mountaya.client.ExchangeResponse
import com.mountaya.client.HttpExchange
import com.mountaya.client.Mountaya
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Collections
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The session server over the SDK's stub transport: an `HttpExchange` of the test's own answers every request the
 * client makes, so nothing reaches the network and no live key is needed; the server answers on the loopback address.
 */
class SessionServerTest {
    /** Stands for a publishable key, shaped as none. */
    private val publishable = "pk_trail_test"

    /** Stands for a secret key, shaped as none. */
    private val secret = "sk_trail_test"

    /** Stands for a session token, shaped as none. */
    private val token = "sess_trail_test"

    /** Every request the client sent through the stub. */
    private val sent = Collections.synchronizedList(mutableListOf<ExchangeRequest>())

    private val closing = mutableListOf<AutoCloseable>()

    @AfterTest
    fun close() {
        for (closeable in closing.asReversed()) closeable.close()
    }

    /** A server minting under a client whose every request [answer] answers, and its address. */
    private fun serve(answer: (ExchangeRequest) -> ExchangeResponse): URI {
        val stub =
            object : HttpExchange {
                override suspend fun send(request: ExchangeRequest): ExchangeResponse {
                    sent += request
                    return answer(request)
                }
            }
        val mountaya = Mountaya(Credential(secretKey = secret), exchange = stub)
        closing += mountaya
        val requests = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        closing += AutoCloseable { requests.cancel() }
        val server =
            SessionServer(SessionServer.minting(mountaya, publishable))
                .listen(InetSocketAddress(InetAddress.getLoopbackAddress(), 0), requests)
        closing += AutoCloseable { server.stop(0) }
        return URI.create("http://127.0.0.1:${server.address.port}")
    }

    private fun json(
        status: Int,
        body: String,
    ): ExchangeResponse =
        ExchangeResponse(status, mapOf("Content-Type" to "application/json"), ExchangeBody(body.encodeToByteArray()))

    private fun call(
        method: String,
        address: URI,
    ): HttpResponse<String> =
        HttpClient
            .newHttpClient()
            .send(
                HttpRequest.newBuilder(address).method(method, HttpRequest.BodyPublishers.noBody()).build(),
                HttpResponse.BodyHandlers.ofString(),
            )

    @Test
    fun aPostToTheSessionPathAnswersTheSessionMintedForThePublishableKeyNamingNoUser() {
        val address =
            serve {
                json(
                    201,
                    """{"data": {"expires_at": "2026-11-23T12:00:00.000Z", "token": "$token", "user_id": null}}""",
                )
            }

        val answer = call("POST", address.resolve("/session"))

        assertEquals(200, answer.statusCode(), answer.body())
        assertEquals("application/json; charset=utf-8", answer.headers().firstValue("Content-Type").orElse(null))
        assertEquals(
            JsonObject(
                mapOf(
                    "expires_at" to JsonPrimitive("2026-11-23T12:00:00.000Z"),
                    "user_id" to JsonNull,
                    "token" to JsonPrimitive(token),
                ),
            ),
            Json.parseToJsonElement(answer.body()),
        )
        val minted = sent.single()
        assertEquals("POST", minted.method)
        val body = assertNotNull(minted.body, "the mint sends a JSON body")
        val asked = Json.parseToJsonElement(body.decodeToString()).jsonObject
        assertEquals(JsonPrimitive(publishable), asked["publishable_key"], "minted for the publishable key")
        assertFalse("user_id" in asked, "the session is minted without user_id")
        assertEquals(secret, minted.header("X-API-Key"), "the mint is authenticated with the secret key")
    }

    @Test
    fun anyOtherPathIsA404AndAnyOtherMethodA405AndNeitherMints() {
        val address = serve { error("Nothing should be minted, and $it was sent.") }

        assertEquals(404, call("POST", address.resolve("/sessions")).statusCode())
        assertEquals(404, call("GET", address.resolve("/")).statusCode())
        val get = call("GET", address.resolve("/session"))
        assertEquals(405, get.statusCode())
        assertEquals("POST", get.headers().firstValue("Allow").orElse(null))
        assertEquals(405, call("PUT", address.resolve("/session")).statusCode())
        assertTrue(sent.isEmpty(), "$sent")
    }

    @Test
    fun aFailedMintAnswers502WithNoKeyAndNoTokenInTheBody() {
        val address =
            serve {
                json(401, """{"error": {"type": "authentication_error", "message": "$secret, $token"}}""")
            }

        val answer = call("POST", address.resolve("/session"))

        assertEquals(502, answer.statusCode(), answer.body())
        val said = Json.parseToJsonElement(answer.body()).jsonObject["error"] as JsonPrimitive
        assertEquals("The session could not be minted: the API answered 401.", said.content)
        for (secretOrToken in listOf(publishable, secret, token, "pk_", "sk_", "sess_")) {
            assertFalse(secretOrToken in answer.body(), "$secretOrToken in ${answer.body()}")
        }
    }

    @Test
    fun withoutEitherKeyTheServerRefusesToStartInOneSentenceNamingTheMissingOne() {
        val found = SessionKeys.read(mapOf(PUBLISHABLE_KEY to publishable, SECRET_KEY to secret)).getOrThrow()
        assertEquals(publishable to secret, found.publishable to found.secret)

        val refusals =
            mapOf(
                mapOf(PUBLISHABLE_KEY to publishable) to
                    "MOUNTAYA_SECRET_KEY is not set: export it in this shell, from your organization's API " +
                    "keys, then run mise run trail:session again.",
                mapOf(SECRET_KEY to secret, PUBLISHABLE_KEY to " ") to
                    "MOUNTAYA_PUBLISHABLE_KEY is not set: export it in this shell, from your organization's " +
                    "API keys, then run mise run trail:session again.",
                emptyMap<String, String>() to
                    "MOUNTAYA_PUBLISHABLE_KEY and MOUNTAYA_SECRET_KEY are not set: export them in this " +
                    "shell, from your organization's API keys, then run mise run trail:session again.",
            )
        for ((environment, sentence) in refusals) {
            val refused = SessionKeys.read(environment).exceptionOrNull()?.message
            assertEquals(sentence, refused, "$environment")
            assertFalse(publishable in refused.orEmpty() || secret in refused.orEmpty(), "a refusal names no key")
        }
        assertEquals("SessionKeys(<redacted>)", SessionKeys(publishable, secret).toString())
    }
}
