package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class CumulativeElevationTest {
    private fun samples(vararg elevations: Double) =
        elevations.mapIndexed { index, it -> ElevationPoint(index * 100.0, it, listOf(0.0, 0.0), index) }

    @Test
    fun aRiseOrFallUnderTheThresholdIsNotCounted() {
        val scan = cumulativeElevation(samples(1_000.0, 1_003.0, 1_001.0, 1_004.0, 1_006.0, 1_000.0))

        assertEquals(listOf(0.0, 0.0, 0.0, 0.0, 6.0, 6.0), scan.ascent)
        assertEquals(listOf(0.0, 0.0, 0.0, 0.0, 0.0, 6.0), scan.descent)
    }

    /** At exactly the threshold the step is counted. */
    @Test
    fun aStepOfExactlyTheThresholdIsCountedAndBecomesTheReference() {
        val scan = cumulativeElevation(samples(0.0, ELEVATION_NOISE_THRESHOLD_METERS, 0.0))

        assertEquals(listOf(0.0, 5.0, 5.0), scan.ascent)
        assertEquals(listOf(0.0, 0.0, 5.0), scan.descent)
    }

    @Test
    fun noSampleIsNoReading() {
        assertEquals(emptyList(), cumulativeElevation(emptyList()).ascent)
    }
}
