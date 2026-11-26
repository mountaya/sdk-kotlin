package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MapViewportTest {
    @Test
    fun twoViewportsAreEqualWhenTheirCenterAndZoomAre() {
        assertEquals(MapViewport(listOf(8.2275, 46.8182), 6.0), MapViewport(listOf(8.2275, 46.8182), 6.0))
        assertEquals(
            MapViewport(listOf(8.2275, 46.8182), 6.0).hashCode(),
            MapViewport(listOf(8.2275, 46.8182), 6.0).hashCode(),
        )
        assertNotEquals(MapViewport(listOf(8.2275, 46.8182), 6.0), MapViewport(listOf(8.2275, 46.8182), 5.0))
        assertNotEquals(MapViewport(listOf(8.2275, 46.8182), 6.0), MapViewport(listOf(46.8182, 8.2275), 6.0))
    }

    /**
     * Equal values hash alike: a zoom of -0 is another viewport than one of 0, and a NaN zoom is
     * the same as itself.
     */
    @Test
    fun twoViewportsCompareTheirNumbersAsTheSameNumbers() {
        assertNotEquals(MapViewport(listOf(0.0, 0.0), 0.0), MapViewport(listOf(0.0, 0.0), -0.0))
        assertNotEquals(MapViewport(listOf(0.0, 0.0), 0.0), MapViewport(listOf(-0.0, 0.0), 0.0))
        assertEquals(MapViewport(listOf(0.0, 0.0), Double.NaN), MapViewport(listOf(0.0, 0.0), Double.NaN))
        assertEquals(
            MapViewport(listOf(Double.NaN, 0.0), Double.NaN).hashCode(),
            MapViewport(listOf(Double.NaN, 0.0), Double.NaN).hashCode(),
        )
    }

    @Test
    fun theCenterIsWrittenLongitudeFirst() {
        assertEquals(
            "MapViewport(center=[8.2275, 46.8182], zoom=6.0)",
            MapViewport(listOf(8.2275, 46.8182), 6.0).toString(),
        )
    }
}
