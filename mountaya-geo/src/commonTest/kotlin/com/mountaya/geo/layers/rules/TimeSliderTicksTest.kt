package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class TimeSliderTicksTest {
    private val ticks =
        listOf(
            TimeSliderTick(23, "layers.timeMinus2d"),
            TimeSliderTick(47, null),
            TimeSliderTick(71, "layers.timeNow"),
            TimeSliderTick(95, null),
            TimeSliderTick(119, "layers.timePlus2d"),
            TimeSliderTick(143, null),
            TimeSliderTick(167, null),
            TimeSliderTick(191, "layers.timePlus5d"),
            TimeSliderTick(215, null),
        )

    @Test
    fun theStopOfTheReadingAndEveryWholeDayEitherSideAreTickedFourOfThemLabelled() {
        assertEquals(ticks, timeSliderTicks(timeSliderWindow(Instant.fromEpochMilliseconds(1773480413900))))
    }

    @Test
    fun aTrackOfNoInstantIsTickedTheSame() {
        assertEquals(ticks, timeSliderTicks(timeSliderWindow(Instant.fromEpochMilliseconds(8_640_000_000_000_000))))
    }
}
