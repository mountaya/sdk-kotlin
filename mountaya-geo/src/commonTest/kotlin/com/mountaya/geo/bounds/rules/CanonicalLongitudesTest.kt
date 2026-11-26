package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class CanonicalLongitudesTest {
    @Test
    fun aWestEdgeWithinOneWorldKeepsItsDigits() {
        assertEquals(listOf(6.86, 6.9), canonicalLongitudes(6.86, 6.9))
        assertEquals(listOf(179.5, 180.5), canonicalLongitudes(179.5, 180.5))
        assertEquals(listOf(180.0, 181.0), canonicalLongitudes(180.0, 181.0))
        assertEquals(listOf(-180.0, -179.0), canonicalLongitudes(-180.0, -179.0))
    }

    @Test
    fun aWestEdgePastTheRangeIsBroughtBackByWholeTurnsAndTheEastEdgeFollows() {
        assertEquals(listOf(179.5, 180.5), canonicalLongitudes(-180.5, -179.5))
        assertEquals(listOf(5.0, 7.0), canonicalLongitudes(365.0, 367.0))
        assertEquals(listOf(-180.0, -170.0), canonicalLongitudes(540.0, 550.0))
        assertEquals(listOf(-180.0, -170.0), canonicalLongitudes(-540.0, -530.0))
    }

    @Test
    fun aTurnOrMoreWideIsTheWholeWorld() {
        assertEquals(listOf(-180.0, 180.0), canonicalLongitudes(0.0, 360.0))
        assertEquals(listOf(-180.0, 180.0), canonicalLongitudes(-200.0, 200.0))
        assertEquals(listOf(-180.0, 180.0), canonicalLongitudes(Double.NEGATIVE_INFINITY, 5.0))
        assertEquals(listOf(-180.0, 180.0), canonicalLongitudes(5.0, Double.POSITIVE_INFINITY))
    }

    @Test
    fun anEdgeAtNoNumberPlacesTheBoxNowhere() {
        val nowhere = listOf(Double.NaN, Double.NaN)

        assertEquals(nowhere, canonicalLongitudes(Double.NaN, 5.0))
        assertEquals(nowhere, canonicalLongitudes(5.0, Double.NaN))
        assertEquals(nowhere, canonicalLongitudes(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY))
        assertEquals(nowhere, canonicalLongitudes(Double.POSITIVE_INFINITY, 5.0))
        assertEquals(nowhere, canonicalLongitudes(5.0, Double.NEGATIVE_INFINITY))
    }
}
