package com.mountaya.elements

import com.mountaya.client.BaseUrls
import com.mountaya.client.Credential
import com.mountaya.client.ExchangeRequest
import com.mountaya.client.ExchangeResponse
import com.mountaya.client.HttpExchange
import com.mountaya.client.Mountaya
import com.mountaya.client.SessionToken
import com.mountaya.client.Url
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.net.ConnectException
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * The request hook on the host JVM, MapLibre left out: the hook's own client sends each request to a server on
 * the loopback address, so the network interceptor that gives each hop its credential runs as it runs on a
 * device. Each test holds the hook through maps of its own and lets them go; the hook is the process's, and
 * the tests run one at a time.
 */
class MapRequestHookTest {
    /** The Tile API the tests' first clients reach, answering a redirect, a refusal or a tile by its path. */
    private val tiles =
        LoopbackServer { path ->
            when {
                path.startsWith("/v1/moved/") -> {
                    LoopbackServer.Answer(302, mapOf("Location" to elsewhere.url("/moved.png", host = "localhost")))
                }

                path.startsWith("/v1/renamed/") -> {
                    LoopbackServer.Answer(301, mapOf("Location" to "/v1/zones/1/2/3"))
                }

                path.startsWith("/v1/refused/") -> {
                    LoopbackServer.Answer(402)
                }

                else -> {
                    LoopbackServer.Answer(204)
                }
            }
        }

    /** A second Tile API, another map's client's. */
    private val otherTiles = LoopbackServer { LoopbackServer.Answer(204) }

    /** A host a style names, which is not Mountaya's, reached by its name rather than its number. */
    private val elsewhere = LoopbackServer { LoopbackServer.Answer(200) }

    /** Every hold a test attached, let go after it. */
    private val attached = mutableListOf<MapRequestHook.Hold>()

    /**
     * A failure the hook told a map: its address, its status, whether its request left under a credential the hook no
     * longer gives the address, and what the client's read of its credential threw, where it never left for want of one.
     */
    private data class Told(
        val url: String,
        val status: Int?,
        val superseded: Boolean = false,
        val cause: Throwable? = null,
    )

    @AfterTest
    fun letGo() {
        for (hold in attached) MapRequestHook.detach(hold)
        for (server in listOf(tiles, otherTiles, elsewhere)) server.close()
    }

    /** The client's own transport, which a map's request never takes. */
    private val unused =
        object : HttpExchange {
            override suspend fun send(request: ExchangeRequest): ExchangeResponse =
                error("A map's request leaves through MapLibre's client, never the transport.")
        }

    private fun client(
        credential: Credential,
        language: String? = "fr",
        server: LoopbackServer = tiles,
    ): Mountaya =
        Mountaya(
            credential,
            language = language,
            baseUrls = BaseUrls(tiles = Url(server.url("").removeSuffix("/"))),
            exchange = unused,
        )

    /**
     * A live map over [client], attached last, the hook's client built on [base], claiming [claimed], telling [told]
     * each answer it claims.
     */
    private fun map(
        client: Mountaya,
        transform: ((MapRequest) -> MapRequest)? = null,
        claimed: List<String> = emptyList(),
        told: MutableList<Told>? = null,
        base: OkHttpClient? = null,
    ): MapRequestHook.Hold =
        MapRequestHook
            .Hold(client, transform, base) { url, status, superseded, cause ->
                told?.add(Told(url, status, superseded, cause))
            }.also {
                it.claimed = claimed
                MapRequestHook.attach(it)
                attached += it
            }

    /** A client over [server] whose session token is read from [token] for each request, none sending the key alone. */
    private fun reading(
        token: AtomicReference<String?>,
        server: LoopbackServer,
    ): Mountaya = client(Credential.Publishable("pk_test_a", SessionToken.Function { token.get() }), server = server)

    /** The client MapLibre would fetch with, the hook's over MapLibre's defaults. */
    private val sending: OkHttpClient get() = MapRequestHook.client(null)

    /** Sends a request to [url] through the hook, its answer let go. */
    private fun send(url: String) {
        sending.newCall(Request.Builder().url(url).build()).execute().close()
    }

