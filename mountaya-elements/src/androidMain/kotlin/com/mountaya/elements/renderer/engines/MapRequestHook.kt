package com.mountaya.elements

import com.mountaya.client.Mountaya
import com.mountaya.client.Url
import okhttp3.Call
import okhttp3.Dispatcher
import okhttp3.Headers
import okhttp3.Headers.Companion.toHeaders
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.maplibre.android.module.http.HttpRequestUtil
import java.io.IOException
import java.net.ConnectException
import java.util.Collections
import java.util.WeakHashMap

/**
 * MapRequestHook is MapLibre's one request hook, held for the process: the `OkHttpClient` MapLibre fetches
 * with, for a style, a tile, a sprite or a glyph, built around two interceptors of the hook's own and
 * installed in MapLibre's one slot for it, `HttpRequestUtil.setOkHttpClient`. Every live map holds it at
 * once, each through its [Hold], from its renderer's start to its close.
 *
 * [answers], the application interceptor, the first of the client's, hears each request once, whatever
 * redirect it follows: it hands it to the app's own hook, the `transformRequest` of the map attached last
 * that set one, which may rewrite its address and its headers, then reads the credential its address
 * takes, and hears its final answer. [headers], the network interceptor, the first of the client's, gives
 * each request as it goes out on the network, each hop of a redirect included, the headers of the most
 * recently attached map whose client's Tile API claims the hop's address (`Mountaya.mapRequestHeaders`):
 * its credential, read for that request, so a refreshed session token reaches the next tile, its language
 * and the SDK that made it, `X-Mountaya-Client`, each header the request does not already carry, so a
 * header the app's hook sets stands. A hop to any other address, a host a style names or a redirect away
 * from the Tile API, is given nothing: OkHttp copies a redirect's headers from the request it was handed,
 * never from a hop on the network, so a credential never follows a redirect off its origin. Two maps whose
 * clients reach the same Tile API with different credentials send the credential of the map attached last
 * (SPEC.md section 11).
 *
 * A request the hook cannot send never leaves the device: its session token refused by the client at that
 * read, a header the client answers that OkHttp cannot carry, or the app's hook answering an address or a
 * header no request carries, or throwing, fails with a `ConnectException` before any connection is made,
 * which MapLibre reports as its resource failed and repeats on its own schedule, each repeat read again. Where the
 * client's read of the credential threw, the failure is told carrying what it threw, the hop of a redirect included.
 *
 * An answer of 400 up, carrying its status, and a request that reached no answer, the connection failing
 * or the request never leaving, carrying none, is told at the address MapLibre asked for, never one the app's
 * hook or a redirect led to, since it is that address which lies under a source's tile templates or names the
 * style asked for (`map.events.access-refused`). It is told to every live map that claims the address
 * ([Hold.claims]): to the one live map where none claims it, and to nobody where several are live. A request
 * MapLibre cancelled, a tile no longer wanted, is no failure and is told to nobody.
 *
 * A refusal of the credential, a 401 or a 403 (`isCredentialRefusal`), is told with whether its request left
 * under a credential the hook no longer gives. [headers] keeps, for each call, the credential its last hop went
 * out on the network with, and a call no hop of which reached the network is read by the one its request
 * entered the hook with. Where the credential read now for the address that last hop went to, under the same
 * choice of map, the one attached last whose client's Tile API claims it, is another, or that read fails, the
 * request left under one the client no longer sends: a session token since replaced, a token minted after a
 * tile left under the key alone, a visitor's cookie since changed. The engine tells such a refusal to nobody
 * (`map.events.access-refused`), and the requests MapLibre sent before the change are answered over many
 * frames, so each is read as it arrives.
 *
 * The client it installs is built on the app's own `OkHttpClient` where the map attached last is given
 * one, the hook's interceptors ahead of the app's, and on MapLibre's defaults otherwise. MapLibre calls it
 * on threads of its own, so what it reads is held in volatile fields, swapped whole.
 */
