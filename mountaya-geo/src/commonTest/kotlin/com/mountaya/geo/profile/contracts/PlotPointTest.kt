package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PlotPointTest {
    @Test
    fun twoPointsAreEqualWhereBothNumbersAreTheSame() {
        assertEquals(PlotPoint(50.0, 51.0), PlotPoint(50.0, 51.0))
        assertEquals(PlotPoint(50.0, 51.0).hashCode(), PlotPoint(50.0, 51.0).hashCode())
        assertNotEquals(PlotPoint(-0.0, 51.0), PlotPoint(0.0, 51.0))
        assertEquals(PlotPoint(Double.NaN, 51.0), PlotPoint(Double.NaN, 51.0))
    }
}
