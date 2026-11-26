package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AltitudeBandTest {
    private val points =
        listOf(
            ElevationPoint(0.0, 1_000.0, listOf(0.0, 0.0), 0),
            ElevationPoint(100.0, 1_200.0, listOf(0.0, 0.0), 1),
            ElevationPoint(200.0, 1_100.0, listOf(0.0, 0.0), 2),
        )

    @Test
    fun theBandHoldsTheSamplesWithinAndTheCurveAtBothEnds() {
        assertEquals(1_000.0 to 1_200.0, altitudeBand(points, 0.0, 200.0))
        assertEquals(1_050.0 to 1_200.0, altitudeBand(points, 25.0, 150.0))
        assertEquals(1_125.0 to 1_150.0, altitudeBand(points, 150.0, 175.0))
    }

    @Test
    fun aStretchTheSamplesDoNotReachHasNoBand() {
        assertEquals(1_100.0 to 1_100.0, altitudeBand(points, 200.0, 900.0))
        assertNull(altitudeBand(points, 300.0, 900.0))
        assertNull(altitudeBand(emptyList(), 0.0, 1.0))
    }
}
