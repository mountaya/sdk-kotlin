package com.mountaya.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.PoiPanelGroup
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The redraws a redraw of the panel may cost of a row it changed nothing of: none. Each row is a part of its own, handed
 * equal values and pressed through handlers that stay, and a panel redraws for a zoom note, a sibling's press and its
 * host's own redraw (`panel-rows` in the contract's `sdk/hot-paths.yaml`).
 */
private const val PANEL_ROWS_BUDGET = 0

/**
 * The redraws of the panel a camera driven frame by frame may cost, for each time the comparison of its zoom with the
 * note's floor flips: one, the note's. The comparison is read on every frame, and a redraw of the panel on every frame
 * redraws its rows with it (`zoom-threshold`).
 */
private const val ZOOM_THRESHOLD_BUDGET = 1

class PoiPanelTest {
    @get:Rule
    val compose = createComposeRule()

    /** A specification drawing two concepts at the points of interest's rank, `parking` and `poi_food`, and one at an itinerary's. */
    private val pointsOfInterest =
        TileSchema.TileSpecification(
            sources =
                listOf(
                    TileSchema.TileSource(
                        id = "pois",
                        layers =
                            listOf(
                                layer("parking", "parking", TileSchema.TileLayerType.Circle),
                                layer("food", "poi_food", TileSchema.TileLayerType.Circle),
                                layer(
                                    "lines",
                                    "itinerary_lines",
                                    TileSchema.TileLayerType.Line,
                                    key = "lines",
                                ),
                            ),
                        tiles = listOf("https://tiles.test/pois/{z}/{x}/{y}"),
                        type = TileSchema.TileSourceType.Vector,
                        zoom_max = 14,
                        zoom_min = 0,
                    ),
                ),
        )

    private fun layer(
        id: String,
        sourceLayer: String,
        type: TileSchema.TileLayerType,
        key: String = sourceLayer,
    ): TileSchema.TileLayer =
        TileSchema.TileLayer(
            id = id,
            key = key,
            layout = null,
            paint = null,
            source_layer = sourceLayer,
            type = type,
            zoom_max = 22,
            zoom_min = 0,
        )

    /** A map drawing the points-of-interest specification. */
    private fun drawing(): MapEngine = Controls.engine().also { it.setSpecification(pointsOfInterest, "pois") }

    @Test
    fun aRowPerConceptTheMapDrawsAtThePointsOfInterestsRankOnWhileShown() {
        val map = drawing()
        compose.runOnUiThread { map.setKeyVisibility("parking", false) }

        val offer = compose.runOnUiThread { poiOffer(map, null, null, null, emptyMap()) }
        assertEquals(listOf("parking", "poi_food"), offer.rows.map { it.id })
        assertEquals(listOf(false, true), offer.rows.map { it.isVisible })
        assertTrue(offer.isAnyVisible)
        assertTrue(poiOffer(null, null, null, null, emptyMap()).rows.isEmpty(), "a map drawing nothing offers no row")
    }

    @Test
    fun aPressShowsOrHidesTheRowsKeysOnTheMapUnlessAHandlerStandsInForIt() {
        val map = drawing()
        compose.runOnUiThread {
            val parking = poiOffer(map, null, null, null, emptyMap()).rows.first()
            poiToggle(parking, false, map, isGroup = false, onToggle = null, onToggleGroup = null)
            assertFalse(map.isKeyVisible("parking"))
            val handed = mutableListOf<String>()
            poiToggle(parking, true, map, isGroup = false, onToggle = {
                key,
                visible,
                ->
                handed += "$key:$visible"
            }, onToggleGroup = null)
            assertEquals(listOf("parking:true"), handed)
            assertFalse(map.isKeyVisible("parking"), "a handler stands in for the map")
            val eat =
                poiOffer(
                    map,
                    null,
                    listOf(PoiPanelGroup("eat", "Eat", listOf("poi_food", "cafe"))),
                    null,
                    emptyMap(),
                ).rows.first()
            assertEquals(listOf("poi_food"), eat.keys)
            poiToggle(eat, false, map, isGroup = true, onToggle = null, onToggleGroup = { id, visible ->
                handed +=
                    "group $id:$visible"
            })
            assertEquals("group eat:false", handed.last())
        }
    }

