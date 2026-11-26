package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PaceMinutesToClockNumberTest {
    @Test
    fun theSecondsAreCarriedAsHundredths() {
        assertEquals(13.2, paceMinutesToClockNumber(13.333333333333334))
        assertEquals(4.05, paceMinutesToClockNumber(4.083333333333333))
        assertEquals(14.0, paceMinutesToClockNumber(13.999))
    }

    @Test
    fun aPaceTheClockStatesNothingForIsNoNumber() {
        for (minutes in listOf(null, 0.0, -1.0, 0.005, Double.NaN)) assertNull(paceMinutesToClockNumber(minutes))
    }
}
