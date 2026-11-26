package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class MarkerPassesTest {
    /** Out along the equator to 0.03 degrees and back: a place on it is passed twice. */
    private val outAndBack = listOf(listOf(0.0, 0.01, 0.02, 0.03, 0.02, 0.01, 0.0).map { listOf(it, 0.0) })

    @Test
    fun theLegsAreLaidEndToEndAndTheJumpBetweenLinesIsNone() {
        val legs =
            walkLegs(
                listOf(
                    listOf(listOf(0.0, 0.0), listOf(1.0, 0.0)),
                    listOf(listOf(5.0, 0.0), listOf(Double.NaN, 0.0), listOf(6.0, 0.0)),
                ),
            )

        assertEquals(2, legs.size)
        assertEquals(listOf(0.0, METERS_PER_DEGREE_MEAN_SPHERE), legs.map { it.start })
        assertEquals(listOf(5.0, 6.0), listOf(legs[1].previous[0], legs[1].current[0]))
    }

    @Test
    fun eachRunOfNearLegsIsOnePassAtItsNearestPoint() {
        val passes = markerPasses(walkLegs(outAndBack), listOf(0.01, 0.0), 60.0)

        assertEquals(2, passes.size)
        assertEquals(0.01 * METERS_PER_DEGREE_MEAN_SPHERE, passes[0].distance, 1e-6)
        assertEquals(0.05 * METERS_PER_DEGREE_MEAN_SPHERE, passes[1].distance, 1e-6)
        assertEquals(0.0, passes[0].meters, 1e-9)
    }

    @Test
    fun aPlaceTheLineNeverComesNearOrThatNamesNoPlaceHasNoPass() {
        assertEquals(emptyList(), markerPasses(walkLegs(outAndBack), listOf(0.01, 0.01), 60.0).map { it.distance })
        assertEquals(emptyList(), markerPasses(walkLegs(outAndBack), listOf(Double.NaN, 0.0), 60.0).map { it.distance })
        assertEquals(emptyList(), markerPasses(walkLegs(outAndBack), listOf(0.01), 60.0).map { it.distance })
    }

    /** 360 degrees east is the prime meridian: the place stands where the line starts and ends. */
    @Test
    fun aPlaceWrittenATurnAwayIsMeasuredWhereItStands() {
        val passes = markerPasses(walkLegs(outAndBack), listOf(360.0, 0.0), 60.0)

        assertEquals(2, passes.size)
        assertEquals(0.0, passes[0].distance)
        assertEquals(0.06 * METERS_PER_DEGREE_MEAN_SPHERE, passes[1].distance, 1e-6)
    }
}