    @Test
    fun theRowOverThemAllShowsOrHidesEveryRowsKeysUnlessAHandlerStandsInForIt() {
        val map = drawing()
        compose.runOnUiThread {
            val rows = poiOffer(map, null, null, null, emptyMap()).rows
            poiToggleAll(false, rows, map, null)
            assertTrue(!map.isKeyVisible("parking") && !map.isKeyVisible("poi_food"))
            assertFalse(poiOffer(map, null, null, null, emptyMap()).isAnyVisible)
            val all = mutableListOf<Boolean>()
            poiToggleAll(true, rows, map) { all += it }
            assertEquals(listOf(true), all)
            assertFalse(map.isKeyVisible("parking"))
        }
    }

    @Test
    fun theNoteStandsBelowTheOverlaysFloorZoomNeverAtItAndNeverWithNoFloor() {
        assertTrue(isPoiNoted(11.9, 12.0))
        assertFalse(isPoiNoted(12.0, 12.0))
        assertFalse(isPoiNoted(3.0, Double.NaN))
        assertFalse(isPoiNoted(3.0, null))
        assertFalse(isPoiNoted(null, 12.0))
    }

    @Test
    fun hotPathBudgetPanelRowsAHostsRedrawsASiblingsPressAndAZoomNoteRedrawNoRowTheyDidNotChange() {
        val map = drawing()
        val probe = RenderProbe()
        var redraws by mutableStateOf(0)
        val toggledAll = mutableListOf<String>()
        val all = ElementsScope.standard.word("pois.displayOnMap")
        compose.setContent {
            // An app's own view holding the panel, which draws it again each time it draws, with a handler of its own made
            // anew each time, as an app's lambdas are.
            val drawn = redraws
            CompositionLocalProvider(LocalMountayaRenderProbe provides probe) {
                PoiPanel(map, minZoom = 12.0, showsAllToggle = true, onToggleAll = { toggledAll += "$drawn:$it" })
            }
        }
        val rows = listOf("PanelRow parking", "PanelRow poi_food", "PanelRow $all")
        compose.waitUntil(TIMEOUT) { compose.runOnUiThread { rows.all { probe[it] >= 1 } } }
        val before = compose.runOnIdle { probe.counts }

        // Twenty redraws of the host, each handing the panel a handler made anew.
        for (redraw in 1..20) {
            val panel = compose.runOnIdle { probe["PoiPanel"] }
            redraws = redraw
            compose.waitUntil(TIMEOUT) { compose.runOnUiThread { probe["PoiPanel"] > panel } }
        }
        // A press on the parking row, which hides its concept and changes that row alone.
        val parking = compose.runOnIdle { probe["PanelRow parking"] }
        compose.onNodeWithText("parking").performClick()
        compose.waitUntil(TIMEOUT) { compose.runOnUiThread { probe["PanelRow parking"] > parking } }
        // The camera below the overlay's floor, which raises the note over the rows.
        compose.runOnUiThread { map.jump(CameraTarget(zoom = 11.0)) }
        val warning = ElementsScope.standard.word("pois.zoomWarning")
        compose.waitUntil(TIMEOUT) { compose.onAllNodesWithText(warning).fetchSemanticsNodes().isNotEmpty() }
        val after = compose.runOnIdle { probe.counts }

        fun since(part: String): Int = (after[part] ?: 0) - (before[part] ?: 0)
        // The work happened: the panel was drawn for each redraw of its host, the row pressed was drawn again and hides
        // its concept, and the note stands.
        assertTrue(since("PoiPanel") >= 20, "the panel drawn ${since("PoiPanel")} times")
        assertTrue(since("PanelRow parking") >= 1)
        assertFalse(compose.runOnIdle { map.isKeyVisible("parking") })
        // The rows nothing changed were drawn again by none of it.
        val unchanged = listOf("PanelRow poi_food", "PanelRow $all").associateWith(::since)
        assertTrue(unchanged.values.sum() <= PANEL_ROWS_BUDGET, "rows drawn again: $unchanged")
    }

