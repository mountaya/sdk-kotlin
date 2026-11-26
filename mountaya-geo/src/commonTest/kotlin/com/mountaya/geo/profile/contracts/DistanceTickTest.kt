package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DistanceTickTest {
    @Test
    fun twoStopsAreEqualWhenTheyHoldTheSameNumbers() {
        assertEquals(DistanceTick(1.2, 1_200.0, 1), DistanceTick(1.2, 1_200.0, 1))
        assertEquals(DistanceTick(1.2, 1_200.0, 1).hashCode(), DistanceTick(1.2, 1_200.0, 1).hashCode())
        assertNotEquals(DistanceTick(1.2, 1_200.0, 1), DistanceTick(1.2, 1_200.0, 0))
        assertEquals(
            "DistanceTick(value=5.0, meters=8046.72, fractionDigits=0)",
            DistanceTick(5.0, 8_046.72, 0).toString(),
        )
    }
}
