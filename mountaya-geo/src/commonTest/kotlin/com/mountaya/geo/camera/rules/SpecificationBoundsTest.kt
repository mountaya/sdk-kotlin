package com.mountaya.geo

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SpecificationBoundsTest {
    @Test
    fun aBoxIsAnsweredAsWrittenAndOneAcrossTheAntimeridianWithItsEastEdgePast180() {
        assertEquals(listOf(5.9, 45.8, 10.5, 47.8), specificationBounds(listOf(5.9, 45.8, 10.5, 47.8)))
        val across = specificationBounds(listOf(179.8, -17.0, -179.9, -16.0))
        assertEquals(4, across?.size)
        assertTrue(abs((across?.get(2) ?: 0.0) - 180.1) < 1e-9, "$across")
        assertEquals(179.8, across?.get(0))
        // A box of one meridian is no box across the antimeridian.
        assertEquals(listOf(6.86, 45.8, 6.86, 45.9), specificationBounds(listOf(6.86, 45.8, 6.86, 45.9)))
    }

    @Test
    fun noBoxOrABoxThatIsNotFourNumbersFramesNothing() {
        assertNull(specificationBounds(null))
        assertNull(specificationBounds(listOf(5.9, 45.8, 10.5)))
        assertNull(specificationBounds(emptyList()))
    }

    @Test
    fun itHandsNormalizeBoundingBoxABoxItFramesTheShortWayNeverTheWholeWorld() {
        val bounds = specificationBounds(listOf(179.8, -17.0, -179.9, -16.0))
        val read = normalizeBoundingBox(assertNotNull(bounds, "bounds across the antimeridian are read"))
        val asWritten = normalizeBoundingBox(listOf(179.8, -17.0, -179.9, -16.0))

        assertTrue(abs(read[2] - read[0] - 0.3) < 1e-9, "$read")
        assertTrue(asWritten[2] - asWritten[0] > 359.0, "$asWritten")
    }
}