internal object MapRequestHook {
    /**
     * Hold is one live map's hold on the hook: the client whose Tile API its requests take a credential
     * from, the app's own hook, the app's own client the hook is built on, and where an answer it claims is
     * told, on a thread of OkHttp's, with whether its request left under a credential the hook no longer
     * gives its address, which a refusal of the credential alone is read for, and what the client's read of
     * the credential threw, for a request that never left for want of one.
     */
    class Hold(
        val client: Mountaya,
        val transform: ((MapRequest) -> MapRequest)?,
        val base: OkHttpClient?,
        val failures: ((url: String, status: Int?, superseded: Boolean, cause: Throwable?) -> Unit)?,
    ) {
        /**
         * The beginnings of every address the map claims (`claimedAddresses`), which its renderer writes as
         * it draws and OkHttp's threads read, swapped whole.
         */
        @Volatile
        var claimed: List<String> = emptyList()

        /** Whether the map claims [url]: it starts with one of the beginnings the map claims. */
        fun claims(url: String): Boolean = claimed.any(url::startsWith)
    }

    /** The credential the first hop of a request takes, read as the request entered the hook, by its address. */
    private class Signing(
        val url: String,
        val headers: Headers,
    )

    /** The credential a hop went out on the network with: the headers its address took, or null where it took none. */
    private class Sent(
        val headers: Headers?,
    )

    /**
     * The failure of a request whose credential the client could not read, its read throwing [read], which the
     * request's failure is told carrying: it never leaves, as every request the hook cannot send.
     */
    private class UnreadableCredential(
        val read: Throwable,
    ) : ConnectException("The map's client refused this request's credential; it was not sent.") {
        init {
            initCause(read)
        }
    }

    /** The client MapLibre fetches with, and the app's own it was built on, or null for MapLibre's defaults. */
    private class Built(
        val base: OkHttpClient?,
        val client: OkHttpClient,
    )

    /** Guards the changes of [holds] and [built], made on the main thread and read on OkHttp's. */
    private val lock = Any()

    /** Every live map's hold, in the order each attached: the last is the map attached most recently. */
    @Volatile
    private var holds: List<Hold> = emptyList()

    @Volatile
    private var built: Built? = null

    /**
     * The credential the last hop of each call under way went out on the network with, by its call: written by
     * [headers] as each hop goes out and taken by [answers] as the call's answer arrives, both interceptors
     * seeing the one call. A call none of whose hops reached the network is never written, and a call MapLibre
     * let go of holds nothing, its key being weak.
     */
    private val sent: MutableMap<Call, Sent> = Collections.synchronizedMap(WeakHashMap())

    /** MapLibre's own defaults, which its loader builds where no app gives it a client: twenty requests a host at a time. */
    private val defaults: OkHttpClient by lazy {
        OkHttpClient.Builder().dispatcher(Dispatcher().apply { maxRequestsPerHost = MAX_REQUESTS_PER_HOST }).build()
    }

    /** The application interceptor: the app's hook and the first hop's credential, and the final answer, heard once. */
    val answers: Interceptor = Interceptor { chain -> answer(chain) }

    /**
     * The network interceptor: each hop as it goes out, given the credential its own address takes, which its
     * call keeps for the answer.
     */
    val headers: Interceptor =
        Interceptor { chain ->
            val hop = chain.request()
            val credential = credentialOf(hop)
            sent[chain.call()] = Sent(credential)
            chain.proceed(signed(hop, credential))
        }

    /** The client MapLibre fetches with, or null before the first map. */
    val client: OkHttpClient? get() = built?.client

    /**
     * Makes the hook MapLibre's for [hold], a map's: the map holds it beside every other live map, attached
     * last, and MapLibre fetches with the hook's client over the map's own [Hold.base]. It is called on the
     * main thread, by the renderer as it starts.
     */
    fun install(hold: Hold) {
        attach(hold)
        HttpRequestUtil.setOkHttpClient(client(hold.base))
    }

    /**
     * Lets [hold] go, as its renderer closes: its map is told nothing more, its client's credential is
     * given to no request where no other live map's client reaches its Tile API, and MapLibre fetches over
     * the own client of the map attached last of those still live. It is called on the main thread.
     */
    fun release(hold: Hold) {
        val latest = detach(hold) ?: return
        HttpRequestUtil.setOkHttpClient(client(latest.base))
    }

