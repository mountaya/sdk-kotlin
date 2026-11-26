package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FormatPaceClockTest {
    @Test
    fun aPaceIsAClockOfMinutesAndTwoDigitSeconds() {
        assertEquals("13:20", formatPaceClock(13.333333333333334))
        assertEquals("4:05", formatPaceClock(4.083333333333333))
        assertEquals("60:00", formatPaceClock(60.0))
        assertEquals("125:30", formatPaceClock(125.5))
    }

    @Test
    fun theSecondsAreRoundedBeforeTheSplitSoNoneReadsSixty() {
        assertEquals("14:00", formatPaceClock(13.999))
        assertEquals("0:01", formatPaceClock(0.01))
    }

    @Test
    fun aPaceThatIsNoPositiveNumberOrRoundsToNoSecondIsNothing() {
        for (minutes in listOf(null, 0.0, -4.0, 0.005, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertEquals("", formatPaceClock(minutes), "$minutes")
        }
        assertNull(clockSeconds(0.008))
        assertEquals(1L, clockSeconds(0.0084))
    }
}
