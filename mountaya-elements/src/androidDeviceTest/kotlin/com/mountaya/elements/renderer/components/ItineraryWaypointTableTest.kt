package com.mountaya.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.mountaya.client.generated.RestSchema
import com.mountaya.elements.Controls.Drawn
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.ItineraryResource
import com.mountaya.geo.ItineraryResourceEngine
import com.mountaya.geo.ProfileRange
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.WaypointField
import com.mountaya.geo.WaypointRowCell
import kotlinx.coroutines.cancel
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ItineraryWaypointTableTest {
    @get:Rule
    val compose = createComposeRule()

    private val english = UnitPreferences("en")
    private val reads = Itineraries.reads()
    private val water = RestSchema.WaypointAmenity.Water
    private val toilets = RestSchema.WaypointAmenity.Toilets

    @AfterTest
    fun stopReading() {
        reads.cancel()
    }

    /** Waits until the row of the place named [name] is drawn. */
    private fun waitForRow(name: String) {
        compose.waitUntil(TIMEOUT) { compose.onAllNodesWithText(name).fetchSemanticsNodes().isNotEmpty() }
    }

    /** The filter's chip of the service named [name], pressed to toggle it. */
    private fun chip(name: String): SemanticsNodeInteraction =
        compose.onNode(hasText(name) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox))

    @Test
    fun itReadsItsItineraryThroughTheCacheAndDrawsTheServicesChipsOverARowAPlace() {
        val cache = Itineraries.cache(reads)
        compose.setContent { Drawn("table") { ItineraryWaypointTable("itin_test", cache, Modifier.width(390.dp)) } }
        waitForRow("Refuge")

        val table = compose.onNodeWithTag("table").getBoundsInRoot()
        assertTrue(table.bottom - table.top >= (48 + 3 * 48).dp, "$table")
        for (name in listOf("Refuge", "Col", "Unnamed waypoint")) compose.onNodeWithText(name).assertExists()
        for (name in listOf("First aid", "Hot meal", "Showers", "Toilets", "Water")) chip(name).assertExists()
    }

    @Test
    fun aChipPressedTogglesItsServiceInTheChoiceTheAppHoldsAtItsEnd() {
        val cache = Itineraries.cache(reads)
        val picked = mutableStateOf<List<RestSchema.WaypointAmenity>>(emptyList())
        compose.setContent { ItineraryWaypointTable("itin_test", cache, Modifier.width(390.dp), picked = picked) }
        waitForRow("Refuge")

        chip("Water").assertIsNotSelected().performClick()
        compose.runOnIdle { assertEquals(listOf(water), picked.value) }
        chip("Water").assertIsSelected()
        compose.onAllNodesWithText("Col").assertCountEquals(0)
        chip("Toilets").performClick()
        compose.runOnIdle { assertEquals(listOf(water, toilets), picked.value) }
        chip("Water").performClick()
        compose.runOnIdle { assertEquals(listOf(toilets), picked.value) }
        chip("Water").assertIsNotSelected()
        chip("Toilets").assertIsSelected()
    }

    @Test
    fun aCellWritesThePlacesFiguresInTheReadersUnitsAndItsServicesByName() {
        val write = { cell: WaypointRowCell -> waypointCellText(cell, english, ElementsScope.standard) }
        val fields = listOf(WaypointField("beds", "Beds", "24"), WaypointField("warden", "Warden", "Anne"))

        assertEquals("Unnamed waypoint", write(WaypointRowCell.Waypoint("pin", null)))
        assertEquals("Refuge", write(WaypointRowCell.Waypoint("pin", "Refuge")))
        assertEquals("0.6 km", write(WaypointRowCell.Distance(556.0)))
        assertEquals("+200 m", write(WaypointRowCell.Elevation(200.0, null)))
        assertEquals("+200 m -150 m", write(WaypointRowCell.Elevation(200.0, 150.0)))
        assertEquals("1,400 m", write(WaypointRowCell.Altitude(1_400.0)))
        assertEquals(
            "Water, Hot meal",
            write(WaypointRowCell.Amenities(listOf(water, RestSchema.WaypointAmenity.HotMeal))),
        )
        assertEquals("18:00", write(WaypointRowCell.Cutoff("18:00")))
        assertEquals("", write(WaypointRowCell.Cutoff(null)), "no formatter, no moment")
        assertEquals("Beds: 24\nWarden: Anne", write(WaypointRowCell.Properties(fields)))
        assertEquals("", write(WaypointRowCell.Profile(ProfileRange(0.0, 1.0))))
    }

    @Test
    fun aRowPressedPinsTheMapsMarkerAtItsPlaceAndHandsThePlacesIdOn() {
        val map = Controls.engine()
        val cache = Itineraries.cache(reads)
        val pressed = mutableListOf<String>()
        compose.setContent {
            ItineraryWaypointTable(
                "itin_test",
                cache,
                Modifier.width(390.dp),
                map = map,
                onWaypointClick = { pressed += it },
            )
        }
        waitForRow("Refuge")

        compose.onNodeWithContentDescription("Waypoints along the itinerary").assertExists()
        compose.onNodeWithText("Waypoint").onParent().assert(isHeading())
        compose
            .onNodeWithText("Refuge")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .performClick()
        compose.onNodeWithText("Refuge").assertIsSelected()
        compose.runOnIdle {
            val marker =
                map.state.value.markers
                    .single()
            assertEquals(WAYPOINT_TABLE_CURSOR_MARKER_ID, marker.id)
            assertEquals(listOf(6.0, 45.005), marker.position, "the place itself")
            assertNull(map.geometry(GeometrySlot.Emphasis).geometry, "a place lights no stretch")
            assertEquals(listOf("hut"), pressed)
        }

        compose.onNodeWithText("Refuge").performClick()
        compose.onNodeWithText("Refuge").assertIsNotSelected()
        compose.runOnIdle {
            assertTrue(
                map.state.value.markers
                    .isEmpty(),
            )
            assertEquals(listOf("hut", "hut"), pressed)
        }
    }

    /**
     * Handed the app's engine, the table reads its state and never configures or closes it: the app chooses the
     * itinerary, and the engine still answers the question the app asks after the table left.
     */
    @Test
    fun aTableOverTheAppsEngineReadsItsPlacesAndNeverClosesIt() {
        val cache = Itineraries.cache(reads)
        val bare = Itineraries.cache(reads, Itineraries.bare)
        val engine = compose.runOnUiThread { ItineraryResourceEngine(cache, itineraryId = "itin_test") }
        var isPresent by mutableStateOf(true)
        compose.setContent { if (isPresent) ItineraryWaypointTable(engine = engine, modifier = Modifier.width(390.dp)) }
        waitForRow("Refuge")

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

    /**
     * A table's own strings stand over the scope's words for it alone: an unnamed place's word, and a service's name,
     * which Geo's filter reads off the scope it is handed.
     */
    @Test
    fun aTablesOwnStringsStandOverTheScopesWordsTheServicesNamesAmongThem() {
        val cache = Itineraries.cache(reads)
        compose.setContent {
            Column {
                ItineraryWaypointTable(
                    "itin_test",
                    cache,
                    Modifier.width(390.dp),
                    strings = mapOf("profile.unlabelledWaypoint" to "Spring", "amenities.water" to "Drinking water"),
                )
                ItineraryWaypointTable("itin_test", cache, Modifier.width(390.dp))
            }
        }
        waitForRow("Spring")
        chip("Drinking water").assertExists()
        compose.onAllNodesWithText("Unnamed waypoint").assertCountEquals(1)
        compose.onAllNodesWithText("Spring").assertCountEquals(1)
    }

    private companion object {
        /** How long a test waits for a table, whose itinerary is read on the main thread. */
        const val TIMEOUT = 5_000L
    }
}
