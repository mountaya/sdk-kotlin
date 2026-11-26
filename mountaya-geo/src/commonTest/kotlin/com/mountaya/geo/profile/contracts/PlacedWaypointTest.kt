package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PlacedWaypointTest {
    private val hut = ProfileWaypoint.AtPosition("hut", listOf(6.8752, 45.9237))

    @Test
    fun twoPlacementsAreEqualWhenTheyHoldOneKeyWaypointAndDistance() {
        assertEquals(PlacedWaypoint("hut#1", hut, 6_130.7), PlacedWaypoint("hut#1", hut, 6_130.7))
        assertEquals(PlacedWaypoint("hut#1", hut, 6_130.7).hashCode(), PlacedWaypoint("hut#1", hut, 6_130.7).hashCode())
        assertNotEquals(PlacedWaypoint("hut#1", hut, 6_130.7), PlacedWaypoint("hut#0", hut, 6_130.7))
        assertEquals(
            "PlacedWaypoint(key=hut#0, waypoint=ProfileWaypoint.AtPosition(id=hut, position=[6.8752, 45.9237]), distance=773.4)",
            PlacedWaypoint("hut#0", hut, 773.4).toString(),
        )
    }
}
