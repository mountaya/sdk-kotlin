package com.mountaya.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mountaya.client.Credential
import com.mountaya.client.ExchangeBody
import com.mountaya.client.ExchangeRequest
import com.mountaya.client.ExchangeResponse
import com.mountaya.client.HttpExchange
import com.mountaya.client.Mountaya
import com.mountaya.elements.Controls.Drawn
import com.mountaya.elements.Controls.color
import com.mountaya.elements.Controls.image
import com.mountaya.elements.Controls.pixel
import com.mountaya.geo.watermarkClearance
import com.mountaya.geo.watermarkInset
import com.mountaya.geo.watermarkWidth
import kotlinx.coroutines.Dispatchers
import org.junit.Rule
import java.util.Collections
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MapControlsTest {
    @get:Rule
    val compose = createComposeRule()

    private val needle = color("#e9501e")
    private val ink = color("#212529")

    /**
     * [controls] laid over a black square 260 density-independent pixels across, as a map they float over: wide and tall
     * enough that a stack in any corner stands clear of the watermark's frame where it sits.
     */
    @Composable
    private fun Laid(controls: @Composable () -> Unit) {
        Box(Modifier.size(260.dp).background(Color.Black)) { controls() }
    }

    @Test
    fun theStackReadsZoomAboveTheCompassTopRightUnlessAnotherCornerIsNamed() {
        val map = Controls.engine()
        compose.setContent {
            // Laid clear of the window's insets, so the stack's safe area is the whole map it floats over.
            Column(Modifier.safeDrawingPadding()) {
                Drawn(
                    "topRight",
                ) { Laid { MapControls(map, zoom = true, compass = true, layers = false, preferences = false) } }
                Drawn(
                    "bottomLeft",
                ) {
                    Laid {
                        MapControls(
                            map,
                            zoom = true,
                            compass = true,
                            layers = false,
                            preferences = false,
                            corner = MapCorner.BottomLeft,
                        )
                    }
                }
            }
        }

        val topRight = compose.image("topRight")
        val bottomLeft = compose.image("bottomLeft")
        // Top right: the zoom chip 10 dp in from the corner, its plus at its first button's middle, and the compass 8 dp
        // under the zoom chip, its needle up.
        assertTrue(compose.pixel(topRight, 226.0, 34.0).isNear(ink))
        assertTrue(compose.pixel(topRight, 226.0, 130.5).isNear(needle))
        assertTrue(compose.pixel(topRight, 34.0, 34.0).isNear(color("#000000")))
        // Bottom left: the same stack, read from the top, in the other corner.
        assertTrue(compose.pixel(bottomLeft, 34.0, 122.0).isNear(ink))
        assertTrue(compose.pixel(bottomLeft, 34.0, 218.5).isNear(needle))
    }

    @Test
    fun theAppsOwnControlsStandBetweenThePanelTogglesAndGeolocate() {
        val map = Controls.engine()
        compose.setContent {
            Box(Modifier.safeDrawingPadding()) {
                Drawn(
                    "stack",
                ) {
                    Laid {
                        MapControls(
                            map,
                            zoom = false,
                            compass = false,
                            layers = false,
                            preferences = false,
                            geolocate = true,
                        ) {
                            Box(Modifier.size(48.dp).background(Color.Red))
                        }
                    }
                }
            }
        }

        val image = compose.image("stack")
        // The app's own control first, the locate control 8 dp under it.
        assertTrue(compose.pixel(image, 226.0, 34.0).isNear(color("#ff0000")))
        assertTrue(compose.pixel(image, 208.0, 72.0).isNear(color("#ffffff")))
    }

    @Test
    fun aDisabledStackShutsEveryControlUnderItTheAppsOwnAmongThem() {
        val map = Controls.engine()
        compose.setContent {
            Box(Modifier.safeDrawingPadding()) {
                Drawn("stack") {
                    Laid {
                        MapControls(
                            map,
                            zoom = true,
                            compass = false,
                            layers = false,
                            preferences = false,
                            enabled = false,
                        ) {
                            DownloadControl(onClick = {})
                        }
                    }
                }
            }
        }

        val image = compose.image("stack")
        assertTrue(compose.pixel(image, 226.0, 34.0).isNear(color("#adb5bd")))
        // The download control 8 dp under the zoom chip, its shaft down the middle of its glyph.
        assertTrue(compose.pixel(image, 226.0, 10.0 + 96 + 8 + 22).isNear(color("#adb5bd")))
    }

    /** With nothing stated, the stack shows each control its fixed default shows, and reads nothing of its organization. */
    @Test
    fun aFlagLeftOutTakesItsFixedDefault() {
        val map = Controls.engine()
        compose.setContent { MapControls(map) }

        compose.onNodeWithContentDescription("Zoom in").assertExists()
        compose.onNodeWithContentDescription("Layers").assertExists()
        compose.onNodeWithContentDescription("Preferences").assertExists()
        compose.onNodeWithContentDescription("Points of interest").assertDoesNotExist()
    }

    /** A flag the app states stands, true or false, over its default. */
    @Test
    fun aFlagTheAppStatesStandsOverItsDefault() {
        val map = Controls.engine()
        compose.setContent { MapControls(map, layers = false, preferences = false, pois = true) }

        compose.onNodeWithContentDescription("Layers").assertDoesNotExist()
        compose.onNodeWithContentDescription("Preferences").assertDoesNotExist()
        compose.onNodeWithContentDescription("Points of interest").assertExists()
    }

    @Test
    fun aToggleOpensItsPanelInPlaceOfAnyOtherWhichTheAppMayHoist() {
        val map = Controls.engine()
        val open = mutableStateOf<MapPanel?>(null)
        compose.setContent { MapControls(map, preferences = true, openPanel = open) }

        compose.onNodeWithContentDescription("Preferences").performClick()
        compose.runOnIdle { assertEquals(MapPanel.Preferences, open.value) }
        compose.onNodeWithText("Imperial").performClick()
        compose.onNodeWithText("Imperial").assertExists()
        compose.runOnIdle { open.value = null }
        compose.onNodeWithText("Imperial").assertDoesNotExist()
    }

    /**
     * The stack hands its own layers panel the time and the pictures the app hands the stack, and offers no time unless the
     * app says so.
     */
    @Test
    fun theStacksLayersPanelOffersTheTimeAndShowsThePicturesTheAppHandsTheStack() {
        val catalogue =
            """{"data": {"elevation": {"id": "e", "tile_size": 512, "url": "https://tiles.test/elevation.json"},
              "itineraries": {"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": false}},
                "content_type": "application/x-protobuf", "id": "i", "locales": {}, "slug": "itineraries", "sources": []},
              "zones": {"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": false}},
                "content_type": "application/x-protobuf", "id": "z", "locales": {}, "slug": "zones", "sources": []},
              "overlays": [{"id": "c", "slug": "conditions", "locales": {}, "children": [
                {"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": true}},
                  "content_type": "application/x-protobuf", "id": "s", "slug": "slopes",
                  "locales": {"en": {"name": "Slope"}}, "sources": []}]}],
              "terrains": [{"id": "t", "slug": "topo", "locales": {"en": {"name": "Topographic"}}, "variants": {
                "light": {"style": {"url": "https://tiles.test/topo/light/style"},
                  "tiles": {"size": 512, "url": "https://tiles.test/topo/light/tiles.json"}},
                "dark": {"style": {"url": "https://tiles.test/topo/dark/style"},
                  "tiles": {"size": 512, "url": "https://tiles.test/topo/dark/tiles.json"}}}}]}}"""
        val serving =
            object : HttpExchange {
                override suspend fun send(request: ExchangeRequest): ExchangeResponse =
                    ExchangeResponse(
                        200,
                        mapOf("Content-Type" to "application/json"),
                        ExchangeBody(catalogue.encodeToByteArray()),
                    )
            }

        fun map(): MapEngine =
            MapEngine(
                Mountaya(
                    Credential.Publishable("pk_test_controls"),
                    exchange = serving,
                    dispatcher = Dispatchers.Unconfined,
                ),
            )
        val pictured = Collections.synchronizedList(mutableListOf<String>())
        val picture = ColorPainter(Color.Red)
        val timed = mutableStateOf<MapPanel?>(MapPanel.Layers)
        val plain = mutableStateOf<MapPanel?>(MapPanel.Layers)
        compose.setContent {
            Column {
                MapControls(
                    map(),
                    layers = true,
                    openPanel = timed,
                    offersTime = true,
                    thumbnail = { choice, slug ->
                        pictured += "${choice.id}:$slug"
                        picture
                    },
                )
                MapControls(map(), layers = true, openPanel = plain)
            }
        }
        compose.waitUntil(5_000L) { compose.onAllNodesWithText("Topographic").fetchSemanticsNodes().size == 2 }

        compose.onAllNodesWithContentDescription("Time").assertCountEquals(1)
        compose.runOnIdle {
            assertTrue("terrain:topo" in pictured && "overlay:slopes" in pictured, "pictures asked: $pictured")
        }
    }

    @Test
    fun aStackInTheTopLeftCornerStartsBelowTheWatermarksFrame() {
        val map = Controls.engine()
        compose.setContent {
            Box(Modifier.safeDrawingPadding()) {
                Box(Modifier.size(400.dp).testTag("map")) {
                    MapControls(map, compass = false, layers = false, preferences = false, corner = MapCorner.TopLeft)
                }
            }
        }

        val top =
            compose
                .onNodeWithTag("map")
                .fetchSemanticsNode()
                .positionOnScreen.y
        val zoom =
            compose
                .onNodeWithContentDescription("Zoom in")
                .fetchSemanticsNode()
                .positionOnScreen.y
        // The frame's bottom edge, -4 plus its 75.65 of height, then the stack's own 10 of padding.
        val below = with(compose.density) { (watermarkClearance + 10.0).dp.toPx() }
        assertTrue(zoom - top >= below - 1f, "the stack starts below the mark's frame, at ${zoom - top} of $below")
    }

    /** A stack driving no map holds the frame as a waiting read does, at the top left of the space it is laid in. */
    @Test
    fun aStackDrivingNoMapInTheTopLeftCornerStartsBelowTheWatermarksFrame() {
        compose.setContent {
            Box(Modifier.safeDrawingPadding()) {
                Box(Modifier.size(400.dp).testTag("map")) {
                    MapControls(null, compass = false, layers = false, preferences = false, corner = MapCorner.TopLeft)
                }
            }
        }

        val top =
            compose
                .onNodeWithTag("map")
                .fetchSemanticsNode()
                .positionOnScreen.y
        val zoom =
            compose
                .onNodeWithContentDescription("Zoom in")
                .fetchSemanticsNode()
                .positionOnScreen.y
        val below = with(compose.density) { (watermarkClearance + 10.0).dp.toPx() }
        assertTrue(zoom - top >= below - 1f, "the stack starts below the mark's frame, at ${zoom - top} of $below")
    }

    /**
     * A stack laid over more than its map, the map standing below a header of the app's, reads the map's real frame off
     * its engine, where the view drawing it publishes it, and starts below the frame measured from the map's safe area.
     */
    @Test
    fun aStackLaidOverMoreThanItsMapKeepsClearOfTheFrameOfTheMapsRealFrame() {
        val map = Controls.engine()
        compose.setContent {
            Box(Modifier.safeDrawingPadding()) {
                Box(Modifier.width(390.dp).height(700.dp).testTag("space")) {
                    MapControls(map, compass = false, layers = false, preferences = false, corner = MapCorner.TopLeft)
                }
            }
        }
        val space = compose.onNodeWithTag("space").fetchSemanticsNode().boundsInWindow
        val header = 100.0
        compose.runOnIdle {
            val density = compose.density.density.toDouble()
            val left = space.left / density
            val top = space.top / density + header
            val drawn = listOf(left, top, left + 390.0, top + 600.0)
            map.shown(ShownMap(drawn, drawn))
        }
        compose.waitForIdle()

        val zoom =
            compose
                .onNodeWithContentDescription("Zoom in")
                .fetchSemanticsNode()
                .boundsInWindow
        val below = with(compose.density) { (header + watermarkClearance + 10.0).dp.toPx() }
        assertTrue(
            zoom.top - space.top >= below - 1f,
            "the stack starts below the frame of the map below the header, at ${zoom.top - space.top} of $below",
        )
    }

    /**
     * In a layout that runs right to left the mark stays at the map's top left, and the stack, top right as the reader
     * sees it, stands clear of its frame.
     */
    @Test
    fun inALayoutThatRunsRightToLeftTheDefaultStackStandsClearOfTheFrameAtTheTopLeft() {
        val map = Controls.engine()
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Box(Modifier.safeDrawingPadding()) {
                    Box(Modifier.width(390.dp).height(600.dp).testTag("map")) {
                        MapControls(map, compass = false, layers = false, preferences = false)
                    }
                }
            }
        }

        val space = compose.onNodeWithTag("map").fetchSemanticsNode().boundsInWindow
        val zoom =
            compose
                .onNodeWithContentDescription("Zoom in")
                .fetchSemanticsNode()
                .boundsInWindow
        val frameRight = with(compose.density) { (watermarkInset + watermarkWidth).dp.toPx() }
        val frameBottom = with(compose.density) { watermarkClearance.dp.toPx() }
        assertTrue(zoom.right > space.right - space.width / 2, "the stack stands at the reader's right")
        assertTrue(
            zoom.left - space.left >= frameRight || zoom.top - space.top >= frameBottom,
            "the stack, at ${zoom.left - space.left} across, never reaches over the frame ending at $frameRight",
        )
    }

    @Test
    fun aStackInTheTopRightCornerOfANarrowMapStandsBelowTheWatermarksFrame() {
        val map = Controls.engine()
        compose.setContent {
            Box(Modifier.safeDrawingPadding()) {
                Box(Modifier.size(200.dp).testTag("map")) {
                    MapControls(map, compass = false, layers = false, preferences = false)
                }
            }
        }

        val top =
            compose
                .onNodeWithTag("map")
                .fetchSemanticsNode()
                .positionOnScreen.y
        val zoom =
            compose
                .onNodeWithContentDescription("Zoom in")
                .fetchSemanticsNode()
                .positionOnScreen.y
        val below = with(compose.density) { (watermarkClearance + 10.0).dp.toPx() }
        assertTrue(zoom - top >= below - 1f, "the stack starts below the mark's frame, at ${zoom - top} of $below")
    }

    @Test
    fun aPanelOpenedOverTheWatermarksFrameOnAPhoneOpensBelowIt() {
        val map = Controls.engine()
        compose.setContent {
            Box(Modifier.safeDrawingPadding()) {
                Box(Modifier.width(390.dp).height(640.dp).testTag("map")) {
                    MapControls(map, zoom = false, compass = false, layers = false, preferences = true)
                }
            }
        }
        compose.onNodeWithContentDescription("Preferences").performClick()

        val top =
            compose
                .onNodeWithTag("map")
                .fetchSemanticsNode()
                .positionOnScreen.y
        val title =
            compose
                .onNode(hasText("Preferences") and isHeading())
                .fetchSemanticsNode()
                .positionOnScreen.y
        val below = with(compose.density) { watermarkClearance.dp.toPx() }
        assertTrue(title - top >= below - 1f, "the panel opens below the mark's frame, at ${title - top} of $below")
    }
}
