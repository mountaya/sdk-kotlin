package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ResolvedItineraryTest {
    private val first = lineString(listOf(6.0, 45.0, 1_000.0), listOf(6.0, 45.001, 1_100.0))
    private val second = lineString(listOf(6.0, 45.001, 1_100.0), listOf(6.0, 45.002, 1_200.0))

    private fun itinerary(distance: Double = 1_000.0): RestSchema.ItineraryWithDetails {
        val hut =
            restPlace(
                "hut",
                position = 1,
                geojson = collectionOf(pointAt(6.0, 45.001, 1_100.0)),
                name = "Hut",
                timing = RestSchema.WaypointTiming { stop_duration = 900 },
            )
        return restItinerary(
            segments =
                listOf(
                    restSegment("b", position = 1, geojson = second, distance = distance, ascent = 100),
                    restSegment("a", position = 0, geojson = first, distance = 500.0, ascent = 50, groupId = "g"),
                ),
            waypoints =
                listOf(
                    hut,
                    restPlace("start", position = 0, geojson = collectionOf(pointAt(6.0, 45.0)), name = "Start"),
                ),
            groups = listOf(restGroup("unused")),
        )
    }

    @Test
    fun everyProjectionIsReadFromOneItineraryAtOnceInChainOrder() {
        val resolved = ResolvedItinerary(itinerary())
        assertEquals(collectionOf(first, second), resolved.geometry)
        assertEquals(listOf("a", "b"), resolved.legs.map { it.id })
        assertEquals(listOf("start", "hut"), resolved.waypoints.map { it.id })
        assertEquals(1_500.0, resolved.characteristics.distance)
        assertEquals(150, resolved.characteristics.ascent)
    }

    @Test
    fun theFiguresAreTheProfilesOwnTheWaypointsPlannedStopsIncluded() {
        val wire = itinerary()
        assertEquals(
            profileCharacteristics(inChainOrder(wire.segments) { it.position }, wire.waypoints),
            ResolvedItinerary(wire).characteristics,
        )
        assertEquals(900, ResolvedItinerary(wire).characteristics.stopDuration)
    }

    @Test
    fun eachProjectionIsTheRulesOwn() {
        val wire = itinerary()
        val resolved = ResolvedItinerary(wire)
        assertEquals(itinerarySegments(wire.segments, wire.groups), resolved.legs)
        assertEquals(itineraryWaypoints(wire.waypoints), resolved.waypoints)
        assertEquals(itineraryGeometry(wire.segments), resolved.geometry)
    }

    @Test
    fun anItineraryOfNoSegmentHasNoGeometryNoLegAndNoFigure() {
        val resolved = ResolvedItinerary(restItinerary())
        assertNull(resolved.geometry)
        assertTrue(resolved.legs.isEmpty())
        assertTrue(resolved.waypoints.isEmpty())
        assertNull(resolved.characteristics.distance)
        assertFalse(resolved.characteristics.durationPartial)
    }

    @Test
    fun anAnswerAndTwoProjectionsAlikeAreEqualAndHashAlike() {
        val resolved = ResolvedItinerary(itinerary())
        val again = ResolvedItinerary(itinerary())
        assertEquals(resolved, resolved)
        assertEquals(again, resolved)
        assertEquals(again.hashCode(), resolved.hashCode())
        assertNotEquals(ResolvedItinerary(itinerary(distance = 2_000.0)), resolved)
    }
}
