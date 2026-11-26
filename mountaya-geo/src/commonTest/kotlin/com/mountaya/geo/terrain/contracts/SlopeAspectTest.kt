package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SlopeAspectTest {
    /** Flat ground's null aspect is not a north-facing slope's 0. */
    @Test
    fun noAspectIsNotABearingOfZero() {
        assertNotEquals(SlopeAspect(0.0, null), SlopeAspect(0.0, 0.0))
        assertEquals(SlopeAspect(0.0, null), SlopeAspect(0.0, null))
        assertEquals(SlopeAspect(5.0, 90.0).hashCode(), SlopeAspect(5.0, 90.0).hashCode())
        assertEquals("SlopeAspect(slope=5.0, aspect=null)", SlopeAspect(5.0, null).toString())
    }

    /**
     * Equal values hash alike: a slope of -0 is another value than one of 0, and NaN is the same
     * as itself, as every value of the geo layer compares.
     */
    @Test
    fun twoAnswersCompareTheirNumbersAsTheSameNumbers() {
        assertEquals(SlopeAspect(Double.NaN, Double.NaN), SlopeAspect(Double.NaN, Double.NaN))
        assertNotEquals(SlopeAspect(0.0, null), SlopeAspect(-0.0, null))
        assertNotEquals(SlopeAspect(5.0, 0.0), SlopeAspect(5.0, -0.0))
        assertNotEquals(SlopeAspect(5.0, Double.NaN), SlopeAspect(5.0, null))
    }
}
