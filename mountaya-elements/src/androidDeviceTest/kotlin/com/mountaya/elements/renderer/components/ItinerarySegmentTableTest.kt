package com.mountaya.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.unit.dp
import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.generated.RestSchema
import com.mountaya.client.generated.TileSchema
import com.mountaya.elements.Controls.Drawn
import com.mountaya.geo.AltitudeRange
import com.mountaya.geo.ItineraryResource
import com.mountaya.geo.ItineraryResourceEngine
import com.mountaya.geo.LayerRank
import com.mountaya.geo.ProfileRange
import com.mountaya.geo.SegmentRowCell
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.lineBetween
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ItinerarySegmentTableTest {
    @get:Rule
    val compose = createComposeRule()

    private val english = UnitPreferences("en")
    private val reads = Itineraries.reads()
    private val drawing = ItineraryDrawing(Itineraries.resolved, null)

    @AfterTest
    fun stopReading() {
        reads.cancel()
    }

    /** Waits until the row of the leg named [name] is drawn. */
    private fun waitForRow(name: String) {
        compose.waitUntil(TIMEOUT) { compose.onAllNodesWithText(name).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test
    fun itReadsItsItineraryThroughTheCacheAndDrawsARowPerLegTheEmptyLineForNone() {
        val cache = Itineraries.cache(reads)
        val bare = Itineraries.cache(reads, Itineraries.bare)
        compose.setContent {
            Column {
                Drawn("table") { ItinerarySegmentTable("itin_test", cache, Modifier.width(390.dp)) }
                Drawn("empty") { ItinerarySegmentTable("itin_test", bare, Modifier.width(390.dp)) }
            }
        }
        waitForRow("Descent")
        compose.waitUntil(TIMEOUT) { compose.onAllNodesWithText(EMPTY).fetchSemanticsNodes().isNotEmpty() }

        val table = compose.onNodeWithTag("table").getBoundsInRoot()
        assertTrue(table.bottom - table.top >= (2 * 48 + 20).dp, "$table")
        val empty = compose.onNodeWithTag("empty").getBoundsInRoot()
        assertTrue(empty.bottom - empty.top < 48.dp, "the empty line alone: $empty")
    }

    @Test
    fun aCellWritesItsFigureOverTheRunningOneInTheReadersUnits() {
        val hiking = RestSchema.TrailDifficultyType.Hiking

        assertEquals(listOf("Climb"), segmentCellText(SegmentRowCell.Segment("Climb"), english))
        assertEquals(listOf("1.1 km", "2.2 km"), segmentCellText(SegmentRowCell.Distance(1_112.0, 2_224.0), english))
        assertEquals(listOf("1.1 km"), segmentCellText(SegmentRowCell.Distance(1_112.0, null), english))
        assertEquals(
            listOf("+400 m -150 m", "+650 m"),
            segmentCellText(SegmentRowCell.Elevation(400.0, 150.0, 650.0), english),
        )
        assertEquals(
            listOf("1,000\u20131,400 m"),
            segmentCellText(SegmentRowCell.Altitude(AltitudeRange(1_000.0, 1_400.0), null), english),
        )
        assertEquals(listOf("1,000 m"), segmentCellText(SegmentRowCell.Altitude(null, 1_000.0), english))
        assertEquals(listOf("hiking"), segmentCellText(SegmentRowCell.Difficulty(hiking), english))
        assertTrue(
            segmentCellText(SegmentRowCell.Profile(ProfileRange(0.0, 1.0), null), english).isEmpty(),
            "a sparkline writes nothing",
        )
    }

    @Test
    fun aRowPressedIsSelectedAndLitOnTheMapItsIdHandedOnASecondPressLettingGo() {
        val map = Controls.engine()
        val cache = Itineraries.cache(reads)
        val pressed = mutableListOf<String>()
        val points = drawing.series.points
        val climb = drawing.rows[0]
        compose.setContent {
            ItinerarySegmentTable(
                "itin_test",
                cache,
                Modifier.width(390.dp),
                map = map,
                units = english,
                onSegmentClick = { pressed += it },
            )
        }
        waitForRow("Climb")

        compose
            .onNodeWithText("Climb")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assertIsNotSelected()
            .performClick()
        compose.onNodeWithText("Climb").assertIsSelected()
        compose.runOnIdle {
            val marker =
                map.state.value.markers
                    .single()
            assertEquals(SEGMENT_TABLE_CURSOR_MARKER_ID, marker.id)
            assertEquals(listOf(6.0, 45.01), marker.position, "the leg's end")
            assertEquals(lineBetween(points, climb.from, climb.to), map.geometry(GeometrySlot.Emphasis).geometry)
            assertEquals(listOf("climb"), pressed)
        }

        compose.onNodeWithText("Climb").performClick()
        compose.onNodeWithText("Climb").assertIsNotSelected()
        compose.runOnIdle {
            assertTrue(
                map.state.value.markers
                    .isEmpty(),
            )
            assertNull(map.geometry(GeometrySlot.Emphasis).geometry)
            assertEquals(listOf("climb", "climb"), pressed)
        }
    }

    @Test
    fun theHeaderIsReadAsAHeadingAndTheRowTheAppHighlightsAsSelected() {
        val cache = Itineraries.cache(reads)
        compose.setContent {
            ItinerarySegmentTable("itin_test", cache, Modifier.width(390.dp), highlighted = "descent")
        }
        waitForRow("Descent")

        compose.onNodeWithContentDescription("Itinerary table").assertExists()
        compose.onNodeWithText("Segment").onParent().assert(isHeading())
        compose.onNodeWithText("Descent").assertIsSelected()
        compose.onNodeWithText("Climb").assertIsNotSelected()
    }

    @Test
    fun aHoveringPointerLightsItsRowOnTheMapAndLeavingTakesItDown() {
        val map = Controls.engine()
        val cache = Itineraries.cache(reads)
        val points = drawing.series.points
        val descent = drawing.rows[1]
        compose.setContent { ItinerarySegmentTable("itin_test", cache, Modifier.width(390.dp), map = map) }
        waitForRow("Descent")

        compose.onNodeWithText("Descent").performMouseInput { enter(center) }
        compose.onNodeWithText("Descent").assertIsSelected()
        compose.runOnIdle {
            assertEquals(
                listOf(6.0, 45.02),
                map.state.value.markers
                    .single()
                    .position,
                "the leg's end",
            )
            assertEquals(lineBetween(points, descent.from, descent.to), map.geometry(GeometrySlot.Emphasis).geometry)
        }

        compose.onNodeWithText("Descent").performMouseInput { exit(Offset(-1f, -1f)) }
        compose.onNodeWithText("Descent").assertIsNotSelected()
        compose.runOnIdle {
            assertTrue(
                map.state.value.markers
                    .isEmpty(),
            )
            assertNull(map.geometry(GeometrySlot.Emphasis).geometry)
        }
    }

    /**
     * A leg the map's own cursor is over lights its row, and pins nothing; the row the app names is lit in place of it
     * (`map.profile.table-rows`).
     */
    @Test
    fun aLegHoveredOnTheMapLightsItsRowUnlessTheAppNamesAnother() {
        val map = Controls.engine()
        val surface = ScriptedMapSurface(map)
        val cache = Itineraries.cache(reads)
        var highlighted by mutableStateOf<String?>(null)
        compose.runOnIdle {
            map.addLayer(
                MapLayer("trail-line", TileSchema.TileLayerType.Line, "trail", rank = LayerRank.ItineraryLine.rank),
            )
            surface.rendered =
                listOf(
                    QueriedFeature(
                        mapOf("segment_id" to JsonPrimitive("descent")),
                        GeoJsonPoint(listOf(6.0, 45.015)),
                        "trail-line",
                    ),
                )
        }
        compose.setContent {
            ItinerarySegmentTable("itin_test", cache, Modifier.width(390.dp), map = map, highlighted = highlighted)
        }
        waitForRow("Descent")

        compose.runOnIdle {
            surface.hover(listOf(10.0, 10.0), listOf(6.0, 45.015))
            surface.drawFrame()
        }
        compose.onNodeWithText("Descent").assertIsSelected()
        compose.onNodeWithText("Climb").assertIsNotSelected()
        compose.runOnIdle {
            assertTrue(
                map.state.value.markers
                    .isEmpty(),
                "the map's cursor pins nothing",
            )
        }

        highlighted = "climb"
        compose.onNodeWithText("Climb").assertIsSelected()
        compose.onNodeWithText("Descent").assertIsNotSelected()

        highlighted = null
        compose.runOnIdle { surface.leave(listOf(-1.0, -1.0), listOf(6.0, 45.015)) }
        compose.onNodeWithText("Descent").assertIsNotSelected()
    }

    /**
     * Handed the app's engine, the table reads its state and never configures or closes it: the app chooses the
     * itinerary, and the engine still answers the question the app asks after the table left.
     */
    @Test
    fun aTableOverTheAppsEngineReadsItsItineraryAndNeverClosesIt() {
        val cache = Itineraries.cache(reads)
        val bare = Itineraries.cache(reads, Itineraries.bare)
        val engine = compose.runOnUiThread { ItineraryResourceEngine(cache, itineraryId = "itin_test") }
        var isPresent by mutableStateOf(true)
        compose.setContent { if (isPresent) ItinerarySegmentTable(engine, Modifier.width(390.dp)) }
        waitForRow("Descent")

        compose.runOnUiThread { isPresent = false }
        compose.waitForIdle()
        compose.runOnUiThread { engine.configure(bare, "itin_test") }
        compose.waitUntil(TIMEOUT) {
            compose.runOnUiThread {
                (engine.state.value as? ItineraryResource.Resolved)?.itinerary?.legs?.isEmpty() == true
            }
        }
        compose.runOnUiThread { engine.close() }
    }

    /** A table's own strings stand over the scope's words for it alone, its empty line among them. */
    @Test
    fun aTablesOwnStringsStandOverTheScopesWordsForItAlone() {
        val bare = Itineraries.cache(reads, Itineraries.bare)
        compose.setContent {
            Column {
                ItinerarySegmentTable(
                    "itin_test",
                    bare,
                    Modifier.width(390.dp),
                    strings = mapOf("table.empty" to "No stage to state."),
                )
                ItinerarySegmentTable("itin_test", bare, Modifier.width(390.dp))
            }
        }
        compose.waitUntil(TIMEOUT) {
            compose.onAllNodesWithText("No stage to state.").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onAllNodesWithText(EMPTY).assertCountEquals(1)
    }

    private companion object {
        /** How long a test waits for a table, whose itinerary is read on the main thread. */
        const val TIMEOUT = 5_000L

        /** What a table of no leg states. */
        const val EMPTY = "This itinerary has no segments to state."
    }
}
