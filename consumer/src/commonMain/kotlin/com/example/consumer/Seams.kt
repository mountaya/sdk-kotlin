package com.example.consumer

import com.mountaya.client.ExchangeBody
import com.mountaya.client.ExchangeRequest
import com.mountaya.client.ExchangeResponse
import com.mountaya.client.HttpExchange
import com.mountaya.client.RandomSource
import com.mountaya.client.SessionSnapshot
import com.mountaya.client.SessionTokenSource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * Recorded wraps another exchange and reads every attempt and answer, which is how a consumer
 * watches its traffic: the client itself records nothing.
 */
class Recorded(
    private val wrapping: HttpExchange,
) : HttpExchange {
    override suspend fun send(request: ExchangeRequest): ExchangeResponse {
        seen(request.method, request.url, request.headers, request.body, request.header("accept"), "$request")
        val response = wrapping.send(request)
        seen(response.status, response.headers, response.header("x-request-id"), "$response")
        return response
    }
}

/** Scripted answers every attempt from a script, a body read in chunks, as a consumer's own test does. */
class Scripted : HttpExchange {
    override suspend fun send(request: ExchangeRequest): ExchangeResponse {
        val chunks = Channel<ByteArray>(Channel.UNLIMITED)
        chunks.trySend("""{"data": null}""".encodeToByteArray())
        chunks.close()
        seen(ExchangeBody(ByteArray(0)), ExchangeBody(onClose = {}) { null }, ExchangeRequest("GET", request.url))
        return ExchangeResponse(200, mapOf("X-Request-Id" to "req_a"), ExchangeBody(chunks))
    }
}

/** Draws is a random source a test pins. */
object Draws : RandomSource {
    override fun unitDraw(): Double = 0.5

    override fun cryptographicUuid(): Uuid = Uuid.parse("6f1c2a9e-3b4d-4e5f-8a6b-7c8d9e0f1a2b")
}

/** Signals is a session source an app with a session machine of its own implements. */
class Signals : SessionTokenSource {
    private val held = MutableStateFlow(SessionSnapshot(isMinting = true))

    override val snapshot: StateFlow<SessionSnapshot> = held.asStateFlow()

    /** Hands the source a token its own machine minted. */
    fun minted(token: String) {
        held.value = SessionSnapshot(token = token, expiresAt = Instant.DISTANT_FUTURE, userId = "us_a")
    }
}