    /** Sends a request to [url] through the hook, and answers the request [server] read. */
    private fun sent(
        url: String,
        server: LoopbackServer = tiles,
    ): LoopbackServer.Seen {
        val before = server.requests.size
        send(url)
        return server.requests.drop(before).single()
    }

    @Test
    fun aTileApiRequestCarriesTheCredentialTheLanguageAndTheSdkThatMadeIt() {
        val client = client(Credential.Publishable("pk_test_a", SessionToken.Value("sess_a")))
        map(client)
        val tile = sent(tiles.url("/v1/zones/12/2093/1425"))
        val named = tile.headers["x-mountaya-client"]

        assertTrue(Regex("""^mountaya-kotlin/\d+\.\d+\.\d+\S*$""").matches(named.orEmpty()), named)
        assertEquals(
            client.mapRequestHeaders(Url(tiles.url("/v1/zones/12/2093/1425")))?.get("X-Mountaya-Client"),
            named,
        )
        assertEquals("pk_test_a", tile.headers["x-api-key"])
        assertEquals("sess_a", tile.headers["x-session-token"])
        assertEquals("fr", tile.headers["accept-language"])
    }

    /** A session token a function answers is read for each request, so a refreshed one reaches the next tile. */
    @Test
    fun aSessionTokenIsReadForEachRequest() {
        var token = "sess_a"
        map(client(Credential.Publishable("pk_test_a", SessionToken.Function { token })))

        assertEquals("sess_a", sent(tiles.url("/v1/zones/1/2/3")).headers["x-session-token"])
        token = "sess_b"
        assertEquals("sess_b", sent(tiles.url("/v1/glyphs/Inter/0-255")).headers["x-session-token"])
    }

    @Test
    fun aShareTokensTileNamesTheSdkBesideTheShareTokenAlone() {
        map(client(Credential.Share("share_a"), language = null))
        val tile = sent(tiles.url("/v1/itineraries/it/1/2/3"))

        assertTrue(tile.headers["x-mountaya-client"].orEmpty().startsWith("mountaya-kotlin/"))
        assertEquals("share_a", tile.headers["x-share-token"])
        assertNull(tile.headers["x-api-key"])
        assertEquals("*", tile.headers["accept-language"])
    }

    @Test
    fun aRequestToAnyOtherHostAStyleNamesIsSentNothingOfTheClientsItsNameIncluded() {
        map(client(Credential.Publishable("pk_test_a", SessionToken.Value("sess_a"))))
        val font = sent(elsewhere.url("/Inter/0-255.pbf", host = "localhost"), elsewhere)

        for (name in listOf("x-mountaya-client", "x-api-key", "x-session-token", "accept-language")) {
            assertNull(font.headers[name], name)
        }
    }

    /**
     * A redirect is followed with the headers of the request OkHttp was handed, never those a hop carried on the
     * network, so a credential given to each hop it reaches the Tile API on never follows one off it.
     */
    @Test
    fun aRedirectFromTheTileApiToAnotherHostCarriesNoCredentialAndOneWithinItCarriesItsOwn() {
        map(client(Credential.Publishable("pk_test_a", SessionToken.Value("sess_a"))))

        send(tiles.url("/v1/moved/1/2/3"))
        val moved = elsewhere.requests.single()
        for (name in listOf("x-mountaya-client", "x-api-key", "x-session-token", "accept-language")) {
            assertNull(moved.headers[name], name)
        }
        assertEquals("pk_test_a", tiles.requests.single().headers["x-api-key"], "the Tile API's own hop is signed")

        send(tiles.url("/v1/renamed/1/2/3"))
        val renamed = tiles.requests.last()
        assertEquals("/v1/zones/1/2/3", renamed.target)
        assertEquals("sess_a", renamed.headers["x-session-token"], "a hop within the Tile API is signed again")
    }

