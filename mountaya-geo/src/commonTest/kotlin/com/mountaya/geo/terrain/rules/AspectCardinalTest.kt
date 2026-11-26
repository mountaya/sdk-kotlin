package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AspectCardinalTest {
    @Test
    fun eachDirectionNamesTheSectorCentredOnItsHeading() {
        for (direction in AspectCardinal.entries) {
            assertEquals(direction, aspectCardinal(direction.heading))
            assertEquals(direction, aspectCardinal(direction.heading + 22.4))
            assertEquals(direction, aspectCardinal(direction.heading - 22.5))
        }
    }

    @Test
    fun anEdgeNamesTheSectorClockwiseOfIt() {
        assertEquals(AspectCardinal.NorthEast, aspectCardinal(22.5))
        assertEquals(AspectCardinal.North, aspectCardinal(337.5))
    }

    @Test
    fun aBearingIsFoldedByWholeTurns() {
        assertEquals(AspectCardinal.NorthWest, aspectCardinal(-45.0))
        assertEquals(AspectCardinal.East, aspectCardinal(810.0))
        assertEquals(AspectCardinal.North, aspectCardinal(360.0))
        assertEquals(AspectCardinal.North, aspectCardinal(-0.0))
    }

    @Test
    fun aBearingOfNoFiniteNumberNamesNone() {
        assertNull(aspectCardinal(Double.NaN))
        assertNull(aspectCardinal(Double.POSITIVE_INFINITY))
        assertNull(aspectCardinal(Double.NEGATIVE_INFINITY))
    }
}
