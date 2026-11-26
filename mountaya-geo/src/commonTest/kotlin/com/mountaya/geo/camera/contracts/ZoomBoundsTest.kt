package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ZoomBoundsTest {
    @Test
    fun twoRangesAreEqualWhenTheyHoldTheSameZooms() {
        assertEquals(ZoomBounds(3.0, 17.0), ZoomBounds(3.0, 17.0))
        assertEquals(ZoomBounds(3.0, 17.0).hashCode(), ZoomBounds(3.0, 17.0).hashCode())
        assertNotEquals(ZoomBounds(3.0, 17.0), ZoomBounds(17.0, 3.0))
        assertEquals(ZoomBounds(Double.NaN, 17.0), ZoomBounds(Double.NaN, 17.0))
        assertNotEquals(ZoomBounds(0.0, 17.0), ZoomBounds(-0.0, 17.0))
    }

    @Test
    fun eachZoomIsNamedByTheEndItHolds() {
        val bounds = ZoomBounds(minZoom = 6.0, maxZoom = 13.0)

        assertEquals(listOf(6.0, 13.0), listOf(bounds.minZoom, bounds.maxZoom))
        assertEquals("ZoomBounds(minZoom=6.0, maxZoom=13.0)", bounds.toString())
    }
}
