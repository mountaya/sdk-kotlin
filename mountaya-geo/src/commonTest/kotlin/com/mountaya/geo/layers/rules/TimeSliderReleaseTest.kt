package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class TimeSliderReleaseTest {
    private val window = timeSliderWindow(Instant.fromEpochMilliseconds(1773480413900))

    @Test
    fun aReleaseOnAStopPinsItsInstantAndOnTheStopOfTheReadingPinsNone() {
        assertEquals(TimeSliderRelease(Instant.fromEpochMilliseconds(1773502013900)), timeSliderRelease(window, 77))
        assertEquals(TimeSliderRelease(null), timeSliderRelease(window, 71))
        assertEquals(TimeSliderRelease(Instant.fromEpochMilliseconds(1773224813900)), timeSliderRelease(window, 0))
        assertEquals(TimeSliderRelease(Instant.fromEpochMilliseconds(1773998813900)), timeSliderRelease(window, 215))
    }

    @Test
    fun aReleaseOnATrackOfNoInstantOrOffTheTrackCommitsNothing() {
        val none = timeSliderWindow(Instant.fromEpochMilliseconds(8_640_000_000_000_000))

        assertNull(timeSliderRelease(none, 80))
        assertNull(timeSliderRelease(none, 71))
        assertNull(timeSliderRelease(window, 216))
        assertNull(timeSliderRelease(window, -1))
    }
}
