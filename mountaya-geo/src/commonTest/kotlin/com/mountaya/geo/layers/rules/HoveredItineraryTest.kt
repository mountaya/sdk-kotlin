package com.mountaya.geo

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class HoveredItineraryTest {
    private fun feature(vararg properties: Pair<String, JsonElement>): Map<String, JsonElement> = properties.toMap()

    private fun text(value: String): JsonElement = JsonPrimitive(value)

    private val nothing = HoveredItinerary(null, null)

    @Test
    fun theFirstLegAndTheFirstPlaceNamedTopmostFirstAFeatureNamingBothAnsweringBoth() {
        assertEquals(
            HoveredItinerary("climb", "hut"),
            hoveredItinerary(
                listOf(
                    feature("waypoint_id" to text("hut")),
                    feature("segment_id" to text("climb")),
                    feature("segment_id" to text("descent")),
                ),
            ),
        )
        assertEquals(
            HoveredItinerary("climb", "col"),
            hoveredItinerary(
                listOf(
                    feature("segment_id" to text("climb"), "waypoint_id" to text("col")),
                    feature("segment_id" to text("descent")),
                ),
            ),
        )
    }

    @Test
    fun aValueThatIsNoIdAnEmptyOneOrOneOfAnotherTypeGivesWayToTheNext() {
        assertEquals(
            nothing,
            hoveredItinerary(listOf(feature("segment_id" to text("")), feature("segment_id" to JsonPrimitive(7)))),
        )
        assertEquals(
            HoveredItinerary("descent", "lake"),
            hoveredItinerary(
                listOf(
                    feature("zone_id" to text("z-1")),
                    feature(),
                    feature("segment_id" to text("descent"), "waypoint_id" to text("")),
                    feature("waypoint_id" to text("lake")),
                ),
            ),
        )
        assertEquals(
            nothing,
            hoveredItinerary(
                listOf(feature("segment_id" to JsonNull, "waypoint_id" to JsonArray(listOf(text("hut"))))),
            ),
        )
    }

    @Test
    fun aFeaturesOwnIdIsNeverReadAndNoFeatureNamesNothing() {
        assertEquals(nothing, hoveredItinerary(listOf(feature("name" to text("Climb"), "id" to text("climb")))))
        assertEquals(nothing, hoveredItinerary(emptyList()))
        assertEquals("segment_id", SEGMENT_ID_PROPERTY)
        assertEquals("waypoint_id", WAYPOINT_ID_PROPERTY)
    }
}
