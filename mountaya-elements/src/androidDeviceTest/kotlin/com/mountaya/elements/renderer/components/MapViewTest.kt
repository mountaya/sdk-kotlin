package com.mountaya.elements

import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.click
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.mountaya.client.BaseUrls
import com.mountaya.client.Credential
import com.mountaya.client.ExchangeBody
import com.mountaya.client.ExchangeRequest
import com.mountaya.client.ExchangeResponse
import com.mountaya.client.HttpExchange
import com.mountaya.client.Mountaya
import com.mountaya.client.Url
import com.mountaya.client.generated.TileSchema
import com.mountaya.elements.MapWindow.onMain
import com.mountaya.elements.MapWindow.until
import com.mountaya.geo.CameraPose
import com.mountaya.geo.GeoJsonFeatureCollection
import com.mountaya.geo.HexColor
import com.mountaya.geo.LayerRank
import com.mountaya.geo.OrganizationRead
import com.mountaya.geo.ThemeToken
import com.mountaya.geo.generated.ElementTokens
import org.junit.Rule
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.layers.LineLayer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The composable, mounted in a test's own activity on the device. */
class MapViewTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun theViewMountsInComposeDrawsItsEnginesStyleAndHandsMapLibresMapToOnReady() {
        val client = Mountaya(Credential.Publishable("pk_test_view"), baseUrls = BaseUrls(tiles = StubTileApi.tiles))
        val map = MapEngine(client, style = StubTileApi.styleUrl)
        map.addSource(MapSource("empty", MapSource.Kind.GeoJson(GeoJsonFeatureCollection(emptyList()))))
        map.addLayer(
            MapLayer(
                id = "route",
                type = TileSchema.TileLayerType.Line,
                source = "empty",
                rank = LayerRank.ItineraryLine.rank,
            ),
        )
        val ready = AtomicReference<MapLibreMap?>(null)
        compose.setContent {
            MapView(
                map,
                MapWindow.screen,
                okHttpClient = StubTileApi.client,
                onReady = { ready.set(it) },
            )
        }

