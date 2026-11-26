package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TimeSliderTickTest {
    @Test
    fun twoTicksAreEqualWhereTheirStopAndTheirWordAre() {
        assertEquals(TimeSliderTick(71, "layers.timeNow"), TimeSliderTick(71, "layers.timeNow"))
        assertEquals(TimeSliderTick(47, null).hashCode(), TimeSliderTick(47, null).hashCode())
        assertNotEquals(TimeSliderTick(71, "layers.timeNow"), TimeSliderTick(71, null))
        assertNotEquals(TimeSliderTick(71, null), TimeSliderTick(95, null))
    }
}
