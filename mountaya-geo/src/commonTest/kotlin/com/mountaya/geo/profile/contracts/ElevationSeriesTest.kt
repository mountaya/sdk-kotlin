package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ElevationSeriesTest {
    private val samples = listOf(ElevationPoint(0.0, 1_035.0, listOf(6.8652, 45.9237), 0))

    @Test
    fun aSeriesHasElevationExactlyWhereItHasASample() {
        assertTrue(ElevationSeries(samples, 0.0, 1_035.0, 1_035.0).hasElevation)
        assertFalse(ElevationSeries(emptyList(), 7_117.5, null, null).hasElevation)
    }

    @Test
    fun twoSeriesAreEqualWhenEverySampleAndFigureIs() {
        val series = ElevationSeries(samples, 10.0, 1_035.0, 1_035.0)

        assertEquals(series, ElevationSeries(samples.toList(), 10.0, 1_035.0, 1_035.0))
        assertEquals(series.hashCode(), ElevationSeries(samples.toList(), 10.0, 1_035.0, 1_035.0).hashCode())
        assertNotEquals(series, ElevationSeries(samples, 10.0, null, 1_035.0))
        assertNotEquals(ElevationSeries(emptyList(), 0.0, null, null), ElevationSeries(emptyList(), -0.0, null, null))
        assertEquals(
            "ElevationSeries(points=[], totalDistance=0.0, minElevation=null, maxElevation=null)",
            ElevationSeries(emptyList(), 0.0, null, null).toString(),
        )
    }
}