    /**
     * Every live map holds the hook at once: a request takes the credential of the map attached last whose
     * client's Tile API claims it, so two maps of two Tile APIs each send their own, and two maps of one send
     * the one attached last's.
     */
    @Test
    fun twoLiveMapsSendEachItsOwnCredentialAndTwoOfOneTileApiTheLatestOnes() {
        val first = map(client(Credential.Publishable("pk_test_a")))
        map(client(Credential.Publishable("pk_test_b"), server = otherTiles))

        assertEquals("pk_test_a", sent(tiles.url("/v1/zones/1/2/3")).headers["x-api-key"])
        assertEquals("pk_test_b", sent(otherTiles.url("/v1/zones/1/2/3"), otherTiles).headers["x-api-key"])

        val latest = map(client(Credential.Publishable("pk_test_c")))
        assertEquals("pk_test_c", sent(tiles.url("/v1/zones/1/2/3")).headers["x-api-key"])
        MapRequestHook.detach(latest)
        assertEquals("pk_test_a", sent(tiles.url("/v1/zones/1/2/3")).headers["x-api-key"])

        // The key never outlives the maps: a request made once none is live carries nothing.
        MapRequestHook.detach(first)
        for (hold in attached) MapRequestHook.detach(hold)
        assertNull(sent(tiles.url("/v1/zones/1/2/3")).headers["x-api-key"])
    }

    /**
     * The app's own hook is the one of the map attached last that set one, called first, and a header it sets
     * stands over the client's.
     */
    @Test
    fun theAppsOwnHookIsTheLatestMapsThatSetOneAndAHeaderItSetsStands() {
        fun naming(app: String): (MapRequest) -> MapRequest =
            { request -> MapRequest(request.url, request.headers + mapOf("accept-language" to "de", "X-App" to app)) }
        map(client(Credential.Publishable("pk_test_a")), transform = naming("first"))
        map(client(Credential.Publishable("pk_test_b"), server = otherTiles))

        val sent = sent(tiles.url("/v1/zones/1/2/3"))
        assertEquals("first", sent.headers["x-app"])
        assertEquals("de", sent.headers["accept-language"])
        assertEquals("pk_test_a", sent.headers["x-api-key"])

        map(client(Credential.Publishable("pk_test_c"), server = otherTiles), transform = naming("latest"))
        assertEquals("latest", sent(tiles.url("/v1/zones/1/2/3")).headers["x-app"])
    }

    /**
     * An answer is told to every live map that claims its address, to the one live map where none does, and to
     * nobody where several are live: a request that reached no answer as one carrying no status, and a request
     * the caller cancelled to nobody.
     */
    @Test
    fun aFailedAnswerIsToldToEveryLiveMapThatClaimsItAndToTheOneLiveMapWhereNoneDoes() {
        val unreachable = LoopbackServer().apply { close() }.url("/v1/zones/1/2/3")
        val toFirst = Collections.synchronizedList(mutableListOf<Told>())
        val toSecond = Collections.synchronizedList(mutableListOf<Told>())
        val alone = map(client(Credential.Publishable("pk_test_a")), told = toFirst)

        send(tiles.url("/v1/refused/1/2/3"))
        send(tiles.url("/v1/zones/1/2/3"))
        assertFailsWith<IOException> { send(unreachable) }
        assertEquals(
            listOf(Told(tiles.url("/v1/refused/1/2/3"), 402), Told(unreachable, null)),
            toFirst.toList(),
            "the one live map hears what no map claims, and an answer under 400 is no failure",
        )

        toFirst.clear()
        alone.claimed = listOf(tiles.url("/v1/refused/a/"), tiles.url("/v1/refused/shared/"))
        map(
            client(Credential.Publishable("pk_test_b")),
            claimed = listOf(tiles.url("/v1/refused/shared/")),
            told = toSecond,
        )
        send(tiles.url("/v1/refused/a/1/2/3"))
        send(tiles.url("/v1/refused/shared/1/2/3"))
        send(tiles.url("/v1/refused/nobodys/1/2/3"))
        val cancelled = sending.newCall(Request.Builder().url(tiles.url("/v1/refused/a/9/9/9")).build())
        cancelled.cancel()
        assertFailsWith<IOException> { cancelled.execute() }

        assertEquals(
            listOf(Told(tiles.url("/v1/refused/a/1/2/3"), 402), Told(tiles.url("/v1/refused/shared/1/2/3"), 402)),
            toFirst.toList(),
        )
        assertEquals(listOf(Told(tiles.url("/v1/refused/shared/1/2/3"), 402)), toSecond.toList())
    }

