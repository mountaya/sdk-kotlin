package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class DistanceToSegmentTest {
    private val start = listOf(6.8652, 45.9237)
    private val end = listOf(6.9271, 45.9682)

    /** In degrees as written, with no cosine: the fixture's cases answer that, and it is not meters. */
    @Test
    fun theDistanceIsPlanarInTheDegreesTheyAreWrittenIn() {
        assertEquals(0.0, distanceToSegment(start, start, end))
        assertEquals(0.039556739110611465, distanceToSegment(listOf(6.9, 45.9), start, end), 1e-15)
        assertEquals(5.0, distanceToSegment(listOf(3.0, 4.0), listOf(0.0, 0.0), listOf(0.0, 0.0)))
    }

    @Test
    fun aPointPastAnEndIsMeasuredToThatEnd() {
        assertEquals(0.07953395501293625, distanceToSegment(listOf(7.0, 46.0), start, end), 1e-15)
        assertEquals(1.0, distanceToSegment(listOf(-1.0, 0.0), listOf(0.0, 0.0), listOf(4.0, 0.0)))
    }

    @Test
    fun aDegenerateSegmentAnswersTheDistanceToItsOneVertex() {
        assertEquals(0.07623555600899513, distanceToSegment(end, start, start), 1e-15)
    }

    /** A plane has no antimeridian: these two are 359.8 degrees apart as written. */
    @Test
    fun nothingIsWrapped() {
        assertEquals(359.8, distanceToSegment(listOf(179.9, 0.0), listOf(-179.9, 0.0), listOf(-179.9, 0.0)), 1e-12)
    }

    @Test
    fun aPositionThatNamesNoPlaceAnswersNaN() {
        assertEquals(Double.NaN, distanceToSegment(listOf(7.0), start, end))
        assertEquals(Double.NaN, distanceToSegment(listOf(7.0, 46.0), listOf(Double.NaN, 45.0), end))
        assertEquals(Double.NaN, distanceToSegment(listOf(7.0, 46.0), start, emptyList()))
    }

    /** The one projection both the distance and a traveller's progress stand on. */
    @Test
    fun theFootIsTheProjectionClampedToTheSegmentsEnds() {
        assertEquals(0.25, planarFootFraction(1.0, 3.0, 0.0, 0.0, 4.0, 0.0))
        assertEquals(0.0, planarFootFraction(-1.0, 0.0, 0.0, 0.0, 4.0, 0.0))
        assertEquals(1.0, planarFootFraction(9.0, 1.0, 0.0, 0.0, 4.0, 0.0))
        assertEquals(0.0, planarFootFraction(3.0, 4.0, 2.0, 2.0, 2.0, 2.0))
        assertEquals(3.0, planarDistanceToFoot(1.0, 3.0, 0.0, 0.0, 4.0, 0.0, 0.25))
        assertEquals(5.0, planarDistanceToSegment(7.0, 4.0, 0.0, 0.0, 4.0, 0.0))
    }
}
