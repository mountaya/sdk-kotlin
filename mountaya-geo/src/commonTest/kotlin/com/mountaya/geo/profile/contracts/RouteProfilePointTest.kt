package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class RouteProfilePointTest {
    @Test
    fun twoSamplesAreEqualWhenTheyHoldTheSameNumbers() {
        assertEquals(RouteProfilePoint(0.8, 1_000.0), RouteProfilePoint(0.8, 1_000.0))
        assertEquals(RouteProfilePoint(0.8, 1_000.0).hashCode(), RouteProfilePoint(0.8, 1_000.0).hashCode())
        assertNotEquals(RouteProfilePoint(0.8, 1_000.0), RouteProfilePoint(1_000.0, 0.8))
        assertEquals("RouteProfilePoint(distanceKm=0.8, elevation=1000.0)", RouteProfilePoint(0.8, 1_000.0).toString())
    }
}
