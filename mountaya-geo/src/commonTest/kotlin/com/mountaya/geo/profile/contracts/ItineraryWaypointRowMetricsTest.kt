package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class ItineraryWaypointRowMetricsTest {
    @Test
    fun everyFigureButTheDistanceIsNullUnlessGivenAndEqualByNumber() {
        val metrics = ItineraryWaypointRowMetrics(300.0)

        assertNull(metrics.sectionDistance)
        assertNull(metrics.altitude)
        assertNull(metrics.ascent)
        assertEquals(ItineraryWaypointRowMetrics(300.0), metrics)
        assertNotEquals(ItineraryWaypointRowMetrics(300.0, altitude = 1_000.0), metrics)
        assertNotEquals(ItineraryWaypointRowMetrics(-0.0), ItineraryWaypointRowMetrics(0.0))
        assertEquals(ItineraryWaypointRowMetrics(300.0).hashCode(), metrics.hashCode())
    }
}
