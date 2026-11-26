package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RefusesMaxBoundsTest {
    @Test
    fun aLockThatEnclosesAnAreaIsNotRefused() {
        assertFalse(refusesMaxBounds(listOf(5.0, 45.0, 7.0, 46.0)))
        assertFalse(refusesMaxBounds(listOf(179.5, 45.0, 180.5, 46.0)))
        assertFalse(refusesMaxBounds(listOf(-179.9999999999, 45.0, 179.9999999999, 46.0)))
        // Drawn upside down, a lock still has a height to divide by.
        assertFalse(refusesMaxBounds(listOf(5.0, 46.0, 7.0, 45.0)))
    }

    /** An east edge west of the west edge is how a renderer reads a lock across the antimeridian. */
    @Test
    fun aLockWrittenAcrossTheAntimeridianIsNotRefusedForIt() {
        assertFalse(refusesMaxBounds(listOf(170.0, 45.0, -170.0, 46.0)))
    }

    @Test
    fun aLockWhoseEdgesAreAWholeNumberOfTurnsApartIsRefused() {
        assertTrue(refusesMaxBounds(listOf(5.0, 45.0, 5.0, 46.0)))
        assertTrue(refusesMaxBounds(listOf(-180.0, 45.0, 180.0, 46.0)))
        assertTrue(refusesMaxBounds(listOf(-360.0, 45.0, 360.0, 46.0)))
        assertTrue(refusesMaxBounds(listOf(180.0, 45.0, -180.0, 46.0)))
    }

    @Test
    fun aLockWhoseSouthAndNorthEdgesAreOneParallelIsRefused() {
        assertTrue(refusesMaxBounds(listOf(5.0, 45.0, 7.0, 45.0)))
    }

    @Test
    fun aLockWithAnEdgeAtNoFiniteCoordinateOrNotFourEdgesIsRefused() {
        assertTrue(refusesMaxBounds(listOf(Double.NaN, 45.0, 6.0, 46.0)))
        assertTrue(refusesMaxBounds(listOf(0.0, 0.0, Double.POSITIVE_INFINITY, 10.0)))
        assertTrue(refusesMaxBounds(listOf(0.0, Double.NEGATIVE_INFINITY, 1.0, 10.0)))
        assertTrue(refusesMaxBounds(listOf(5.0, 45.0, 7.0)))
        assertTrue(refusesMaxBounds(emptyList()))
    }
}
