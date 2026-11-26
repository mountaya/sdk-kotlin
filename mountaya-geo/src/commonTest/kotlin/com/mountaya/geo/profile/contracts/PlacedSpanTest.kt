package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PlacedSpanTest {
    @Test
    fun twoPlacedStretchesAreEqualWhereTheStretchItsPlaceAndItsNumbersAre() {
        val span = ProfileRange(4_000.0, 10_000.0)
        val placed = PlacedSpan(span, 1, 4_000.0, 10_000.0, 0.0, 100.0)

        assertEquals(PlacedSpan(span, 1, 4_000.0, 10_000.0, 0.0, 100.0), placed)
        assertEquals(PlacedSpan(span, 1, 4_000.0, 10_000.0, 0.0, 100.0).hashCode(), placed.hashCode())
        assertNotEquals(PlacedSpan(span, 0, 4_000.0, 10_000.0, 0.0, 100.0), placed)
    }
}
