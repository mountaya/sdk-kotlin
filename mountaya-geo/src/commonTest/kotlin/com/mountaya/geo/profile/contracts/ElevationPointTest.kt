package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ElevationPointTest {
    @Test
    fun twoSamplesAreEqualWhenTheyHoldTheSameNumbers() {
        val sample = ElevationPoint(952.5, 1_120.0, listOf(6.8752, 45.9287), 1)

        assertEquals(sample, ElevationPoint(952.5, 1_120.0, listOf(6.8752, 45.9287), 1))
        assertEquals(sample.hashCode(), ElevationPoint(952.5, 1_120.0, listOf(6.8752, 45.9287), 1).hashCode())
        assertNotEquals(sample, ElevationPoint(952.5, 1_120.0, listOf(6.8752, 45.9287), 2))
        assertNotEquals(ElevationPoint(0.0, 0.0, listOf(0.0, 0.0), 0), ElevationPoint(-0.0, 0.0, listOf(0.0, 0.0), 0))
        assertEquals(
            ElevationPoint(Double.NaN, 0.0, listOf(0.0, 0.0), 0),
            ElevationPoint(Double.NaN, 0.0, listOf(0.0, 0.0), 0),
        )
        assertEquals(
            "ElevationPoint(distance=952.5, elevation=1120.0, lngLat=[6.8752, 45.9287], index=1)",
            sample.toString(),
        )
    }
}
