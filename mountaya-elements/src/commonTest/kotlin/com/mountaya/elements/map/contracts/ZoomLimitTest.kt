package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ZoomLimitTest {
    @Test
    fun aLimitIsKeptReleasedOrAZoom() {
        assertEquals<ZoomLimit>(ZoomLimit.Zoom(8.0), ZoomLimit.Zoom(8.0))
        assertEquals<ZoomLimit>(ZoomLimit.Zoom(12.5), ZoomLimit.Zoom(12.5))
        assertEquals(ZoomLimit.Zoom(12.5).hashCode(), ZoomLimit.Zoom(12.5).hashCode())
        assertNotEquals<ZoomLimit>(ZoomLimit.Kept, ZoomLimit.Released)
        assertEquals<ZoomLimit>(ZoomLimit.Zoom(Double.NaN), ZoomLimit.Zoom(Double.NaN))
        assertNotEquals<ZoomLimit>(ZoomLimit.Zoom(0.0), ZoomLimit.Zoom(-0.0))
    }
}
