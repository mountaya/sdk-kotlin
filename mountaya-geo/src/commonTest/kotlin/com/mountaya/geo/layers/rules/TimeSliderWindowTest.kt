package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Instant

class TimeSliderWindowTest {
    @Test
    fun theTrackRunsSeventyOneHoursBackAndOneHundredFortyFourAhead() {
        val now = Instant.fromEpochMilliseconds(1773480413900)
        val window = timeSliderWindow(now)

        assertEquals(71, TIME_WINDOW_PAST_HOURS)
        assertEquals(144, TIME_WINDOW_FUTURE_HOURS)
        assertEquals(now - 71.hours, window.start)
        assertEquals(now + 144.hours, window.dateAt(window.maxValue))
    }

    @Test
    fun aTrackWithAnEndPastTheRangeEverySdkHoldsHoldsNoInstant() {
        val last = Instant.fromEpochMilliseconds(8_640_000_000_000_000)
        val first = Instant.fromEpochMilliseconds(-8_640_000_000_000_000)

        assertEquals(last, timeSliderWindow(last - 144.hours).dateAt(215))
        assertNull(timeSliderWindow(last - 144.hours + 1.nanoseconds).start)
        assertNull(timeSliderWindow(last).start)
        assertEquals(first, timeSliderWindow(first + 71.hours).start)
        assertNull(timeSliderWindow(first + 71.hours - 1.nanoseconds).start)
        assertEquals(
            -8_639_999_226_000_000,
            timeSliderWindow(Instant.fromEpochMilliseconds(-8_639_999_744_400_000)).dateAt(215)?.toEpochMilliseconds(),
        )
    }
}
