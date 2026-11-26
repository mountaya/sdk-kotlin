package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CurveStopTest {
    @Test
    fun twoStopsAreEqualWhereTheyStandAtOneNumberInOneColourOrNone() {
        assertEquals(CurveStop(0.2, null), CurveStop(0.2, null))
        assertNotEquals(CurveStop(0.2, "#f00"), CurveStop(0.2, null))
        assertNotEquals(CurveStop(-0.0, null), CurveStop(0.0, null))
        assertEquals(CurveStop(0.4, "#f00").hashCode(), CurveStop(0.4, "#f00").hashCode())
    }
}
