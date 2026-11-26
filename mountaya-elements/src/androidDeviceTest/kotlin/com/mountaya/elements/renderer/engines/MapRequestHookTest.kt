package com.mountaya.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.mountaya.client.BaseUrls
import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import com.mountaya.client.SessionToken
import com.mountaya.client.Url
import com.mountaya.client.generated.TileSchema
import com.mountaya.elements.MapWindow.onMain
import com.mountaya.elements.MapWindow.showing
import com.mountaya.elements.MapWindow.until
import com.mountaya.geo.LayerRank
import com.mountaya.geo.ZoomBounds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Rule
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView
import java.io.IOException
import java.net.ConnectException
import java.util.Collections
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * The process's request hook, on the device MapLibre runs on, over the OkHttp release the app resolves: the
 * client's own, 5.x, which raises the 4.x MapLibre names. Every MapLibre test runs one at a time, the
 * instrumentation's own order, since the hook is the process's. What each hop carries on the network is read
 * by a Tile API on the device's loopback address, as MapLibre's own requests reach it; the hook's every rule
 * of a hop is the host tests' too.
 */
class MapRequestHookTest {
    @get:Rule
    val compose = createComposeRule()

    /**
     * MapLibre's HTTP module reads MapLibre's own instance as it loads, so MapLibre starts before the hook is
     * first installed, as a map starts it, whichever test of the process runs first.
     */
    @BeforeTest
    fun startMapLibre() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync { MapLibre.getInstance(instrumentation.targetContext.applicationContext) }
    }

    private fun client(
        credential: Credential,
        tiles: Url = StubTileApi.tiles,
        language: String? = "fr",
    ): Mountaya = Mountaya(credential, language = language, baseUrls = BaseUrls(tiles = tiles))

    /** A map's hold over [base], told each answer it claims through [told], with its address and its status. */
    private fun hold(
        client: Mountaya,
        base: OkHttpClient? = StubTileApi.client,
        told: ((String, Int?) -> Unit)? = null,
    ): MapRequestHook.Hold =
        MapRequestHook.Hold(client, null, base, told?.let { tell -> { url, status, _, _ -> tell(url, status) } })

    @Test
    fun theHookIsMapLibresOneRequestHookItsClientBuiltOnTheAppsOwnItsInterceptorsAheadOfTheApps() {
        val first = hold(client(Credential.Publishable("pk_test_a")))
        val second = hold(client(Credential.Publishable("pk_test_b")))
        val bare = hold(client(Credential.Publishable("pk_test_b")), base = null)
        try {
            MapRequestHook.install(first)
            val built = assertNotNull(MapRequestHook.client, "the hook builds its client")

            assertTrue(OkHttp.VERSION.startsWith("5."), OkHttp.VERSION)
            assertEquals(listOf(MapRequestHook.answers, StubTileApi), built.interceptors)
            assertEquals(listOf(MapRequestHook.headers), built.networkInterceptors)
            MapRequestHook.install(second)
            assertSame(built, MapRequestHook.client)
            MapRequestHook.install(bare)
            val bareClient = assertNotNull(MapRequestHook.client, "the hook builds its client")
            assertEquals(listOf(MapRequestHook.answers), bareClient.interceptors)
            assertEquals(listOf(MapRequestHook.headers), bareClient.networkInterceptors)
            // The map attached last lets go, and MapLibre fetches over the app's own client of the one before it.
            MapRequestHook.release(bare)
            val kept = assertNotNull(MapRequestHook.client, "the hook keeps its client")
            assertEquals(listOf(MapRequestHook.answers, StubTileApi), kept.interceptors)
        } finally {
            for (hold in listOf(bare, second, first)) MapRequestHook.release(hold)
        }
    }

    @Test
    fun aRequestWhoseSessionTokenTheClientRefusesNeverLeavesTheDevice() {
        val refusing =
            hold(
                client(
                    Credential.Publishable(
                        "pk_test_a",
                        SessionToken.Function {
                            "sk_live"
                        },
                    ),
                ),
            )
        MapRequestHook.install(refusing)
        val before = StubTileApi.requests.size
        try {
            assertFailsWith<ConnectException> {
                assertNotNull(MapRequestHook.client, "the hook builds its client")
                    .newCall(Request.Builder().url("https://tiles.example.test/v1/zones/1/2/3").build())
                    .execute()
            }
            assertEquals(before, StubTileApi.requests.size)
        } finally {
            MapRequestHook.release(refusing)
        }
    }

    @Test
    fun aFailedAnswerIsToldWithItsAddressAndStatusOneOfNoAnswerWithNoneAndACancelledOneToNobody() {
        val told = java.util.Collections.synchronizedList(mutableListOf<Pair<String, Int?>>())
        val alone = hold(client(Credential.Publishable("pk_test_a"))) { url, status -> told += url to status }
        // Claimed, so what is told does not hang on whether another map of the process is live.
        alone.claimed = listOf("https://tiles.example.test/", "https://unreachable.test/")
        MapRequestHook.install(alone)
        try {
            val sending = assertNotNull(MapRequestHook.client, "the hook builds its client")

            sending
                .newCall(
                    Request.Builder().url("https://tiles.example.test/v1/refused/1/2/3").build(),
                ).execute()
                .close()
            sending
                .newCall(
                    Request.Builder().url("https://tiles.example.test/v1/zones/1/2/3").build(),
                ).execute()
                .close()
            assertFailsWith<IOException> {
                sending.newCall(Request.Builder().url("https://unreachable.test/v1/zones/1/2/3").build()).execute()
            }
            val cancelled = sending.newCall(Request.Builder().url("https://unreachable.test/v1/cancelled").build())
            cancelled.cancel()
            assertFailsWith<IOException> { cancelled.execute() }

            assertEquals(
                listOf(
                    "https://tiles.example.test/v1/refused/1/2/3" to 402,
                    "https://unreachable.test/v1/zones/1/2/3" to null,
                ),
                synchronized(told) { told.toList() },
                "an answer under 400 is no failure, and a cancelled request is told to nobody",
            )
        } finally {
            MapRequestHook.release(alone)
        }
    }

    @Test
    fun theMapsOwnRequestsPassThroughTheHookItsStyleAndItsTilesFetchedWithTheCredentialOnTheNetwork() {
        val elsewhere = LoopbackServer { LoopbackServer.Answer(204) }
        val tiles = LoopbackTileApi(elsewhere)
        try {
            val map =
                MapEngine(
                    client(Credential.Publishable("pk_test_map", SessionToken.Value("sess_map")), tiles = tiles.origin),
                    style = tiles.style,
                )
            compose.showing(map, okHttpClient = null)

            val style = tiles.server.requests.first { it.target.endsWith("/style") }
            assertEquals("pk_test_map", style.headers["x-api-key"])
            assertEquals("sess_map", style.headers["x-session-token"])
            assertEquals("fr", style.headers["accept-language"])
            // Once the map draws, a tile of the Tile API carries the credential, and a tile of another host the
            // style names carries nothing of it.
            compose.waitUntil(MapWindow.TIMEOUT) {
                tiles.server.requests.any {
                    !it.target.endsWith(
                        "/style",
                    ) && it.headers["x-session-token"] == "sess_map"
                }
            }
            compose.waitUntil(MapWindow.TIMEOUT) { elsewhere.requests.isNotEmpty() }
            assertTrue(
                elsewhere.requests.all {
                    it.headers["x-api-key"] == null && it.headers["x-session-token"] == null &&
                        it.headers["x-mountaya-client"] == null
                },
            )
        } finally {
            tiles.server.close()
            elsewhere.close()
        }
    }

    /**
     * Two maps live at once, each over a Tile API of its own: each map's requests carry its own client's
     * credential and never the other's, and a source the first draws refused is a refusal of the first alone.
     */
    @Test
    fun twoLiveMapsEachSendTheirOwnCredentialAndHearTheirOwnRefusalAlone() {
        val elsewhere = LoopbackServer { LoopbackServer.Answer(204) }
        val firstApi = LoopbackTileApi(elsewhere)
        val secondApi = LoopbackTileApi(elsewhere)
        try {
            val first =
                MapEngine(
                    client(Credential.Publishable("pk_test_first"), tiles = firstApi.origin),
                    style = firstApi.style,
                )
            val second =
                MapEngine(
                    client(Credential.Publishable("pk_test_second"), tiles = secondApi.origin),
                    style = secondApi.style,
                )
            compose.setContent {
                val context = LocalContext.current
                val renderers =
                    remember {
                        MapLibre.getInstance(context.applicationContext)
                        listOf(first, second).map { MapRenderer(it, MapView(context), null) {} }
                    }
                Column { for (renderer in renderers) RendererView(renderer, Modifier.size(390.dp, 400.dp)) }
            }

            compose.waitUntil(MapWindow.TIMEOUT) {
                listOf(firstApi, secondApi).all { api -> api.server.requests.any { !it.target.endsWith("/style") } }
            }
            assertTrue(firstApi.server.requests.all { it.headers["x-api-key"] == "pk_test_first" })
            assertTrue(secondApi.server.requests.all { it.headers["x-api-key"] == "pk_test_second" })

            compose.onMain {
                first.addSource(
                    MapSource(
                        "refused",
                        MapSource.Kind.Vector(
                            listOf(firstApi.server.url("/v1/refused/{z}/{x}/{y}")),
                            ZoomBounds(0.0, 14.0),
                        ),
                    ),
                )
                first.addLayer(
                    MapLayer(
                        id = "refused-lines",
                        type = TileSchema.TileLayerType.Line,
                        source = "refused",
                        sourceLayer = "lines",
                        rank = LayerRank.Overlay.rank,
                    ),
                )
            }
            compose.until { first.state.value.isAccessRequired }
            compose.onMain {
                assertFalse(
                    second.state.value.isAccessRequired,
                    "the second map heard the first's refusal",
                )
            }
        } finally {
            firstApi.server.close()
            secondApi.server.close()
            elsewhere.close()
        }
    }

    /**
     * A style asked for before the session's first token lands goes out under the key alone; an organization that
     * requires a session refuses it, and the refusal lands once the token has. It refuses a credential the client no
     * longer sends: it is told to nobody, and the style is asked for again, under the token, and drawn, where MapLibre's
     * own failure to load it, read after the hook's word, would have given the map back the base map shown.
     */
    @Test
    fun aStyleRefusedUnderACredentialTheClientNoLongerSendsIsAskedForAgainAndToldToNobody() {
        val elsewhere = LoopbackServer { LoopbackServer.Answer(204) }
        val token = AtomicReference<String?>(null)
        // The winter style's first ask leaves under the key alone, and the token lands as the server refuses it.
        val tiles =
            LoopbackTileApi(elsewhere) { path ->
                if (path.endsWith("/winter/light/style") && token.compareAndSet(null, "sess_map")) {
                    LoopbackServer.Answer(401)
                } else {
                    null
                }
            }
        val winter = Url(tiles.server.url("/v1/terrains/winter/light/style"))
        val events = Collections.synchronizedList(mutableListOf<MapEvent>())
        val listening = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        try {
            val map =
                MapEngine(
                    client(
                        Credential.Publishable("pk_test_map", SessionToken.Function { token.get() }),
                        tiles = tiles.origin,
                    ),
                    style = tiles.style,
                )
            val renderer = compose.showing(map, okHttpClient = null)
            compose.onMain {
                listening.launch(start = CoroutineStart.UNDISPATCHED) { map.events().collect { events += it } }
                map.setStyle(winter)
            }
            compose.until { renderer.map?.style?.uri == winter.toString() }

            val asked = tiles.server.requests.filter { it.target.endsWith("/winter/light/style") }
            assertEquals(listOf(null, "sess_map"), asked.map { it.headers["x-session-token"] })
            compose.onMain {
                assertEquals(winter, map.state.value.style, "the style asked for is the one shown")
                assertFalse(map.state.value.isAccessRequired, "a refusal of a credential given up stands for nothing")
            }
            val told = synchronized(events) { events.toList() }
            assertTrue(told.none { it is MapEvent.AccessRefused || it is MapEvent.Failure }, "$told")
        } finally {
            listening.cancel()
            tiles.server.close()
            elsewhere.close()
        }
    }

    /**
     * A style refused under the credential the client sends is one of the map's own requests refused: the hook tells it
     * once, as a refusal of access, and MapLibre's own failure to load it, which gives the map back the base map shown,
     * tells nothing more. The base map given back answers nothing as it loads again, so the refusal stands until a style
     * the map asks for loads (`map.events.access-standing`).
     */
    @Test
    fun aStyleRefusedUnderTheCredentialTheClientSendsIsToldOnceAsARefusalAndNeverAsAFailure() {
        val elsewhere = LoopbackServer { LoopbackServer.Answer(204) }
        val tiles =
            LoopbackTileApi(elsewhere) { path ->
                if (path.endsWith("/refused/light/style")) LoopbackServer.Answer(401) else null
            }
        val refused = Url(tiles.server.url("/v1/terrains/refused/light/style"))
        val events = Collections.synchronizedList(mutableListOf<MapEvent>())
        val listening = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        try {
            val winter = Url(tiles.server.url("/v1/terrains/winter/light/style"))
            val map =
                MapEngine(client(Credential.Publishable("pk_test_map"), tiles = tiles.origin), style = tiles.style)
            val renderer = compose.showing(map, okHttpClient = null)
            compose.onMain {
                listening.launch(start = CoroutineStart.UNDISPATCHED) { map.events().collect { events += it } }
                map.setStyle(refused)
            }
            compose.until { synchronized(events) { events.any { it is MapEvent.AccessRefused } } }
            compose.until { renderer.map?.style?.uri == tiles.style.toString() }

            assertEquals(1, tiles.server.requests.count { it.target.endsWith("/refused/light/style") }, "asked once")
            compose.onMain {
                // Read on the main thread once the base map given back loaded there, where the engine is told of it.
                assertEquals(tiles.style, map.state.value.style, "the map is given back the base map shown")
                assertTrue(map.state.value.isAccessRequired, "the base map given back ends no refusal")
                assertTrue(synchronized(events) { MapEvent.AccessRestored !in events }, "nothing restored yet")
                map.setStyle(winter)
            }
            compose.until { synchronized(events) { MapEvent.AccessRestored in events } }
            assertEquals(
                listOf(MapEvent.AccessRefused(401, refused.toString()), MapEvent.AccessRestored),
                synchronized(events) { events.toList() },
            )
        } finally {
            listening.cancel()
            tiles.server.close()
            elsewhere.close()
        }
    }

    /**
     * A style answered 200 whose document MapLibre does not read fails with no failure the hook heard: the renderer
     * tells it as a failure of the style's own address carrying no status, as `sdk-swift` tells it, before the map is
     * given back the base map shown, so no style fails without a word.
     */
    @Test
    fun aStyleAnswered200ThatDoesNotParseIsToldAsAFailureWithNoStatusAndTheBaseMapShownComesBack() {
        val elsewhere = LoopbackServer { LoopbackServer.Answer(204) }
        val tiles =
            LoopbackTileApi(elsewhere) { path ->
                if (path.endsWith("/broken/light/style")) {
                    LoopbackServer.Answer(200, mapOf("Content-Type" to "application/json"), "not a style")
                } else {
                    null
                }
            }
        val broken = Url(tiles.server.url("/v1/terrains/broken/light/style"))
        val events = Collections.synchronizedList(mutableListOf<MapEvent>())
        val listening = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        try {
            val map =
                MapEngine(client(Credential.Publishable("pk_test_map"), tiles = tiles.origin), style = tiles.style)
            compose.showing(map, okHttpClient = null)
            compose.onMain {
                listening.launch(start = CoroutineStart.UNDISPATCHED) { map.events().collect { events += it } }
                map.setStyle(broken)
            }
            compose.until { synchronized(events) { events.any { it is MapEvent.Failure } } }
            compose.until { map.state.value.style == tiles.style }

            assertEquals(
                listOf<MapEvent>(MapEvent.Failure(broken.toString(), null)),
                synchronized(events) { events.filter { it is MapEvent.Failure || it is MapEvent.AccessRefused } },
            )
            assertEquals(1, tiles.server.requests.count { it.target.endsWith("/broken/light/style") }, "asked once")
        } finally {
            listening.cancel()
            tiles.server.close()
            elsewhere.close()
        }
    }

    /**
     * LoopbackTileApi is a Tile API on the device's loopback address: its base map's style, whose vector tiles
     * it serves and whose raster tiles [elsewhere] does, under a name that is not its own, a tile under
     * `/v1/refused/` refused a 402, and every other tile answered with no content, where [answering] gives a
     * path no answer of its own.
     */
    private class LoopbackTileApi(
        elsewhere: LoopbackServer,
        answering: (path: String) -> LoopbackServer.Answer? = { null },
    ) {
        val server: LoopbackServer =
            LoopbackServer { path ->
                answering(path) ?: when {
                    path.endsWith("/style") -> {
                        LoopbackServer.Answer(200, mapOf("Content-Type" to "application/json"), document)
                    }

                    path.startsWith("/v1/refused/") -> {
                        LoopbackServer.Answer(402)
                    }

                    else -> {
                        LoopbackServer.Answer(204)
                    }
                }
            }

        /** The origin a client reaches it at. */
        val origin: Url = Url(server.url("").removeSuffix("/"))

        /** Its base map's style document. */
        val style: Url = Url(server.url("/v1/terrains/outdoor/light/style"))

        private val document: String =
            """{"version": 8, "name": "loopback",
             "sources": {
               "base": {"type": "vector", "tiles": ["${server.url(
                "/v1/terrains/outdoor/light/{z}/{x}/{y}",
            )}"], "maxzoom": 14},
               "elsewhere": {"type": "raster", "tiles": ["${elsewhere.url(
                "/{z}/{x}/{y}.png",
                host = "localhost",
            )}"], "tileSize": 256}
             },
             "layers": [
               {"id": "base-background", "type": "background", "paint": {"background-color": "#ffffff"}},
               {"id": "base-elsewhere", "type": "raster", "source": "elsewhere"},
               {"id": "base-water", "type": "fill", "source": "base", "source-layer": "water", "paint": {"fill-color": "#0000ff"}}
             ]}"""
    }
}
