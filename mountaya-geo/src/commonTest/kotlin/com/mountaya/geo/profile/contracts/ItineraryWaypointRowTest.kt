package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ItineraryWaypointRowTest {
    @Test
    fun aRowIsTheSpanReachingItsPlaceAndEqualWhereEveryMemberIs() {
        val hut = ProfilePlace(ProfileWaypoint.AtDistance("hut", 200.0), name = "Hut")
        val row = ItineraryWaypointRow("hut#0", hut, 0, 0.0, 200.0, ItineraryWaypointRowMetrics(200.0))
        val span: ProfileSpan<*> = row

        assertEquals(0.0, span.from)
        assertEquals(200.0, span.to)
        assertEquals(ItineraryWaypointRow("hut#0", hut, 0, 0.0, 200.0, ItineraryWaypointRowMetrics(200.0)), row)
        assertNotEquals(ItineraryWaypointRow("hut#1", hut, 0, 0.0, 200.0, ItineraryWaypointRowMetrics(200.0)), row)
    }
}