    /**
     * A request whose credential the client refuses at its read, or one carrying a header OkHttp cannot send,
     * never leaves the device: it fails before any connection is made, and is told.
     */
    @Test
    fun aRequestTheHookCannotSendNeverLeavesTheDeviceAndIsTold() {
        val told = Collections.synchronizedList(mutableListOf<Told>())
        map(client(Credential.Publishable("pk_test_a", SessionToken.Function { "sk_live" })), told = told)

        assertFailsWith<ConnectException> { send(tiles.url("/v1/zones/1/2/3")) }
        for (hold in attached) MapRequestHook.detach(hold)
        map(client(Credential.Share("share_a") { "visit=été" }), told = told)
        assertFailsWith<ConnectException> { send(tiles.url("/v1/zones/1/2/3")) }

        assertEquals(emptyList(), tiles.requests.map { it.target })
        assertEquals(listOf<Int?>(null, null), told.map { it.status })
    }

    /**
     * A request whose credential the client's read threw for never leaves, and is told carrying what the read threw, so
     * the map's failure says why it drew nothing (`map.events.access-refused`); one the hook cannot send for another
     * reason, the app's own hook throwing, carries none, since no read of the credential failed.
     */
    @Test
    fun aRequestWhoseCredentialReadThrewIsToldCarryingWhatItThrew() {
        val told = Collections.synchronizedList(mutableListOf<Told>())
        val locked = IllegalStateException("The token store is locked.")
        map(client(Credential.Publishable("pk_test_a", SessionToken.Function { throw locked })), told = told)
        assertFailsWith<ConnectException> { send(tiles.url("/v1/zones/1/2/3")) }

        for (hold in attached) MapRequestHook.detach(hold)
        val throwing: (MapRequest) -> MapRequest = { throw IllegalStateException("The app's own hook threw.") }
        map(client(Credential.Publishable("pk_test_a")), transform = throwing, told = told)
        assertFailsWith<ConnectException> { send(tiles.url("/v1/zones/1/2/3")) }

        assertEquals(emptyList(), tiles.requests.map { it.target })
        assertEquals(2, told.size)
        assertSame(locked, told[0].cause, "the read's own failure")
        assertNull(told[0].status, "a request that never left heard no status")
        assertNull(told[1].cause, "the app's own hook throwing is no read of the credential")
    }

    /**
     * A redirect's hop within the Tile API reads the credential again, and a read that throws there fails the call
     * before the hop leaves, told at the address the map asked for and carrying what the read threw.
     */
    @Test
    fun aRedirectsHopWhoseCredentialReadThrewIsToldCarryingWhatItThrew() {
        val told = Collections.synchronizedList(mutableListOf<Told>())
        val reads = AtomicInteger(0)
        val locked = IllegalStateException("The token store locked between two hops.")
        val token = SessionToken.Function { if (reads.getAndIncrement() == 0) "sess_a" else throw locked }
        map(client(Credential.Publishable("pk_test_a", token)), told = told)

        assertFailsWith<ConnectException> { send(tiles.url("/v1/renamed/1/2/3")) }

        assertEquals(listOf("/v1/renamed/1/2/3"), tiles.requests.map { it.target }, "the redirect's hop never left")
        assertEquals(listOf(tiles.url("/v1/renamed/1/2/3")), told.map { it.url })
        assertSame(locked, told.single().cause)
    }

