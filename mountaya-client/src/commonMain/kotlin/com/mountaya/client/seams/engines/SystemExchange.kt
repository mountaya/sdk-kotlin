package com.mountaya.client

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

/**
 * SystemExchange sends every attempt through the platform's own HTTP stack, and is the exchange a
 * client is built with where it is given none: OkHttp on the jvm and android, URLSession on iOS,
 * both through Ktor's client.
 *
 * It moves bytes and decides nothing. It holds no cookie, no cached answer and no HTTP credential,
 * so nothing the platform keeps rides a request; it follows no redirect, and a `3xx` is answered as
 * it arrived; and the platform's own timeouts are set out of the way, so the client's deadlines are
 * the only ones an attempt has. A body arrives a chunk at a time as the platform reads it, an event
 * stream's included, and a body the client closes before its end lets its call go, its connection
 * with it.
 *
 * The stack writes the headers that carry the request itself: `Host`, `Content-Length`,
 * `Accept-Encoding`, whose coding the platform undoes, and, where the client writes none, Ktor's
 * own `User-Agent`. It writes no `Accept-Language` of its own: the client's credential projection
 * names one on every attempt, `*` where the client names no language, so URLSession never sends the
 * device's languages.
 *
 * Every exchange built with the constructor that takes nothing shares one client, so every client
 * of a process shares its connections. Wrapping one in an exchange of its own is how a consumer
 * watches its traffic, since the client itself records nothing.
 */
public class SystemExchange : HttpExchange {
    /** The platform's client and the calls it has in flight. */
    internal val loader: Loader

    /** Creates the exchange, over the one client every client of the process shares. */
    public constructor() : this(Loader.shared)

    /** Creates an exchange over [loader], a test's platform client of its own. */
    internal constructor(loader: Loader) {
        this.loader = loader
    }

    /**
     * Sends one attempt and returns its answer as soon as its status and headers have arrived, its
     * body following as it is read.
     *
     * It throws the platform's failure for a request that never arrived, and the coroutine's
     * `CancellationException` once the caller is cancelled, the platform's own report of a
     * cancelled request included.
     */
    override suspend fun send(request: ExchangeRequest): ExchangeResponse = loader.send(request)

    /**
     * Loader is one platform client and the calls it has in flight.
     *
     * A call outlives the `send` that started it, since its body is read after, so it runs under
     * the client's own job rather than the caller's, and the caller's cancellation is carried to it
     * by hand: while the answer is awaited, and while a chunk of its body is.
     */
    internal class Loader(
        /** The platform's client, with this exchange's rules. */
        val client: HttpClient,
    ) {
        /** The parent of every call in flight, under the client's job, which no call's failure ends. */
        val calls: CompletableJob = SupervisorJob(client.coroutineContext.job)

        /** Where every call runs: the client's dispatcher, under [calls]. */
        private val scope = CoroutineScope(client.coroutineContext + calls)

        /** Sends [request] and returns its answer, its body still to be read. */
        suspend fun send(request: ExchangeRequest): ExchangeResponse {
            // A caller already gone sends nothing.
            currentCoroutineContext().ensureActive()
            val statement = client.prepareRequest { write(request) }
            val head = CompletableDeferred<Head>()
            val release = CompletableDeferred<Unit>()
            val call =
                scope.launch {
                    try {
                        statement.execute { response ->
                            head.complete(
                                Head(response.status.value, joined(response.headers), response.bodyAsChannel()),
                            )
                            // The call stays open until its body is let go. Ktor then cancels it
                            // where the body was not read to its end.
                            release.await()
                        }
                    } catch (failure: Throwable) {
                        head.completeExceptionally(failure)
                    }
                }
            val answered =
                try {
                    head.await()
                } catch (failure: Throwable) {
                    call.cancel()
                    throw reading(failure)
                }
            // One reader, and its one buffer, for every read of the answer's body.
            val reader =
                BodyReader(
                    HeaderLookup.value(CONTENT_LENGTH, answered.headers)?.toLongOrNull(),
                ) { bytes, offset, length ->
                    read(answered.body, bytes, offset, length, release)
                }
            return ExchangeResponse(
                status = answered.status,
                headers = answered.headers,
                body = ExchangeBody(reader) { release.complete(Unit) },
            )
        }

        /**
         * Reads what [body] holds into [bytes], from [offset] and for [length] at most, and answers how many it read,
         * or -1 at its end; a read its caller gives up lets the call go.
         */
        private suspend fun read(
            body: ByteReadChannel,
            bytes: ByteArray,
            offset: Int,
            length: Int,
            release: CompletableDeferred<Unit>,
        ): Int {
            val count =
                try {
                    body.readAvailable(bytes, offset, length)
                } catch (failure: Throwable) {
                    if (failure is CancellationException) release.complete(Unit)
                    throw reading(failure)
                }
            if (count >= 0) return count
            // A channel that broke off reads as ended, so its failure is asked for.
            body.closedCause?.let { throw reading(it) }
            return -1
        }

        companion object {
            /** The one loader every exchange built plainly shares, built on first use. */
            val shared: Loader by lazy { Loader(systemClient()) }
        }
    }

    /** The status and headers of an answer, and its body as Ktor reads it. */
    internal class Head(
        val status: Int,
        val headers: Map<String, String>,
        val body: ByteReadChannel,
    )

    /** A request's body, carried with the `Content-Type` the client wrote, exactly as written. */
    private class Bytes(
        private val bytes: ByteArray,
        contentType: String?,
    ) : OutgoingContent.ByteArrayContent() {
        override val contentLength: Long get() = bytes.size.toLong()
        override val headers: Headers = contentType?.let { headersOf(HttpHeaders.ContentType, it) } ?: Headers.Empty

        override fun bytes(): ByteArray = bytes
    }

    internal companion object {
        /**
         * Returns a new platform client with this exchange's rules: no redirect followed, none of
         * Ktor's transformations, which would add an `Accept` and an `Accept-Charset` of their own,
         * and no plugin that keeps a cookie, caches an answer, holds a credential, repeats a request
         * or times one out.
         */
        fun systemClient(): HttpClient =
            HttpClient(systemEngine()) {
                followRedirects = false
                useDefaultTransformers = false
                expectSuccess = false
            }

        /** Writes [request] as Ktor sends it: the method, the address, every header the client wrote, and the body. */
        fun HttpRequestBuilder.write(request: ExchangeRequest) {
            method = HttpMethod.parse(request.method)
            url(request.url.toString())
            var contentType: String? = null
            for ((name, value) in request.headers) {
                // Ktor carries a body's type with the body, and refuses it among the headers.
                if (HeaderLookup.asciiLowercase(name) == CONTENT_TYPE) contentType = value else header(name, value)
            }
            val body = request.body
            if (body != null || contentType != null) setBody(Bytes(body ?: ByteArray(0), contentType))
        }

        /** Every header of an answer, a header sent more than once holding its values joined by a comma and a space. */
        fun joined(headers: Headers): Map<String, String> =
            headers.names().associateWith { name -> headers.getAll(name).orEmpty().joinToString(", ") }

        /** The failure a platform failure stands for: the coroutine's cancellation for a request the platform cancelled. */
        fun reading(failure: Throwable): Throwable =
            if (failure !is CancellationException && isPlatformCancellation(failure)) {
                CancellationException("The platform cancelled the request.", failure)
            } else {
                failure
            }
    }
}

/** The `Content-Type` header's name, folded as names are compared. */
private const val CONTENT_TYPE = "content-type"

/** The header an answer states its body's length in, which sizes a body read whole. */
private const val CONTENT_LENGTH = "Content-Length"
