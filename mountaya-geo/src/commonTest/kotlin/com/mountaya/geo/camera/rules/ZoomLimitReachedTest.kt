package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoomLimitReachedTest {
    private val window = ZoomBounds(10.0, 14.0)

    @Test
    fun aCameraStandsAtAnEndAtItOrPastItAndAtNeitherAFractionInside() {
        assertEquals(ZoomWindowEnd.Ceiling, zoomLimitReached(14.0, window))
        assertEquals(ZoomWindowEnd.Ceiling, zoomLimitReached(14.5, window))
        assertEquals(ZoomWindowEnd.Floor, zoomLimitReached(10.0, window))
        assertEquals(ZoomWindowEnd.Floor, zoomLimitReached(9.0, window))
        assertEquals(ZoomWindowEnd.None, zoomLimitReached(12.0, window))
        assertEquals(ZoomWindowEnd.None, zoomLimitReached(13.999, window))
        assertEquals(ZoomWindowEnd.None, zoomLimitReached(10.001, window))
        assertEquals(ZoomWindowEnd.Both, zoomLimitReached(12.0, ZoomBounds(12.0, 12.0)))
        assertEquals(ZoomWindowEnd.Floor, zoomLimitReached(-2.0, ZoomBounds(-2.0, 22.0)))
        assertEquals(ZoomWindowEnd.Ceiling, zoomLimitReached(22.0, ZoomBounds(-2.0, 22.0)))
        assertEquals(ZoomWindowEnd.None, zoomLimitReached(Double.NaN, window))
    }

    @Test
    fun theControlTowardAnEndTheCameraStandsAtIsDisabled() {
        assertEquals(emptyList(), zoomControlsDisabled(ZoomWindowEnd.None))
        assertEquals(listOf(ZoomStep.ZoomOut), zoomControlsDisabled(ZoomWindowEnd.Floor))
        assertEquals(listOf(ZoomStep.ZoomIn), zoomControlsDisabled(ZoomWindowEnd.Ceiling))
        assertEquals(listOf(ZoomStep.ZoomIn, ZoomStep.ZoomOut), zoomControlsDisabled(ZoomWindowEnd.Both))
    }
}
