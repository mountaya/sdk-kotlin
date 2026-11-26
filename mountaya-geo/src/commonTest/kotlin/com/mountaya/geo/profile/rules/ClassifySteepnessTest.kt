package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ClassifySteepnessTest {
    @Test
    fun aSlopeFallsInTheLastClassItReaches() {
        assertEquals(SteepnessClass.From25To30, classifySteepness(25.0))
        assertEquals(SteepnessClass.From30To35, classifySteepness(30.0))
        assertEquals(SteepnessClass.From40To45, classifySteepness(44.999))
        assertEquals(SteepnessClass.From45, classifySteepness(60.0))
    }

    @Test
    fun aSlopeUnderTheFirstClassADescentOrNoNumberHasNone() {
        assertNull(classifySteepness(24.999))
        assertNull(classifySteepness(-30.0))
        assertNull(classifySteepness(Double.NaN))
        assertEquals("steepness", STEEPNESS_OVERLAY_SLUG)
    }
}
