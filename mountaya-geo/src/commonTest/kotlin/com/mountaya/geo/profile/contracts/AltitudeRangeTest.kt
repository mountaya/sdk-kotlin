package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AltitudeRangeTest {
    @Test
    fun twoBandsAreEqualWhereBothNumbersAreTheSame() {
        assertEquals(AltitudeRange(1_035.0, 1_850.0), AltitudeRange(1_035.0, 1_850.0))
        assertNotEquals(AltitudeRange(1_850.0, 1_035.0), AltitudeRange(1_035.0, 1_850.0))
        assertEquals(AltitudeRange(0.0, 1.0).hashCode(), AltitudeRange(0.0, 1.0).hashCode())
    }
}