    @Test
    fun hotPathBudgetZoomThresholdSixtyCameraFramesRedrawThePanelOnceAsTheNoteFlips() {
        val map = drawing()
        compose.runOnUiThread { map.jump(CameraTarget(zoom = 13.0)) }
        val probe = RenderProbe()
        compose.setContent {
            CompositionLocalProvider(LocalMountayaRenderProbe provides probe) { PoiPanel(map, minZoom = 12.0) }
        }
        compose.waitUntil(TIMEOUT) { compose.runOnUiThread { probe["PoiPanel"] >= 1 && probe["PoiNote"] >= 1 } }
        val before = compose.runOnIdle { probe.counts }

        // A camera driven frame by frame from zoom 13 down to 11, each frame landing where the app moved it, the note's
        // floor crossed once at 12.
        for (frame in 1..60) {
            compose.runOnUiThread { map.jump(CameraTarget(zoom = 13.0 - 2.0 * frame / 60)) }
            compose.waitForIdle()
        }
        val after = compose.runOnIdle { probe.counts }

        fun since(part: String): Int = (after[part] ?: 0) - (before[part] ?: 0)
        // The work happened: the zoom was compared on every frame, and the note stands.
        assertTrue(compose.runOnIdle { map.state.value.camera.zoom } < 12.0)
        assertTrue(since("PoiNote compared") >= 60, "the zoom compared ${since("PoiNote compared")} times")
        compose.onAllNodesWithText(ElementsScope.standard.word("pois.zoomWarning")).assertCountEquals(1)
        // The panel, its note and its rows were drawn again once, as the comparison flipped.
        val parts = listOf("PoiPanel", "PoiNote", "PanelRow parking", "PanelRow poi_food")
        val redrawn = parts.associateWith(::since)
        val flips = 1
        assertTrue(
            redrawn.values.sum() <= ZOOM_THRESHOLD_BUDGET * flips,
            "drawn again over 60 frames and $flips flip: $redrawn",
        )
    }

    @Test
    fun aMapDrawingNoPointOfInterestShowsNoPanelAtAllAndOneDrawingThemARowToPress() {
        val empty = Controls.engine()
        val map = drawing()
        compose.setContent {
            Column {
                Controls.Drawn("empty") { PoiPanel(empty) }
                Controls.Drawn("drawn") { PoiPanel(map) }
            }
        }

        val emptyBounds = compose.onNodeWithTag("empty").getBoundsInRoot()
        assertEquals(0f, (emptyBounds.bottom - emptyBounds.top).value)
        val drawnBounds = compose.onNodeWithTag("drawn").getBoundsInRoot()
        assertTrue((drawnBounds.bottom - drawnBounds.top).value > 48f)
        compose.onNodeWithText("parking").performClick()
        compose.runOnIdle { assertFalse(map.isKeyVisible("parking")) }
    }

    /** A panel's own strings stand over the scope's words for it alone, the row over every concept among them. */
    @Test
    fun aPanelsOwnStringsStandOverTheScopesWordsForItAlone() {
        val map = drawing()
        compose.setContent {
            Column {
                PoiPanel(map, showsAllToggle = true, strings = mapOf("pois.displayOnMap" to "Show them all"))
                PoiPanel(map, showsAllToggle = true)
            }
        }

        compose.onAllNodesWithText("Show them all").assertCountEquals(1)
        compose.onAllNodesWithText(ElementsScope.standard.word("pois.displayOnMap")).assertCountEquals(1)
    }

    private companion object {
        /** How long a test waits for the panel, which reads its map's state on the main thread. */
        const val TIMEOUT = 5_000L
    }
}
