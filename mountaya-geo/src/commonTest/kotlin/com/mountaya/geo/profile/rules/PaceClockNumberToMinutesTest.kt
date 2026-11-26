package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PaceClockNumberToMinutesTest {
    @Test
    fun theHundredthsAreReadAsSeconds() {
        assertEquals(13.333333333333334, paceClockNumberToMinutes(13.2))
        assertEquals(4.083333333333333, paceClockNumberToMinutes(4.05))
    }

    @Test
    fun secondsPastFiftyNineCarryIntoTheMinutes() {
        assertEquals(14.0, paceClockNumberToMinutes(13.6))
        assertEquals(14.5, paceClockNumberToMinutes(13.9))
    }

    @Test
    fun aNumberThatIsNoPositiveFiniteNumberIsNoPace() {
        for (value in listOf(
            null,
            0.0,
            -13.2,
            Double.NaN,
            Double.POSITIVE_INFINITY,
        )) {
            assertNull(paceClockNumberToMinutes(value))
        }
    }
}
