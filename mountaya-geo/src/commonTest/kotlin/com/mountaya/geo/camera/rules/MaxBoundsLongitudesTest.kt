package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MaxBoundsLongitudesTest {
    private fun assertEdges(
        expected: List<Double>,
        actual: List<Double>,
    ) {
        assertEquals(2, actual.size)
        // Finer than the hair, so an answer a whole turn wide fails.
        expected.indices.forEach { assertEquals(expected[it], actual[it], 1e-11, "edge $it of $actual") }
    }

    @Test
    fun aLockNarrowerThanATurnKeepsItsWidth() {
        assertEquals(listOf(5.0, 7.0), maxBoundsLongitudes(5.0, 7.0))
        assertEquals(listOf(179.5, 180.5), maxBoundsLongitudes(179.5, 180.5))
        assertEquals(listOf(-179.5, 180.0), maxBoundsLongitudes(-179.5, 180.0))
    }

    @Test
    fun itsWestEdgeIsBroughtWithinOneWorldAndItsEastEdgeFollows() {
        assertEquals(listOf(5.0, 7.0), maxBoundsLongitudes(365.0, 367.0))
        assertEquals(listOf(179.5, 180.5), maxBoundsLongitudes(-180.5, -179.5))
    }

    /** A renderer reads a pair written east first as a lock across the antimeridian. */
    @Test
    fun edgesWrittenEastFirstAreNeverReordered() {
        assertEquals(listOf(170.0, -170.0), maxBoundsLongitudes(170.0, -170.0))
    }

    @Test
    fun aLockATurnOrMoreWideIsTheWholeWorldAHairUnderATurnAroundItsCentre() {
        assertEdges(listOf(-179.9999999999, 179.9999999999), maxBoundsLongitudes(-180.0, 180.0))
        assertEdges(listOf(-179.9999999999, 179.9999999999), maxBoundsLongitudes(-200.0, 200.0))
        assertEdges(listOf(1e-10, 359.9999999999), maxBoundsLongitudes(0.0, 360.0))
        assertEdges(listOf(-59.9999999999, 299.9999999999), maxBoundsLongitudes(10_000_000.0, 10_000_400.0))
        val world = maxBoundsLongitudes(-180.0, 180.0)
        assertTrue(world[1] - world[0] < 360.0)
        assertEquals(179.9999999999, WORLD_LOCK_HALF_SPAN_DEGREES, 1e-12)
    }

    @Test
    fun aLockReachingAnInfinityIsTheWholeWorldOnThePrimeMeridian() {
        assertEdges(listOf(-179.9999999999, 179.9999999999), maxBoundsLongitudes(Double.NEGATIVE_INFINITY, 5.0))
        assertEdges(listOf(-179.9999999999, 179.9999999999), maxBoundsLongitudes(5.0, Double.POSITIVE_INFINITY))
        assertEdges(
            listOf(-179.9999999999, 179.9999999999),
            maxBoundsLongitudes(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
        )
    }

    @Test
    fun anEdgeAtNoNumberPlacesTheLockNowhere() {
        val nowhere = listOf(Double.NaN, Double.NaN)

        assertEquals(nowhere, maxBoundsLongitudes(Double.NaN, 5.0))
        assertEquals(nowhere, maxBoundsLongitudes(5.0, Double.NaN))
        assertEquals(nowhere, maxBoundsLongitudes(Double.POSITIVE_INFINITY, 5.0))
        assertEquals(nowhere, maxBoundsLongitudes(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY))
        assertEquals(nowhere, maxBoundsLongitudes(5.0, Double.NEGATIVE_INFINITY))
    }
}
