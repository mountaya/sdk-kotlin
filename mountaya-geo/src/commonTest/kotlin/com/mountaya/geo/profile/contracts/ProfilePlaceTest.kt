package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProfilePlaceTest {
    @Test
    fun aNamedPlaceDividesTheItineraryUnlessItSaysOtherwiseABareMarkerOnlyWhereItSaysSo() {
        assertTrue(ProfilePlace(ProfileWaypoint.AtDistance("col", 500.0), name = "Col").dividesRoute)
        assertFalse(ProfilePlace(ProfileWaypoint.AtDistance("water", 200.0)).dividesRoute)
        assertFalse(
            ProfilePlace(ProfileWaypoint.AtDistance("spring", 150.0), name = "Spring", divides = false).dividesRoute,
        )
        assertTrue(ProfilePlace(ProfileWaypoint.AtDistance("marker", 350.0), divides = true).dividesRoute)
    }

    @Test
    fun anItinerarysPlaceStandsAtItsPositionWithItsNameColourGlyphAndDetailsPinned() {
        val details = WaypointDetails(amenities = listOf(RestSchema.WaypointAmenity.Water))
        val place =
            ProfilePlace(
                ItineraryPlace(
                    "hut",
                    listOf(6.8652, 45.9237),
                    name = "Hut",
                    color = "#e8590c",
                    glyph = RestSchema.WaypointIcon("hut"),
                    details = details,
                ),
            )

        assertEquals(ProfileWaypoint.AtPosition("hut", listOf(6.8652, 45.9237)), place.placement)
        assertEquals("hut", place.id)
        assertEquals("Hut", place.name)
        assertEquals("#e8590c", place.color)
        assertEquals(RestSchema.WaypointIcon("hut"), place.glyph)
        assertEquals(details, place.details)
        assertTrue(place.isPinned)
        assertNull(place.divides)
        assertNull(place.metrics)
    }

    @Test
    fun twoPlacesAreEqualWhereEveryMemberIsTheSame() {
        val place = ProfilePlace(ProfileWaypoint.AtDistance("col", 500.0), name = "Col", isPinned = false)
        val same = ProfilePlace(ProfileWaypoint.AtDistance("col", 500.0), name = "Col", isPinned = false)
        val pinned = ProfilePlace(ProfileWaypoint.AtDistance("col", 500.0), name = "Col")
        val measured =
            ProfilePlace(
                ProfileWaypoint.AtDistance("col", 500.0),
                name = "Col",
                isPinned = false,
                metrics = ItinerarySegmentRowMetrics(ascent = 250.0),
            )

        assertEquals(same, place)
        assertEquals(same.hashCode(), place.hashCode())
        assertNotEquals(pinned, place)
        assertNotEquals(measured, place)
    }
}
