package com.mountaya.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import com.mountaya.client.generated.TileSchema
import com.mountaya.elements.Controls.Drawn
import com.mountaya.elements.Controls.color
import com.mountaya.elements.Controls.image
import com.mountaya.elements.Controls.pixel
import com.mountaya.geo.LayersPanelChoice
import com.mountaya.geo.LayersPanelPress
import com.mountaya.geo.LayersSelectionEngine
import com.mountaya.geo.LayersSelectionTarget
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Rule
import java.util.Collections
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/**
 * The redraws a redraw of the panel may cost of a card or a switch it changed nothing of: none. Each is a part of its
 * own, handed equal values and pressed through a handler that stays, and a panel redraws for a press on another and for
 * its host's own redraw (`panel-rows` in the contract's `sdk/hot-paths.yaml`).
 */
private const val PANEL_ROWS_BUDGET = 0

@OptIn(ExperimentalCoroutinesApi::class)
class LayersPanelTest {
    @get:Rule
    val compose = createComposeRule()

    /** A clock that reads [now] on every read. */
    private class Fixed(
        private val now: Instant,
    ) : Clock {
        override fun now(): Instant = now
    }

    /** A map a selection drives, which answers every command at once and records it. */
    private class Answering : LayersSelectionTarget {
        val calls: MutableList<String> = Collections.synchronizedList(mutableListOf())
        override val terrain: String = "topo"

        override suspend fun loadLayers(): TileSchema.Layers =
            Json.decodeFromString(TileSchema.Layers.serializer(), CATALOGUE)

        override suspend fun setTerrain(slug: String?) {
            calls += "terrain:${slug ?: "-"}"
        }

        override suspend fun setOverlay(slug: String) {
            calls += "overlay:$slug"
        }

        override suspend fun clearOverlay() {
            calls += "overlay:-"
        }

        override suspend fun addEnrichment(slug: String) {
            calls += "add:$slug"
        }

        override suspend fun removeEnrichment(slug: String) {
            calls += "remove:$slug"
        }

        override suspend fun setInstant(instant: Instant?) {
            calls += "instant:${instant ?: "-"}"
        }

        private companion object {
            fun overlay(
                slug: String,
                name: String,
            ): String =
                """{"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": false}},
                   "content_type": "application/x-protobuf", "id": "$slug", "slug": "$slug",
                   "locales": {"en": {"name": "$name"}}, "sources": []}"""

            val CATALOGUE = """{"elevation": {"id": "e", "tile_size": 512, "url": "https://tiles.test/elevation.json"},
              "itineraries": ${overlay("itineraries", "Itineraries")},
              "zones": ${overlay("zones", "Zones")},
              "overlays": [
                {"id": "c", "slug": "conditions", "locales": {"en": {"name": "Conditions"}},
                  "children": [${overlay("slopes", "Slope")}]},
                {"id": "x", "slug": "enrichment", "locales": {},
                  "children": [${overlay("pois", "Points of interest")}, ${overlay("hillshade", "Hillshade")}]},
                {"id": "h", "slug": "hazards", "locales": {"en": {"name": "Hazards"}},
                  "children": [${overlay("avalanche", "Avalanche risk")}]}],
              "terrains": [
                {"id": "t", "slug": "topo", "locales": {"en": {"name": "Topographic"}}, "variants": {
                  "light": {"style": {"url": "https://tiles.test/topo/light/style"},
                    "tiles": {"size": 512, "url": "https://tiles.test/topo/light/tiles.json"}},
                  "dark": {"style": {"url": "https://tiles.test/topo/dark/style"},
                    "tiles": {"size": 512, "url": "https://tiles.test/topo/dark/tiles.json"}}}}]}"""
        }
    }

    @Test
    fun aPressAsksTheSelectionsCommandForItsRowWhichDrivesTheMap() =
        runTest {
            val map = Answering()
            val selection = LayersSelectionEngine(backgroundScope, map)
            val locked = mutableListOf<String>()
            runCurrent()

            layersPress(LayersPanelPress.Command(LayersPanelChoice.Overlay, "slopes"), selection, null)
            layersPress(LayersPanelPress.Command(LayersPanelChoice.Enrichment, "hillshade"), selection, null)
            layersPress(LayersPanelPress.Command(LayersPanelChoice.Terrain, "topo"), selection, null)
            layersPress(LayersPanelPress.Locked("snow-depth"), selection) { locked += it }
            runCurrent()

            assertEquals("slopes", selection.state.value.overlay)
            assertEquals(listOf("hillshade"), selection.state.value.enrichments)
            assertEquals(listOf("overlay:slopes", "add:hillshade", "terrain:topo"), map.calls)
            assertEquals(listOf("snow-depth"), locked, "a locked entry selects nothing, its press handed on")
            selection.close()
        }

