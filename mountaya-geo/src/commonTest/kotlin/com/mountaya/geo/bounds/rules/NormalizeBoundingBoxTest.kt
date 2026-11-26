package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class NormalizeBoundingBoxTest {
    @Test
    fun aCanonicalBoxIsAnsweredAsItIs() {
        assertEquals(listOf(6.86, 45.8, 6.9, 45.92), normalizeBoundingBox(listOf(6.86, 45.8, 6.9, 45.92)))
    }

    /**
     * A west edge greater than the east one is a box drawn from its east corner, never one across
     * the antimeridian: that box is written with its east edge past 180.
     */
    @Test
    fun eachAxisIsOrderedLowToHigh() {
        assertEquals(listOf(6.86, 45.8, 6.9, 45.92), normalizeBoundingBox(listOf(6.9, 45.92, 6.86, 45.8)))
        assertEquals(listOf(-170.0, 45.0, 170.0, 46.0), normalizeBoundingBox(listOf(170.0, 45.0, -170.0, 46.0)))
    }

    @Test
    fun theWestEdgeIsBroughtWithinOneWorldAndTheEastEdgeFollows() {
        assertEquals(listOf(179.5, -17.0, 180.5, -16.0), normalizeBoundingBox(listOf(179.5, -17.0, 180.5, -16.0)))
        assertEquals(listOf(179.5, -17.0, 180.5, -16.0), normalizeBoundingBox(listOf(-180.5, -17.0, -179.5, -16.0)))
        assertEquals(listOf(5.0, 0.0, 7.0, 1.0), normalizeBoundingBox(listOf(365.0, 0.0, 367.0, 1.0)))
    }

    @Test
    fun aTurnOrMoreWideIsTheWholeWorld() {
        assertEquals(listOf(-180.0, 0.0, 180.0, 10.0), normalizeBoundingBox(listOf(-200.0, 0.0, 200.0, 10.0)))
        assertEquals(listOf(-180.0, 0.0, 180.0, 10.0), normalizeBoundingBox(listOf(0.0, 0.0, 360.0, 10.0)))
        assertEquals(
            listOf(-180.0, 0.0, 180.0, 10.0),
            normalizeBoundingBox(listOf(Double.NEGATIVE_INFINITY, 0.0, 5.0, 10.0)),
        )
    }

    @Test
    fun theLatitudesAreHeldWithinTheWebMercatorLimit() {
        assertEquals(listOf(-10.0, -85.051129, 10.0, 85.051129), normalizeBoundingBox(listOf(-10.0, -89.0, 10.0, 89.0)))
        assertEquals(listOf(-10.0, 85.051129, 10.0, 85.051129), normalizeBoundingBox(listOf(-10.0, 86.0, 10.0, 89.0)))
    }

    @Test
    fun nothingIsRefused() {
        assertEquals(
            listOf(Double.NaN, 45.0, Double.NaN, 46.0),
            normalizeBoundingBox(listOf(Double.NaN, 45.0, 6.0, 46.0)),
        )
        assertEquals(
            listOf(Double.NaN, 45.0, Double.NaN, 46.0),
            normalizeBoundingBox(listOf(Double.POSITIVE_INFINITY, 45.0, Double.POSITIVE_INFINITY, 46.0)),
        )
        assertEquals(listOf(5.0, Double.NaN, 6.0, Double.NaN), normalizeBoundingBox(listOf(5.0, Double.NaN, 6.0, 46.0)))
    }

    @Test
    fun aBoxThatIsNotFourNumbersIsNoBox() {
        val none = List(4) { Double.NaN }

        assertEquals(none, normalizeBoundingBox(emptyList()))
        assertEquals(none, normalizeBoundingBox(listOf(6.0, 45.0, 7.0)))
        assertEquals(none, normalizeBoundingBox(listOf(6.0, 45.0, 0.0, 7.0, 46.0, 1000.0)))
    }
}
