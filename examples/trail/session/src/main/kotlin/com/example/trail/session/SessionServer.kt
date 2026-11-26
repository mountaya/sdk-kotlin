package com.example.trail.session

import com.mountaya.admin.admin
import com.mountaya.admin.generated.AdminSchema
import com.mountaya.client.ApiException
import com.mountaya.client.ConnectionException
import com.mountaya.client.Mountaya
import com.mountaya.client.MountayaException
import com.mountaya.client.generated.RestSchema
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.net.InetSocketAddress

/**
 * SessionServer answers `POST /session` with the `Session` [mint] answers, as the JSON object the API answered under
 * `data`, so the app's `MOUNTAYA_SESSION_URL` can point here while the secret key stays in this process. Any other path
 * is a 404 and any other method a 405; a mint that fails is a 502 whose body names no key and no token.
 */
class SessionServer(
    private val mint: suspend () -> RestSchema.Session,
) {
    /** Answer is an answer: its status, its JSON body and the methods a 405 allows. */
    class Answer(
        /** The HTTP status. */
        val status: Int,
        /** The JSON body. */
        val body: String,
        /** The `Allow` header a 405 carries, or null for none. */
        val allow: String? = null,
    )

    /** Returns the answer to [method] on [path], which waits on the API for a mint. */
    suspend fun answer(
        method: String,
        path: String,
    ): Answer =
        when {
            path != PATH -> {
                Answer(404, error("Nothing is served at $path: POST $PATH mints a session."))
            }

            method != "POST" -> {
                Answer(405, error("$PATH answers POST alone."), allow = "POST")
            }

            else -> {
                // trail: trail.session
                try {
                    Answer(200, json.encodeToString(RestSchema.Session.serializer(), mint()))
                } catch (failure: ApiException) {
                    Answer(502, error("The session could not be minted: the API answered ${failure.status}."))
                } catch (failure: ConnectionException) {
                    Answer(502, error("The session could not be minted: the API could not be reached."))
                } catch (failure: MountayaException) {
                    Answer(502, error("The session could not be minted: the API's answer could not be read."))
                }
            }
        }

    /**
     * Serves [answer] on [address], each exchange answered in a coroutine of [scope], so a mint waiting on the API
     * holds no other request up, and each answer logged by its method, path and status, never by what it carries.
     */
    fun listen(
        address: InetSocketAddress,
        scope: CoroutineScope,
    ): HttpServer {
        val server = HttpServer.create(address, 0)
        server.createContext("/") { exchange -> scope.launch { respond(exchange) } }
        server.start()
        return server
    }

    private suspend fun respond(exchange: HttpExchange) {
        try {
            val answer = answer(exchange.requestMethod, exchange.requestURI.path)
            val bytes = answer.body.encodeToByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json; charset=utf-8")
            answer.allow?.let { exchange.responseHeaders.add("Allow", it) }
            exchange.sendResponseHeaders(answer.status, bytes.size.toLong())
            exchange.responseBody.write(bytes)
            println("${exchange.requestMethod} ${exchange.requestURI.path} ${answer.status}")
        } finally {
            exchange.close()
        }
    }

    private fun error(sentence: String): String = JsonObject(mapOf("error" to JsonPrimitive(sentence))).toString()

    /** The path the server answers, and the mint it answers with. */
    companion object {
        /** The one path the server answers. */
        const val PATH = "/session"

        /** The JSON a session is written in: every member stated, a null user among them, as the API writes it. */
        private val json = Json { encodeDefaults = true }

        /**
         * Returns the mint of [mountaya], a client under the secret key: a session for [publishableKey], naming no
         * user.
         */
        fun minting(
            mountaya: Mountaya,
            publishableKey: String,
        ): suspend () -> RestSchema.Session =
            {
                mountaya.admin.sessions
                    .postSession(AdminSchema.SessionCreateParams(publishable_key = publishableKey))
                    .data
            }
    }
}
