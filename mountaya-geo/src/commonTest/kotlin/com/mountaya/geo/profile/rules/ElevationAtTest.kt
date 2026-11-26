package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ElevationAtTest {
    /** Two lines meet at 200 m: the earlier ends at 1,200 m of altitude and the later starts at 1,000. */
    private val points =
        listOf(
            ElevationPoint(0.0, 1_000.0, listOf(0.0, 0.0), 0),
            ElevationPoint(100.0, 1_100.0, listOf(0.0, 0.0), 1),
            ElevationPoint(200.0, 1_200.0, listOf(0.0, 0.0), 2),
            ElevationPoint(200.0, 1_000.0, listOf(0.0, 0.0), 3),
            ElevationPoint(300.0, 1_050.0, listOf(0.0, 0.0), 4),
        )

    @Test
    fun theCurveIsReadOnTheLineBetweenTheTwoSamplesEitherSide() {
        assertEquals(1_050.0, elevationAt(points, 50.0))
        assertEquals(1_100.0, elevationAt(points, 100.0))
        assertEquals(1_025.0, elevationAt(points, 250.0))
    }

    @Test
    fun beyondTheSamplesTheNearerEndIsRead() {
        assertEquals(1_000.0, elevationAt(points, -10.0))
        assertEquals(1_050.0, elevationAt(points, 900.0))
        assertNull(elevationAt(points, Double.NaN))
        assertNull(elevationAt(emptyList(), 0.0))
    }

    @Test
    fun whereTwoLinesMeetAStretchEndsOnTheEarlierAndStartsOnTheLater() {
        assertEquals(1_200.0, elevationAt(points, 200.0))
        assertEquals(3, startIndexAt(points, 200.0))
        assertEquals(1_000.0, startElevationAt(points, 200.0))
        assertEquals(-1, startIndexAt(points, 150.0))
        assertEquals(1_150.0, startElevationAt(points, 150.0))
    }

    @Test
    fun theFirstSampleAtOrPastADistanceIsFoundByHalving() {
        assertEquals(0, firstIndexAtOrPast(points, 0.0))
        assertEquals(2, firstIndexAtOrPast(points, 150.0))
        assertEquals(2, firstIndexAtOrPast(points, 200.0))
        assertEquals(5, firstIndexAtOrPast(points, 301.0))
        assertEquals(0, firstIndexAtOrPast(points, Double.NaN))
    }
}
