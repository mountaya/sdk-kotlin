package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BearingDegreesTest {
    @Test
    fun theBearingIsACompassHeadingClockwiseFromNorth() {
        assertEquals(0.0, bearingDegrees(listOf(0.0, 0.0), listOf(0.0, 1.0)))
        assertEquals(90.0, bearingDegrees(listOf(0.0, 0.0), listOf(1.0, 0.0)))
        assertEquals(180.0, bearingDegrees(listOf(0.0, 0.0), listOf(0.0, -1.0)))
        assertEquals(270.0, bearingDegrees(listOf(0.0, 0.0), listOf(-1.0, 0.0)))
        // The initial heading of the great circle, not the planar angle of the two positions.
        assertEquals(44.99563645534485, bearingDegrees(listOf(0.0, 0.0), listOf(1.0, 1.0)), 1e-9)
        assertEquals(44.023155178806974, bearingDegrees(listOf(6.8652, 45.9237), listOf(6.9271, 45.9682)), 1e-9)
    }

    @Test
    fun theLongitudesAreReadTheShortWayRound() {
        assertEquals(90.0, bearingDegrees(listOf(179.9, 0.0), listOf(-179.9, 0.0)), 1e-9)
        assertEquals(270.0, bearingDegrees(listOf(-179.9, 0.0), listOf(179.9, 0.0)), 1e-9)
        assertEquals(90.0, bearingDegrees(listOf(190.0, 0.0), listOf(-169.0, 0.0)), 1e-9)
    }

    /**
     * A position written 180 and one written -180 are one place, read the short way round: no
     * heading, rather than the 90 a turn's sine left over would answer.
     */
    @Test
    fun onePlaceHasNoHeadingAndAnswersZero() {
        assertEquals(0.0, bearingDegrees(listOf(6.8652, 45.9237), listOf(6.8652, 45.9237)))
        assertEquals(0.0, bearingDegrees(listOf(180.0, 10.0), listOf(-180.0, 10.0)))
        assertEquals(0.0, bearingDegrees(listOf(6.8652, 45.9237, 1000.0), listOf(6.8652, 45.9237, 2000.0)))
    }

    @Test
    fun aHeadingAHairWestOfNorthStaysUnderATurn() {
        assertEquals(0.0, bearingDegrees(listOf(0.0, 0.0), listOf(-1e-16, 1.0)))
        val justWest = bearingDegrees(listOf(0.0, 0.0), listOf(-1e-9, 1.0))
        assertTrue(justWest < 360.0 && justWest > 359.0)
    }

    @Test
    fun aPositionThatNamesNoPlaceHasNoBearing() {
        assertTrue(bearingDegrees(listOf(6.0), listOf(6.0, 45.0)).isNaN())
        assertTrue(bearingDegrees(listOf(6.0, 45.0), listOf(Double.NaN, 45.0)).isNaN())
        assertTrue(bearingDegrees(listOf(6.0, Double.POSITIVE_INFINITY), listOf(6.0, 45.0)).isNaN())
    }

    @Test
    fun aCompassBearingIsWrittenWithinOneTurn() {
        assertEquals(270.0, compassBearing(-90.0))
        assertEquals(180.0, compassBearing(180.0))
        // A negative zero reads as zero, and a hair under zero that rounds to a turn is north.
        assertEquals(0.0, compassBearing(-0.0))
        assertEquals(0.0, compassBearing(-1e-15))
        assertTrue(compassBearing(Double.NaN).isNaN())
    }
}
