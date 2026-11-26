package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class TimeSliderReadoutTest {
    private val window = timeSliderWindow(Instant.fromEpochMilliseconds(1773480413900))

    /** What the stop [value] reads, as `word count`. */
    private fun read(value: Int): String = timeSliderReadout(window, value).let { "${it.word} ${it.count}" }

    @Test
    fun theStopOfTheReadingReadsNowAndAnOffsetUnderADayItsWholeHours() {
        assertEquals("layers.timeNow null", read(71))
        assertEquals(Instant.fromEpochMilliseconds(1773480413900), timeSliderReadout(window, 71).instant)
        assertEquals("layers.timeHoursAgo 1", read(70))
        assertEquals("layers.timeHoursAhead 1", read(72))
        assertEquals("layers.timeHoursAgo 23", read(48))
        assertEquals("layers.timeHoursAhead 23", read(94))
    }

    @Test
    fun fromADayOnAnOffsetReadsItsWholeDaysTruncatedTowardTheReading() {
        assertEquals("layers.timeDaysAhead 1", read(95))
        assertEquals("layers.timeDaysAgo 1", read(47))
        assertEquals("layers.timeDaysAhead 1", read(118))
        assertEquals("layers.timeDaysAgo 1", read(24))
        assertEquals("layers.timeDaysAgo 2", read(23))
        assertEquals("layers.timeDaysAgo 2", read(0))
        assertEquals("layers.timeDaysAhead 6", read(215))
        assertEquals(Instant.fromEpochMilliseconds(1773998813900), timeSliderReadout(window, 215).instant)
    }

    @Test
    fun aStopThatStandsForNoInstantReadsItsOffsetAlone() {
        val none = timeSliderWindow(Instant.fromEpochMilliseconds(8_640_000_000_000_000))

        assertEquals(TimeSliderReadout(null, "layers.timeNow", null), timeSliderReadout(none, 71))
        assertEquals(TimeSliderReadout(null, "layers.timeHoursAhead", 9), timeSliderReadout(none, 80))
        assertEquals(TimeSliderReadout(null, "layers.timeDaysAgo", 2), timeSliderReadout(none, 0))
    }
}
