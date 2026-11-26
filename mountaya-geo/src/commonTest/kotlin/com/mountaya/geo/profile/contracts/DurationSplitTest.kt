package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DurationSplitTest {
    @Test
    fun twoSplitsAreEqualWhenTheyHoldTheSameHoursAndMinutes() {
        assertEquals(DurationSplit(2, 35), DurationSplit(2, 35))
        assertEquals(DurationSplit(2, 35).hashCode(), DurationSplit(2, 35).hashCode())
        assertNotEquals(DurationSplit(2, 35), DurationSplit(35, 2))
        assertEquals("DurationSplit(hours=2, minutes=35)", DurationSplit(2, 35).toString())
    }
}
