package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AxisLabelBoxTest {
    @Test
    fun twoBoxesAreEqualWhereTheKeysAreWrittenAlikeAndTheEdgesAreTheSameNumbers() {
        assertEquals(AxisLabelBox("a", 0.0, 60.0), AxisLabelBox("a", 0.0, 60.0))
        assertEquals(AxisLabelBox("a", 0.0, 60.0).hashCode(), AxisLabelBox("a", 0.0, 60.0).hashCode())
        assertNotEquals(AxisLabelBox("b", 0.0, 60.0), AxisLabelBox("a", 0.0, 60.0))
        assertNotEquals(AxisLabelBox("a", -0.0, 60.0), AxisLabelBox("a", 0.0, 60.0))
    }
}