    /**
     * Adds [hold] to the live maps, attached last; one already live moves to the end. It is the half of
     * [install] that names no MapLibre type, which a test on the host JVM, where MapLibre cannot load, sends
     * through [client].
     */
    fun attach(hold: Hold) {
        synchronized(lock) { holds = holds.filterNot { it === hold } + hold }
    }

    /**
     * Takes [hold] off the live maps, and answers the map attached last of those still live where [hold]
     * was the last attached, whose own client MapLibre then fetches over, or null.
     */
    fun detach(hold: Hold): Hold? =
        synchronized(lock) {
            val wasLatest = holds.lastOrNull() === hold
            holds = holds.filterNot { it === hold }
            if (wasLatest) holds.lastOrNull() else null
        }

    /** The hook's client over [base], the app's own client, or MapLibre's defaults where it is null: built once per base in turn. */
    fun client(base: OkHttpClient?): OkHttpClient =
        synchronized(lock) {
            val held = built
            if (held != null && held.base === base) return@synchronized held.client
            (base ?: defaults)
                .newBuilder()
                .apply {
                    interceptors().add(0, answers)
                    networkInterceptors().add(0, headers)
                }.build()
                .also { built = Built(base, it) }
        }

    /**
     * Hears one request: hands it to the app's hook, reads the credential of its first hop, and tells a failed
     * answer at the address MapLibre asked for, a refusal of the credential with whether its request left under
     * one the hook no longer gives the address its last hop went to.
     */
    private fun answer(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val call = chain.call()
        // The address the map asked for, which every answer is told at.
        val asked = request.url.toString()
        val sending =
            try {
                prepared(request)
            } catch (refused: ConnectException) {
                if (!call.isCanceled()) tell(asked, null, false, (refused as? UnreadableCredential)?.read)
                throw refused
            }
        val response =
            try {
                chain.proceed(sending)
            } catch (failure: IOException) {
                sent.remove(call)
                // A redirect's hop whose credential the client could not read fails in the network interceptor, and
                // its failure arrives here as the call's.
                if (!call.isCanceled()) tell(asked, null, false, (failure as? UnreadableCredential)?.read)
                throw failure
            }
        // The credential the call's last hop went out on the network with, or, where none reached it, the one the
        // request entered the hook with.
        val left = sent.remove(call) ?: Sent(sending.tag(Signing::class.java)?.headers)
        val status = response.code
        if (status >= FIRST_FAILING_STATUS) {
            val hop = response.request.url.toString()
            tell(asked, status, superseded = isCredentialRefusal(status) && isSuperseded(hop, left.headers))
        }
        return response
    }

    /**
     * Returns [request] as it enters the network: rewritten by the app's own hook, the one of the map attached
     * last that set one, and carrying, as a tag no hop sends, the credential its address takes, read once,
     * which [signed] gives the first hop.
     *
     * @throws ConnectException where the request cannot be sent: its session token refused by the client, a
     * header the client answers that OkHttp cannot carry, or the app's hook answering an address or a header
     * no request carries, or throwing.
     */
    fun prepared(request: Request): Request {
        val live = holds
        val transform = live.lastOrNull { it.transform != null }?.transform
        val rewritten = transform?.let { rewritten(request, it) } ?: request
        val url = rewritten.url.toString()
        val credential = credential(url, live) ?: return rewritten
        return rewritten
            .newBuilder()
            .tag(Signing::class.java, Signing(url, credential))
            .build()
    }

    /**
     * The credential [hop], a request as it goes out on the network, takes: the one read as the request entered
     * the hook where the hop is its first, and the one its address takes now for a redirect's, or null where its
     * address takes none.
     *
     * @throws ConnectException where a redirect's hop cannot be sent, its session token refused by the client
     * or a header the client answers one OkHttp cannot carry.
     */
    private fun credentialOf(hop: Request): Headers? {
        val url = hop.url.toString()
        val first = hop.tag(Signing::class.java)?.takeIf { it.url == url }
        return first?.headers ?: credential(url, holds)
    }

