package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ProfileWaypointTest {
    @Test
    fun twoWaypointsAreEqualWhenTheyAreOfOneKindWithOneIdAndPlace() {
        val hut = ProfileWaypoint.AtPosition("hut", listOf(6.8752, 45.9237))

        assertEquals(hut, ProfileWaypoint.AtPosition("hut", listOf(6.8752, 45.9237)))
        assertEquals(hut.hashCode(), ProfileWaypoint.AtPosition("hut", listOf(6.8752, 45.9237)).hashCode())
        assertNotEquals<ProfileWaypoint>(
            ProfileWaypoint.AtDistance("hut", 0.0),
            ProfileWaypoint.AtPosition("hut", listOf(0.0)),
        )
        assertNotEquals(ProfileWaypoint.AtDistance("marker", 0.0), ProfileWaypoint.AtDistance("marker", -0.0))
        assertEquals(
            "ProfileWaypoint.AtDistance(id=marker, distance=1200.0)",
            ProfileWaypoint.AtDistance("marker", 1_200.0).toString(),
        )
        assertEquals("hut", hut.id)
    }
}
