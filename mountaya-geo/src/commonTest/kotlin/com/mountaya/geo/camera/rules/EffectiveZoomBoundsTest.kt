package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class EffectiveZoomBoundsTest {
    @Test
    fun theMinimumIsFlooredAndTheMaximumBuffered() {
        assertEquals(ZoomBounds(3.0, 17.0), effectiveZoomBounds(0.0, 18.0))
        assertEquals(ZoomBounds(6.0, 13.0), effectiveZoomBounds(6.0, 14.0))
        assertEquals(ZoomBounds(3.0, 4.0), effectiveZoomBounds(3.0, 5.0))
    }

    /**
     * The minimum is the end that bounds how far a camera pulls out, so it keeps its value and the
     * maximum widens the range.
     */
    @Test
    fun aNarrowRangeIsWidenedAtItsMaximum() {
        assertEquals(ZoomBounds(4.0, 5.0), effectiveZoomBounds(4.0, 5.0))
        assertEquals(ZoomBounds(22.0, 23.0), effectiveZoomBounds(22.0, 22.0))
        assertEquals(ZoomBounds(3.0, 4.0), effectiveZoomBounds(0.0, 2.0))
    }

    @Test
    fun aFractionalRangeIsHeldAsWritten() {
        assertEquals(ZoomBounds(4.5, 15.25), effectiveZoomBounds(4.5, 16.25))
    }

    @Test
    fun nothingIsRefusedOrOrdered() {
        assertEquals(ZoomBounds(10.0, 11.0), effectiveZoomBounds(10.0, 2.0))
        assertEquals(ZoomBounds(Double.NaN, Double.NaN), effectiveZoomBounds(Double.NaN, 18.0))
        assertEquals(ZoomBounds(3.0, Double.NaN), effectiveZoomBounds(0.0, Double.NaN))
    }

    @Test
    fun theFloorTheBufferAndTheSpanAreTheFixturesOwn() {
        assertEquals(3.0, EFFECTIVE_MIN_ZOOM_FLOOR)
        assertEquals(1.0, ZOOM_BUFFER)
        assertEquals(1.0, EFFECTIVE_ZOOM_MIN_SPAN)
    }
}
