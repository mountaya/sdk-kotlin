package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SlopeRampStopTest {
    @Test
    fun twoStopsAreEqualWhenTheyHoldTheSameColourAndDegrees() {
        assertEquals(SlopeRampStop("#66BB6A", 2.0), SlopeRampStop("#66BB6A", 2.0))
        assertEquals(SlopeRampStop("#66BB6A", 2.0).hashCode(), SlopeRampStop("#66BB6A", 2.0).hashCode())
        assertNotEquals(SlopeRampStop("#66BB6A", 2.0), SlopeRampStop("#66bb6a", 2.0))
        assertNotEquals(SlopeRampStop("#66BB6A", 0.0), SlopeRampStop("#66BB6A", -0.0))
        assertEquals("SlopeRampStop(color=#66BB6A, degrees=2.0)", SlopeRampStop("#66BB6A", 2.0).toString())
    }
}