    @Test
    fun theSectionsStandInTheStudiosOrderThePointsOfInterestNoSwitchAndTheTimeLastWhereOffered() {
        val map = Answering()
        compose.setContent {
            val scope = rememberCoroutineScope()
            val selection = remember { LayersSelectionEngine(scope, map) }
            CompositionLocalProvider(LocalMountayaClock provides Fixed(Instant.parse("2026-03-14T09:26:53.900Z"))) {
                LayersPanel(selection, offersTime = true)
            }
        }
        compose.waitUntil(TIMEOUT) { compose.onAllNodesWithText("Slope").fetchSemanticsNodes().isNotEmpty() }

        val tops =
            listOf(
                "Base map",
                "Enrichment",
                "Overlays",
                "Time",
            ).map { compose.onNodeWithText(it).getBoundsInRoot().top }
        assertEquals(tops.sorted(), tops, "the terrains, the enrichments, the overlays, then the time")
        compose.onAllNodesWithText("Points of interest").assertCountEquals(0)
        compose.onAllNodesWithText("Hazards").assertCountEquals(1)
        assertTrue(
            compose
                .onNodeWithContentDescription("Time")
                .fetchSemanticsNode()
                .config[SemanticsProperties.StateDescription]
                .endsWith(" · Now"),
            "the thumb stands at now with none pinned",
        )
    }

    @Test
    fun aPickInTheCategorySwitchShowsItsOverlaysForAsLongAsThePanelLives() {
        val map = Answering()
        compose.setContent {
            val scope = rememberCoroutineScope()
            val selection = remember { LayersSelectionEngine(scope, map) }
            LayersPanel(selection)
        }
        compose.waitUntil(TIMEOUT) { compose.onAllNodesWithText("Slope").fetchSemanticsNodes().isNotEmpty() }

        compose.onNodeWithText("Hazards").performClick()
        compose.waitUntil(TIMEOUT) { compose.onAllNodesWithText("Avalanche risk").fetchSemanticsNodes().isNotEmpty() }
        compose.onAllNodesWithText("Slope").assertCountEquals(0)
        compose.onNodeWithText("Avalanche risk").performClick()
        compose.waitUntil(TIMEOUT) { map.calls.contains("overlay:avalanche") }
        assertTrue(map.calls.none { it.startsWith("overlay:slopes") }, "a pick in the switch selects no overlay")
    }

