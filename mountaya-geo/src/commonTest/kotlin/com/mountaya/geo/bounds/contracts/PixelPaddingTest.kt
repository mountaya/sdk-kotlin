package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PixelPaddingTest {
    @Test
    fun oneNumberIsThePaddingOfEveryEdge() {
        assertEquals(PixelPadding(top = 100.0, right = 100.0, bottom = 100.0, left = 100.0), PixelPadding(100.0))
        assertEquals(PixelPadding(100.0).hashCode(), PixelPadding(100.0, 100.0, 100.0, 100.0).hashCode())
    }

    @Test
    fun eachEdgeIsNamedByItsSide() {
        val padding = PixelPadding(top = 56.0, right = 57.0, bottom = 250.0, left = 58.0)

        assertEquals(listOf(56.0, 57.0, 250.0, 58.0), listOf(padding.top, padding.right, padding.bottom, padding.left))
        assertEquals("PixelPadding(top=56.0, right=57.0, bottom=250.0, left=58.0)", padding.toString())
        assertNotEquals(PixelPadding(56.0, 57.0, 250.0, 58.0), PixelPadding(250.0, 57.0, 56.0, 58.0))
    }

    @Test
    fun twoPaddingsCompareTheirNumbersAsTheSameNumbers() {
        assertEquals(PixelPadding(Double.NaN), PixelPadding(Double.NaN))
        assertNotEquals(PixelPadding(0.0), PixelPadding(-0.0))
    }
}
