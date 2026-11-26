package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CurveColorTest {
    /** A text compares by its code units: a precomposed e acute and e with a combining acute are two colours. */
    @Test
    fun twoStretchesAreEqualWhereBothEndsAreTheSameNumbersAndTheColoursAlike() {
        val stretch = CurveColor(200.0, 400.0, "#f00")

        assertEquals(CurveColor(200.0, 400.0, "#f00"), stretch)
        assertEquals(CurveColor(200.0, 400.0, "#f00").hashCode(), stretch.hashCode())
        assertNotEquals(CurveColor(200.0, 400.0, "#F00"), stretch)
        assertEquals(CurveColor(0.0, 1.0, "\u00E9"), CurveColor(0.0, 1.0, "\u00E9"))
        assertNotEquals(CurveColor(0.0, 1.0, "e\u0301"), CurveColor(0.0, 1.0, "\u00E9"))
    }
}