    /**
     * An answer is told at the address the map asked for, never one a redirect led to, within the Tile API or off it:
     * that address lies under a source's tile templates or names the style asked for, so the map that claims it hears
     * it once, where the address the redirect led to is claimed by no map and, two being live, would be told to none.
     * A redirect answered with a tile still loads, and tells nothing.
     */
    @Test
    fun aRedirectedRefusalIsToldOnceAtTheAddressTheMapAskedForAndARedirectedTileStillLoads() {
        LoopbackServer { LoopbackServer.Answer(401) }.use { away ->
            LoopbackServer { path ->
                when {
                    path.startsWith("/v1/zones/moved/") -> {
                        LoopbackServer.Answer(302, mapOf("Location" to "/v1/denied/1/2/3"))
                    }

                    path.startsWith("/v1/zones/away/") -> {
                        LoopbackServer.Answer(302, mapOf("Location" to away.url("/1/2/3.pbf", host = "localhost")))
                    }

                    path.startsWith("/v1/zones/fine/") -> {
                        LoopbackServer.Answer(301, mapOf("Location" to "/v1/served/1/2/3"))
                    }

                    path.startsWith("/v1/denied/") -> {
                        LoopbackServer.Answer(401)
                    }

                    else -> {
                        LoopbackServer.Answer(200, body = "tile")
                    }
                }
            }.use { api ->
                val toZones = Collections.synchronizedList(mutableListOf<Told>())
                val toOther = Collections.synchronizedList(mutableListOf<Told>())
                // The text before the first `{` of the source's template `/v1/zones/{z}/{x}/{y}`, which its map claims.
                map(
                    client(Credential.Publishable("pk_test_a"), server = api),
                    claimed = listOf(api.url("/v1/zones/")),
                    told = toZones,
                )
                map(
                    client(Credential.Publishable("pk_test_b"), server = otherTiles),
                    claimed = listOf(otherTiles.url("/v1/")),
                    told = toOther,
                )

                send(api.url("/v1/zones/moved/1/2/3"))
                send(api.url("/v1/zones/away/1/2/3"))
                sending.newCall(Request.Builder().url(api.url("/v1/zones/fine/1/2/3")).build()).execute().use { tile ->
                    assertEquals(200, tile.code)
                    assertEquals("tile", tile.body.string())
                }

                assertEquals(
                    listOf(
                        "/v1/zones/moved/1/2/3",
                        "/v1/denied/1/2/3",
                        "/v1/zones/away/1/2/3",
                        "/v1/zones/fine/1/2/3",
                        "/v1/served/1/2/3",
                    ),
                    api.requests.map { it.target },
                    "each redirect followed",
                )
                assertEquals(listOf("/1/2/3.pbf"), away.requests.map { it.target })
                assertEquals(
                    listOf(
                        Told(api.url("/v1/zones/moved/1/2/3"), 401, superseded = false),
                        Told(api.url("/v1/zones/away/1/2/3"), 401, superseded = false),
                    ),
                    toZones.toList(),
                )
                assertEquals(emptyList(), toOther.toList())
            }
        }
    }

    /**
     * The case a map starting before its session's first token lands meets: a tile goes out under the key alone, an
     * organization that requires a session refuses it, and the refusal lands once the token has. Its request left
     * under a credential the client no longer sends, which the engine tells nobody; one that left under the token is
     * a refusal of the credential the client sends.
     */
    @Test
    fun aRefusalOfATileThatLeftUnderTheKeyAloneIsToldSupersededOnceTheFirstTokenLands() {
        val token = AtomicReference<String?>(null)
        LoopbackServer { path ->
            // The token lands while the server reads the tile that left without it.
            if (path.startsWith("/v1/early/")) token.set("sess_a")
            LoopbackServer.Answer(401)
        }.use { api ->
            val told = Collections.synchronizedList(mutableListOf<Told>())
            map(reading(token, api), told = told)

            send(api.url("/v1/early/12/2093/1425"))
            send(api.url("/v1/zones/12/2093/1425"))

            assertNull(api.requests[0].headers["x-session-token"], "the first tile left under the key alone")
            assertEquals("sess_a", api.requests[1].headers["x-session-token"])
            assertEquals(
                listOf(
                    Told(api.url("/v1/early/12/2093/1425"), 401, superseded = true),
                    Told(api.url("/v1/zones/12/2093/1425"), 401, superseded = false),
                ),
                told.toList(),
            )
        }
    }

    /**
     * A visitor's cookie read for each request moves on: a 403 of a tile that left with the old one is superseded, and
     * so is a refusal arriving where the client now refuses the credential it would send, its read failing.
     */
    @Test
    fun aForbiddenTileThatLeftWithACookieSinceChangedOrUnderACredentialTheClientNowRefusesIsToldSuperseded() {
        val cookie = AtomicReference("visit=1")
        val token = AtomicReference<String?>("sess_a")
        LoopbackServer { path ->
            when {
                path.startsWith("/v1/moved-on/") -> cookie.set("visit=2")
                path.startsWith("/v1/revoked/") -> token.set("sk_live")
            }
            LoopbackServer.Answer(403)
        }.use { api ->
            val told = Collections.synchronizedList(mutableListOf<Told>())
            map(client(Credential.Share("share_a") { cookie.get() }, server = api), told = told)

            send(api.url("/v1/moved-on/1/2/3"))
            send(api.url("/v1/zones/1/2/3"))
            assertEquals(listOf("visit=1", "visit=2"), api.requests.map { it.headers["cookie"] })

            for (hold in attached) MapRequestHook.detach(hold)
            map(reading(token, api), told = told)
            send(api.url("/v1/revoked/1/2/3"))

            assertEquals(
                listOf(
                    Told(api.url("/v1/moved-on/1/2/3"), 403, superseded = true),
                    Told(api.url("/v1/zones/1/2/3"), 403, superseded = false),
                    Told(api.url("/v1/revoked/1/2/3"), 403, superseded = true),
                ),
                told.toList(),
            )
        }
    }

