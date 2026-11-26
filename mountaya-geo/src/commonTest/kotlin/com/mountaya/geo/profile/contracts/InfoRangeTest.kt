package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class InfoRangeTest {
    @Test
    fun twoRangesAreEqualWhenTheyHoldOneSpanAndEqualValues() {
        assertEquals(InfoRange(0, 2, "gravel"), InfoRange(0, 2, "gravel"))
        assertEquals(InfoRange(0, 2, "gravel").hashCode(), InfoRange(0, 2, "gravel").hashCode())
        assertNotEquals(InfoRange(0, 2, "gravel"), InfoRange(0, 2, "asphalt"))
        assertNotEquals(InfoRange(0, 2, "gravel"), InfoRange(0, 3, "gravel"))
        assertEquals("InfoRange(fromIndex=0, toIndex=2, value=gravel)", InfoRange(0, 2, "gravel").toString())
    }
}