    @Test
    fun aReleaseOnTheSliderPinsItsStopsInstantAndOnNowPinsNone() {
        val map = Answering()
        val now = Instant.parse("2026-03-14T09:26:53.900Z")
        compose.setContent {
            val scope = rememberCoroutineScope()
            val selection = remember { LayersSelectionEngine(scope, map) }
            CompositionLocalProvider(
                LocalMountayaClock provides Fixed(now),
            ) { LayersPanel(selection, offersTime = true) }
        }
        compose.waitUntil(TIMEOUT) { compose.onAllNodesWithText("Slope").fetchSemanticsNodes().isNotEmpty() }
        val slider = compose.onNodeWithContentDescription("Time")
        slider.assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, rangeAt(71f)))

        slider.performSemanticsAction(SemanticsActions.SetProgress) { it(77f) }
        compose.waitUntil(TIMEOUT) { map.calls.contains("instant:${now + 6.hours}") }
        slider.assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, rangeAt(77f)))
        assertTrue(
            compose
                .onNodeWithContentDescription("Time")
                .fetchSemanticsNode()
                .config[SemanticsProperties.StateDescription]
                .endsWith(" · in 6 hours"),
        )
        slider.performSemanticsAction(SemanticsActions.SetProgress) { it(71f) }
        compose.waitUntil(TIMEOUT) { map.calls.contains("instant:-") }
    }

    @Test
    fun aClockPastTheRangeOfATrackHoldsTheSliderUnderItsSentence() {
        val map = Answering()
        compose.setContent {
            val scope = rememberCoroutineScope()
            val selection = remember { LayersSelectionEngine(scope, map) }
            CompositionLocalProvider(
                LocalMountayaClock provides Fixed(Instant.fromEpochMilliseconds(8_640_000_000_000_000)),
            ) {
                LayersPanel(selection, offersTime = true)
            }
        }
        compose.waitUntil(TIMEOUT) { compose.onAllNodesWithText("Slope").fetchSemanticsNodes().isNotEmpty() }

        compose.onNodeWithText("The current time could not be read, so the time can't be changed.").assertExists()
        compose.onNodeWithContentDescription("Time").assertIsNotEnabled()
        assertTrue(map.calls.none { it.startsWith("instant:") })
    }

    @Test
    fun thePanelDrawsTheSelectionsCatalogueOnThePanelsGroundAndAPressDrivesIt() {
        val map = Answering()
        compose.setContent {
            val scope = rememberCoroutineScope()
            val selection = remember { LayersSelectionEngine(scope, map) }
            Column {
                Drawn("light") { LayersPanel(selection) }
                Drawn("dark", dark = true) { LayersPanel(selection) }
            }
        }
        compose.waitUntil(TIMEOUT) { compose.onAllNodesWithText("Slope").fetchSemanticsNodes().size == 2 }

        val bounds = compose.onNodeWithTag("light").getBoundsInRoot()
        // Laid out in whole pixels: at a density that makes 340 no whole number of pixels, the panel reads up to one
        // pixel wider.
        val width = (bounds.right - bounds.left).value
        assertTrue(width >= 240f, "the panel is $width wide")
        assertTrue(width <= 340f + 1f / compose.density.density, "the panel is $width wide")
        assertTrue(compose.pixel(compose.image("light"), 4.0, 4.0).isNear(color("#ffffff")))
        assertTrue(compose.pixel(compose.image("dark"), 4.0, 4.0).isNear(color("#25262b")))
        compose.onAllNodesWithText("Slope")[0].performClick()
        compose.waitUntil(TIMEOUT) { map.calls.contains("overlay:slopes") }
    }

    @Test
    fun hotPathBudgetPanelRowsAHostsRedrawsAndAPressOnARowRedrawNoRowTheyDidNotChange() {
        val map = Answering()
        val probe = RenderProbe()
        var redraws by mutableStateOf(0)
        val locked = mutableListOf<String>()
        compose.setContent {
            val scope = rememberCoroutineScope()
            val selection = remember { LayersSelectionEngine(scope, map) }
            // An app's own view holding the panel, which draws it again each time it draws, with handlers of its own made
            // anew each time, as an app's lambdas are.
            val drawn = redraws
            CompositionLocalProvider(LocalMountayaRenderProbe provides probe) {
                LayersPanel(selection, isLocked = { drawn < 0 }, onLockedClick = { locked += "$drawn:$it" })
            }
        }
        val rows =
            listOf("LayersCard Topographic", "LayersCard Slope", "LayersSwitch Hillshade", "LayersSegment Hazards")
        compose.waitUntil(TIMEOUT) { compose.runOnUiThread { rows.all { probe[it] >= 1 } } }
        compose.waitForIdle()
        val before = compose.runOnIdle { probe.counts }

        // Twenty redraws of the host, each handing the panel handlers made anew.
        for (redraw in 1..20) {
            val panel = compose.runOnIdle { probe["LayersPanel"] }
            redraws = redraw
            compose.waitUntil(TIMEOUT) { compose.runOnUiThread { probe["LayersPanel"] > panel } }
        }
        // A press on the slope's card, which selects it and changes that card alone.
        val slope = compose.runOnIdle { probe["LayersCard Slope"] }
        compose.onNodeWithText("Slope").performClick()
        compose.waitUntil(TIMEOUT) { map.calls.contains("overlay:slopes") }
        compose.waitUntil(TIMEOUT) { compose.runOnUiThread { probe["LayersCard Slope"] > slope } }
        compose.waitForIdle()
        val after = compose.runOnIdle { probe.counts }

        fun since(part: String): Int = (after[part] ?: 0) - (before[part] ?: 0)
        // The work happened: the panel was drawn for each redraw of its host, and the card pressed was drawn again.
        assertTrue(since("LayersPanel") >= 20, "the panel drawn ${since("LayersPanel")} times")
        assertTrue(since("LayersCard Slope") >= 1)
        assertTrue(locked.isEmpty(), "nothing is locked")
        // The cards, switches and segments nothing changed were drawn again by none of it.
        val unchanged =
            listOf("LayersCard Topographic", "LayersSwitch Hillshade", "LayersSegment Hazards").associateWith(::since)
        assertTrue(unchanged.values.sum() <= PANEL_ROWS_BUDGET, "rows drawn again: $unchanged")
    }

    /**
     * A panel's own strings stand over the scope's words for it alone, the heading of the base maps among them, and the
     * panel beside it, handed none, reads the scope's.
     */
    @Test
    fun aPanelsOwnStringsStandOverItsHeadingsForItAlone() {
        val map = Answering()
        compose.setContent {
            val scope = rememberCoroutineScope()
            val selection = remember { LayersSelectionEngine(scope, map) }
            Column {
                LayersPanel(selection, strings = mapOf("layers.terrain" to "Backgrounds"))
                LayersPanel(selection)
            }
        }
        compose.waitUntil(TIMEOUT) { compose.onAllNodesWithText("Slope").fetchSemanticsNodes().size == 2 }

        compose.onAllNodesWithText("Backgrounds").assertCountEquals(1)
        compose.onAllNodesWithText("Base map").assertCountEquals(1)
    }

    /** The range a slider over the forecast window states with its thumb at [stop]. */
    private fun rangeAt(stop: Float) =
        androidx.compose.ui.semantics
            .ProgressBarRangeInfo(stop, 0f..215f, steps = 214)

    private companion object {
        /** How long a test waits for the selection, whose commands run on the main thread. */
        const val TIMEOUT = 5_000L
    }
}
