package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NearestSampleIndexTest {
    private val points = samples(0.0 to 1_000.0, 1.0 to 1_000.0, 240.0 to 1_100.0)

    @Test
    fun theNearestSampleIsFoundTheEarlierTakingATie() {
        assertEquals(0, nearestSampleIndex(points, 0.4))
        assertEquals(0, nearestSampleIndex(points, 0.5))
        assertEquals(1, nearestSampleIndex(points, 120.5))
        assertEquals(2, nearestSampleIndex(points, 121.0))
    }

    @Test
    fun aDistanceBeforeOrPastTheSamplesReadsThatEndAndNoSampleReadsNone() {
        assertEquals(0, nearestSampleIndex(points, -50.0))
        assertEquals(2, nearestSampleIndex(points, 5_000.0))
        assertEquals(0, nearestSampleIndex(points, Double.NaN))
        assertNull(nearestSampleIndex(emptyList(), 0.0))
    }
}
