package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ZoomIntoRangeTest {
    private val window = ZoomBounds(10.0, 14.0)

    @Test
    fun aCameraOutsideTheWindowMovesToItsNearerEdge() {
        assertEquals(10.0, zoomIntoRange(6.0, window))
        assertEquals(10.0, zoomIntoRange(9.5, window))
        assertEquals(14.0, zoomIntoRange(18.0, window))
        assertEquals(14.0, zoomIntoRange(14.25, window))
        // A window under zoom 0, a renderer's own floor, is reached from above.
        assertEquals(0.0, zoomIntoRange(2.0, ZoomBounds(-2.0, 0.0)))
    }

    @Test
    fun aCameraInsideTheWindowItsEdgesIncludedStays() {
        assertNull(zoomIntoRange(12.0, window))
        assertNull(zoomIntoRange(10.0, window))
        assertNull(zoomIntoRange(14.0, window))
    }

    @Test
    fun noMoveSatisfiesAWindowWithAnEndThatIsNoFiniteNumberOrAFloorAboveItsCeiling() {
        assertNull(zoomIntoRange(6.0, ZoomBounds(12.0, 8.0)))
        assertNull(zoomIntoRange(6.0, ZoomBounds(10.0, Double.POSITIVE_INFINITY)))
        assertNull(zoomIntoRange(18.0, ZoomBounds(Double.NaN, 14.0)))
        assertNull(zoomIntoRange(Double.NaN, window))
    }
}
