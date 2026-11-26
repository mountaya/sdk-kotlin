package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SplitDurationTest {
    @Test
    fun theTotalIsRoundedToWholeMinutesBeforeItIsSplit() {
        assertEquals(DurationSplit(2, 35), splitDuration(9_320.0))
        assertEquals(DurationSplit(1, 59), splitDuration(7_169.0))
        assertEquals(DurationSplit(2, 0), splitDuration(7_170.0))
        assertEquals(DurationSplit(1, 0), splitDuration(3_599.0))
    }

    @Test
    fun aHalfMinuteRoundsUpAndAnyLessToNoMinute() {
        assertEquals(DurationSplit(0, 3), splitDuration(150.0))
        assertEquals(DurationSplit(0, 1), splitDuration(30.0))
        assertEquals(DurationSplit(0, 0), splitDuration(29.0))
        assertEquals(DurationSplit(0, 0), splitDuration(29.999999999999996))
    }

    @Test
    fun hoursAreNeverCarriedIntoDays() {
        assertEquals(DurationSplit(24, 0), splitDuration(86_400.0))
        assertEquals(DurationSplit(100, 1), splitDuration(360_060.0))
    }

    @Test
    fun aMissingZeroNegativeOrNonFiniteDurationIsNoDuration() {
        for (seconds in listOf(null, 0.0, -0.0, -60.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertNull(splitDuration(seconds), "$seconds")
        }
    }

    @Test
    fun aHalfIsReadExactly() {
        assertEquals(1.0, roundHalfUp(0.5))
        assertEquals(0.0, roundHalfUp(0.49999999999999994))
        assertEquals(3.0, roundHalfUp(2.5))
        assertEquals(2.0, roundHalfUp(2.0))
    }
}