    /** Returns [hop] given [credential], each header the hop does not already carry, or [hop] itself where it is null. */
    private fun signed(
        hop: Request,
        credential: Headers?,
    ): Request {
        if (credential == null) return hop
        val sending = hop.newBuilder()
        for ((name, value) in credential) {
            if (hop.header(name) == null) sending.header(name, value)
        }
        return sending.build()
    }

    /**
     * Whether a request to [url] whose last hop went out with [left], the credential its address took then, or
     * none, left under one the hook no longer gives [url]: the credential a read for [url] finds now, under the same
     * choice of map, the one attached last whose client's Tile API claims it, is another, or that read fails, its
     * session token refused by the client or a header the client answers one OkHttp cannot carry.
     */
    private fun isSuperseded(
        url: String,
        left: Headers?,
    ): Boolean {
        val now =
            try {
                credential(url, holds)
            } catch (refused: ConnectException) {
                return true
            }
        return now != left
    }

    /**
     * Tells the failed answer to [url], carrying [status] where an answer carried one, whether its request left
     * under a credential the hook no longer gives the address, [superseded], and what the client's read of its
     * credential threw, [cause], where it never left for want of one, to every live map that claims the address: to
     * the one live map where none claims it, and to nobody where several are live.
     */
    private fun tell(
        url: String,
        status: Int?,
        superseded: Boolean,
        cause: Throwable? = null,
    ) {
        val live = holds
        val claimants = live.filter { it.claims(url) }
        val told = if (claimants.isEmpty() && live.size == 1) live else claimants
        for (hold in told) hold.failures?.invoke(url, status, superseded, cause)
    }

    /**
     * The credential of the map attached last whose client's Tile API claims [url], as that client reads it for
     * this request, or null where none claims it.
     *
     * @throws ConnectException where that client refuses its session token at this read, an [UnreadableCredential]
     * carrying what the read threw, or answers a header OkHttp cannot carry.
     */
    private fun credential(
        url: String,
        live: List<Hold>,
    ): Headers? {
        val address = Url(url)
        for (hold in live.asReversed()) {
            val headers =
                try {
                    hold.client.mapRequestHeaders(address)
                } catch (refusal: Exception) {
                    throw UnreadableCredential(refusal)
                } ?: continue
            return try {
                headers.toHeaders()
            } catch (refusal: IllegalArgumentException) {
                throw refused("The map's client answered a header OkHttp cannot carry", refusal)
            }
        }
        return null
    }

    /** [request] as [transform] answers it: its URL, and each header it states set over the request's own. */
    private fun rewritten(
        request: Request,
        transform: (MapRequest) -> MapRequest,
    ): Request {
        val given =
            MapRequest(
                Url(request.url.toString()),
                request.headers.names().associateWith {
                    request.headers.values(it).joinToString(", ")
                },
            )
        val answered =
            try {
                transform(given)
            } catch (failure: Exception) {
                throw refused("The map's transformRequest threw", failure)
            }
        val url =
            answered.url.toString().toHttpUrlOrNull()
                ?: throw refused("The map's transformRequest answered a URL no request is sent to", null)
        return try {
            request
                .newBuilder()
                .url(url)
                .apply { for ((name, value) in answered.headers) header(name, value) }
                .build()
        } catch (failure: IllegalArgumentException) {
            throw refused("The map's transformRequest answered a header no request carries", failure)
        }
    }

    /** The failure of a request that never leaves: no connection is made, and MapLibre repeats it on its own schedule. */
    private fun refused(
        reason: String,
        cause: Throwable?,
    ): ConnectException = ConnectException("$reason; it was not sent.").apply { if (cause != null) initCause(cause) }

    /** The requests MapLibre's own client sends to one host at a time. */
    private const val MAX_REQUESTS_PER_HOST = 20

    /** The least status of an answer that failed: a refusal or a fault of the client's request, or of the server. */
    private const val FIRST_FAILING_STATUS = 400
}