        compose.waitUntil(MapWindow.TIMEOUT) { ready.get() != null }
        compose.onMain {
            val style = assertNotNull(ready.get()?.style, "the ready map holds its style")
            assertNotNull(style.getLayer("base-background"), "the style holds its background")
            assertTrue(style.getLayer("route") is LineLayer)
        }
    }

    /**
     * The renderer takes the accent its pins are drawn in, and the tooltip's ground and text a marker's label is drawn
     * in, as the host's tokens state them under the scheme in force, and takes them again as the scheme changes.
     */
    @Test
    fun aMapUnderTheHostsTokensHandsItsRendererTheAccentAndTheTooltipTheyReadUnderTheSchemeInForce() {
        val map = engine()
        val tokens =
            ElementTokens.standard.copy {
                accent =
                    ThemeToken(light = HexColor("#1c7ed6"), dark = HexColor("#e9501e"))
                tooltipBg = ThemeToken(light = HexColor("#343a40"), dark = HexColor("#e9ecef"))
                tooltipFg = ThemeToken(light = HexColor("#f8f9fa"), dark = HexColor("#212529"))
            }
        var dark by mutableStateOf(true)
        val loaded = AtomicBoolean(false)
        var shown: MapRenderer? = null
        compose.setContent {
            val context = LocalContext.current
            val renderer =
                remember {
                    MapLibre.getInstance(context.applicationContext)
                    MapRenderer(
                        map,
                        org.maplibre.android.maps
                            .MapView(context),
                        StubTileApi.client,
                    ) { loaded.set(true) }
                }
            shown = renderer
            Controls.Drawn("map", dark = dark) {
                CompositionLocalProvider(
                    LocalMountayaTokens provides tokens,
                ) { RendererView(renderer, MapWindow.screen) }
            }
        }
        compose.waitUntil(MapWindow.TIMEOUT) { loaded.get() }
        val renderer = assertNotNull(shown, "the renderer is shown once the style is loaded")

        compose.until { renderer.accent == HexColor("#e9501e") }
        compose.onMain {
            assertEquals(HexColor("#e9ecef"), renderer.tooltipBg, "the label's ground under the dark scheme")
            assertEquals(HexColor("#212529"), renderer.tooltipFg, "the label's text under the dark scheme")
        }
        dark = false
        compose.until { renderer.accent == HexColor("#1c7ed6") }
        compose.onMain {
            assertEquals(HexColor("#343a40"), renderer.tooltipBg, "the label's ground under the light scheme")
            assertEquals(HexColor("#f8f9fa"), renderer.tooltipFg, "the label's text under the light scheme")
        }
    }

    /** Shows [map] in the composable over the stub, handing each click and each camera at rest to the lists given. */
    private fun showing(
        map: MapEngine,
        taps: MutableList<MapTap>,
        rested: MutableList<CameraPose>,
    ): MapLibreMap {
        val ready = AtomicReference<MapLibreMap?>(null)
        compose.setContent {
            MapView(
                map,
                MapWindow.screen,
                okHttpClient = StubTileApi.client,
                onReady = { ready.set(it) },
                onClick = { taps += it },
                onCameraChange = { rested += it },
            )
        }
        compose.waitUntil(MapWindow.TIMEOUT) { ready.get() != null }
        return assertNotNull(ready.get(), "the map is ready")
    }

    private fun engine(): MapEngine =
        MapEngine(
            Mountaya(Credential.Publishable("pk_test_view"), baseUrls = BaseUrls(tiles = StubTileApi.tiles)),
            style = StubTileApi.styleUrl,
            camera = CameraPose(listOf(6.86, 45.83), 10.0, 0.0, 0.0),
        )

    @Test
    fun aViewHandedANewEngineDrawsItAndBeginsItsOrganizationRead() {
        // Each engine reads its organization from an origin that refuses every read, so a read begun fails at once.
        fun refused(origin: String): MapEngine =
            MapEngine(
                Mountaya(
                    Credential.Publishable("pk_test_rebind"),
                    baseUrls = BaseUrls(rest = Url(origin), tiles = StubTileApi.tiles),
                    exchange = Refusing,
                ),
                style = StubTileApi.styleUrl,
            )
        val first = refused("https://first.rebind.test")
        val second = refused("https://second.rebind.test")
        var shown by mutableStateOf(first)
        compose.setContent { MapView(shown, MapWindow.screen, okHttpClient = StubTileApi.client) }
        compose.waitUntil(MapWindow.TIMEOUT) { first.organizationRead.value == OrganizationRead.Failed }

        compose.runOnIdle { shown = second }

        compose.waitUntil(MapWindow.TIMEOUT) { second.organizationRead.value == OrganizationRead.Failed }
    }

    /**
     * A view handed a new engine hosts the renderer it makes for it in place of the last: the new renderer's view is
     * attached, the last one's leaves the window, and the new map draws its own mark. From a map whose client holds no
     * publishable key, which draws the mark at once, to one whose organization holds white label, which draws none, and
     * from there to one whose organization does not, whose mark shows on the view now attached.
     */
    @Test
    fun aViewHandedANewEngineHostsItsRendererAndDrawsItsOwnMark() {
        val keyless =
            MapEngine(
                Mountaya(Credential.Share("share_test_swap"), baseUrls = BaseUrls(tiles = StubTileApi.tiles)),
                style = StubTileApi.styleUrl,
            )

        /** A map whose organization, read from an origin of its own, answers [whiteLabel]. */
        fun answering(
            origin: String,
            whiteLabel: Boolean,
        ): MapEngine =
            MapEngine(
                Mountaya(
                    Credential.Publishable("pk_test_swap"),
                    baseUrls = BaseUrls(rest = Url(origin), tiles = StubTileApi.tiles),
                    exchange = Organizations(whiteLabel),
                ),
                style = StubTileApi.styleUrl,
            )
        val whiteLabelled = answering("https://white-label.swap.test", whiteLabel = true)
        val marked = answering("https://marked.swap.test", whiteLabel = false)
        var shown by mutableStateOf(keyless)
        var root: View? = null
        compose.setContent {
            root = LocalView.current
            MapView(shown, MapWindow.screen, okHttpClient = StubTileApi.client)
        }

        /** The layers the pins and the mark stand on that the window holds, one for each renderer's view attached. */
        fun layers(): List<MapRenderer.PinLayer> {
            val window = root ?: return emptyList()
            return descendantsOf(window).filterIsInstance<MapRenderer.PinLayer>().toList()
        }

        /** The mark [layer] shows, laid out and shown in its window, or null where it shows none. */
        fun markOn(layer: MapRenderer.PinLayer): WatermarkView? =
            (0 until layer.childCount)
                .map { layer.getChildAt(it) }
                .filterIsInstance<WatermarkView>()
                .singleOrNull { it.isAttachedToWindow && it.isShown && it.width > 0 }

        compose.until { layers().singleOrNull()?.let(::markOn) != null }
        val first = compose.onMain { layers().single() }

        compose.runOnIdle { shown = whiteLabelled }
        compose.waitUntil(MapWindow.TIMEOUT) { whiteLabelled.organizationRead.value == OrganizationRead.Answered(true) }
        compose.until { layers().singleOrNull()?.let { it !== first } == true }
        val second =
            compose.onMain {
                val layer = layers().single()
                assertFalse(first.isAttachedToWindow, "the last renderer's view leaves the window")
                assertTrue(layer.isShown, "the new renderer's view is attached and shown")
                assertNull(markOn(layer), "a map whose organization holds white label draws no mark")
                layer
            }

        compose.runOnIdle { shown = marked }
        compose.waitUntil(MapWindow.TIMEOUT) { marked.organizationRead.value == OrganizationRead.Answered(false) }
        compose.until { layers().singleOrNull()?.let { it !== second && markOn(it) != null } == true }
        compose.onMain {
            assertFalse(second.isAttachedToWindow, "the white-label map's view leaves the window")
            assertNull(markOn(second), "and it never drew a mark")
        }
    }

    /** Every view [view] holds, itself first, then each child's in order. */
    private fun descendantsOf(view: View): Sequence<View> =
        sequence {
            yield(view)
            if (view is ViewGroup) {
                for (index in 0 until view.childCount) yieldAll(descendantsOf(view.getChildAt(index)))
            }
        }

    /** Answers every read of an organization with one of [whiteLabel], as the API answers a key it knows. */
    private class Organizations(
        private val whiteLabel: Boolean,
    ) : HttpExchange {
        override suspend fun send(request: ExchangeRequest): ExchangeResponse =
            ExchangeResponse(
                200,
                mapOf("Content-Type" to "application/json"),
                ExchangeBody(
                    """{"data": {"created_at": "2026-01-01T00:00:00Z", "etag": "e",
                    "id": "00000000-0000-4000-8000-000000000000", "name": "swap", "awaiting_agreement": false,
                    "updated_at": "2026-01-01T00:00:00Z", "white_label": $whiteLabel,
                    "settings": {"etag": "s",
                      "default_props": {"preferred_language": "en", "preferred_theme": "auto",
                        "preferred_unit_length": "metric", "preferred_unit_pace": "per-time",
                        "preferred_unit_slope": "degrees", "preferred_unit_temperature": "celsius",
                        "segments_color": "#bc4a51", "terrain": "", "terrain_variant": "light",
                        "with_access_restrictions": true, "with_collection_itineraries": true,
                        "with_collection_zones": true, "with_directions_control": true, "with_download_control": true,
                        "with_navigation_control": true, "with_overlays": [], "with_overlays_control": false,
                        "with_preferences_control": true, "with_profile_analysis": true,
                        "with_profile_characteristics": true, "with_profile_elevation": true,
                        "with_profile_pace_control": true, "with_streetview_control": true, "with_terrains": []},
                      "safety_rules": {"boundaries": [], "domains": [], "require_session_token": false,
                        "with_collection_itineraries": true, "with_collection_zones": true, "with_overlays": [],
                        "with_overlays_control": false, "with_profile_analysis": true, "with_profile_elevation": true,
                        "zoom_max": 22, "zoom_min": 0},
                      "theme": {"colors": {"ui": []}}}}}""".encodeToByteArray(),
                ),
            )
    }

    /** Answers every request 404, as an API that knows no organization of the key does. */
    private object Refusing : HttpExchange {
        override suspend fun send(request: ExchangeRequest): ExchangeResponse =
            ExchangeResponse(
                404,
                mapOf("Content-Type" to "application/json"),
                ExchangeBody("""{"errors":[]}""".encodeToByteArray()),
            )
    }

    @Test
    fun theViewHandsTheCameraAtRestToOnCameraChangeWhoeverMovedIt() {
        val map = engine()
        val rested = mutableListOf<CameraPose>()
        showing(map, mutableListOf(), rested)

        compose.onMain { map.jump(CameraTarget(center = listOf(7.5, 46.0), zoom = 13.0, bearing = 0.0, pitch = 0.0)) }
        compose.until { rested.any { abs(it.zoom - 13) < 0.01 } }
        val before = compose.onMain { rested.size }
        compose.onRoot().performTouchInput { swipeLeft() }
        compose.until { rested.size > before && map.state.value.camera == rested.last() }
    }

    /** A double tap zooms, which MapLibre reads before a single tap is confirmed, so it is no click. */
    @Test
    fun theViewHandsAClickToOnClickAndADoubleTapToNoHandler() {
        val map = engine()
        val taps = mutableListOf<MapTap>()
        val maplibre = showing(map, taps, mutableListOf())

        compose.onRoot().performTouchInput { click(center) }
        compose.until { taps.size == 1 }
        val zoom = compose.onMain { maplibre.cameraPosition.zoom }
        compose.onRoot().performTouchInput { doubleClick(center) }
        compose.until { maplibre.cameraPosition.zoom > zoom + 0.5 }
        compose.onMain {
            assertEquals(1, taps.size, "a double tap is no click")
            assertTrue(taps.single().position[0] in -180.0..180.0)
        }
    }
}