    /**
     * A refusal of the entitlement, a 402, and every other failure are read for no credential: the engine tells them
     * as they are whatever the request left with.
     */
    @Test
    fun onlyARefusalOfTheCredentialIsReadForTheCredentialItsRequestLeftWith() {
        val token = AtomicReference<String?>(null)
        LoopbackServer { path ->
            token.set("sess_${path.substringAfterLast('/')}")
            LoopbackServer.Answer(path.substringAfter("/v1/").substringBefore('/').toInt())
        }.use { api ->
            val told = Collections.synchronizedList(mutableListOf<Told>())
            map(reading(token, api), told = told)

            for (status in listOf(402, 404, 429, 500)) send(api.url("/v1/$status/$status"))

            assertEquals(
                listOf(402, 404, 429, 500).map { Told(api.url("/v1/$it/$it"), it, superseded = false) },
                told.toList(),
            )
        }
    }

    /**
     * A redirect's hop is signed again for its own address, so the credential a refusal is read against is the one
     * its call's last hop went out with, never the first's, and read again for the address that hop went to.
     */
    @Test
    fun aRefusalIsReadAgainstTheCredentialTheCallsLastHopWentOutWith() {
        val token = AtomicReference<String?>("sess_a")
        LoopbackServer { path ->
            if (path.startsWith("/v1/renamed/")) {
                token.set("sess_b")
                LoopbackServer.Answer(301, mapOf("Location" to "/v1/zones/1/2/3"))
            } else {
                LoopbackServer.Answer(401)
            }
        }.use { api ->
            val told = Collections.synchronizedList(mutableListOf<Told>())
            map(reading(token, api), told = told)

            send(api.url("/v1/renamed/1/2/3"))

            assertEquals(listOf("sess_a", "sess_b"), api.requests.map { it.headers["x-session-token"] })
            // Told at the address asked for, and read against the credential of the address the last hop went to.
            assertEquals(listOf(Told(api.url("/v1/renamed/1/2/3"), 401, superseded = false)), told.toList())
        }
    }

    /**
     * A call no hop of which reached the network, answered by the app's own client, is read against the credential its
     * request entered the hook with.
     */
    @Test
    fun aRefusalNoHopOfWhichReachedTheNetworkIsReadAgainstTheCredentialItsRequestEnteredWith() {
        val token = AtomicReference<String?>("sess_a")
        val answering =
            OkHttpClient
                .Builder()
                .addInterceptor { chain ->
                    val path = chain.request().url.encodedPath
                    if (path.startsWith("/v1/moved-on/")) token.set("sess_b")
                    Response
                        .Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(401)
                        .message("Unauthorized")
                        .body("".toResponseBody(null))
                        .build()
                }.build()
        val told = Collections.synchronizedList(mutableListOf<Told>())
        map(
            client(Credential.Publishable("pk_test_a", SessionToken.Function { token.get() })),
            told = told,
            base = answering,
        )
        val sending = MapRequestHook.client(answering)

        sending.newCall(Request.Builder().url(tiles.url("/v1/zones/1/2/3")).build()).execute().close()
        sending.newCall(Request.Builder().url(tiles.url("/v1/moved-on/1/2/3")).build()).execute().close()

        assertEquals(emptyList(), tiles.requests.map { it.target }, "the app's client answered every request")
        assertEquals(
            listOf(
                Told(tiles.url("/v1/zones/1/2/3"), 401, superseded = false),
                Told(tiles.url("/v1/moved-on/1/2/3"), 401, superseded = true),
            ),
            told.toList(),
        )
    }
}
