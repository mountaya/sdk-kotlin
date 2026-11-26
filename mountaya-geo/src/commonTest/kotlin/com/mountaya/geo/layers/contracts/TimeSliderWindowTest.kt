package com.mountaya.geo.layers.contracts

import com.mountaya.geo.TimeSliderWindow
import com.mountaya.geo.timeSliderWindow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class TimeSliderWindowTest {
    private val now = Instant.fromEpochMilliseconds(1773480413900)
    private val window: TimeSliderWindow = timeSliderWindow(now)

    @Test
    fun aStopStandsForItsHourFromTheStartAndAValueOffTheTrackForNone() {
        assertEquals(71, window.nowValue)
        assertEquals(215, window.maxValue)
        assertEquals(Instant.fromEpochMilliseconds(1773224813900), window.dateAt(0))
        assertEquals(now, window.dateAt(71))
        assertEquals(Instant.fromEpochMilliseconds(1773998813900), window.dateAt(215))
        assertNull(window.dateAt(-1))
        assertNull(window.dateAt(216))
    }

    @Test
    fun anInstantIsPlacedAtItsNearestStopAHalfHourAtTheLaterHeldToTheTrack() {
        fun at(written: String): Int = window.valueAt(Instant.parse(written))

        assertEquals(71, window.valueAt(null))
        assertEquals(71, at("2026-03-14T09:26:53.900Z"))
        assertEquals(72, at("2026-03-14T10:00:00Z"))
        assertEquals(71, at("2026-03-14T09:00:00Z"))
        assertEquals(131, at("2026-03-16T21:05:00Z"))
        assertEquals(72, at("2026-03-14T09:56:53.900Z"))
        assertEquals(71, at("2026-03-14T08:56:53.900Z"))
        assertEquals(71, at("2026-03-14T09:56:53.899999999Z"))
        assertEquals(73, at("2026-03-14T16:26:53.900+05:00"))
        assertEquals(0, at("2026-03-10T00:00:00Z"))
        assertEquals(0, at("2026-03-11T10:56:53.899Z"))
        assertEquals(215, at("2026-03-25T00:00:00Z"))
    }

    @Test
    fun aTrackOfNoInstantPlacesEveryInstantAtNowAndStandsForNone() {
        val none = timeSliderWindow(Instant.fromEpochMilliseconds(8_640_000_000_000_000))

        assertNull(none.start)
        assertEquals(71, none.valueAt(Instant.parse("2026-03-14T09:26:53.900Z")))
        assertNull(none.dateAt(0))
        assertEquals(215, none.maxValue)
    }

    @Test
    fun twoWindowsAreEqualWhereTheyStartAtOneInstant() {
        assertEquals(window, timeSliderWindow(now))
        assertEquals(window.hashCode(), timeSliderWindow(now).hashCode())
        assertNotEquals(window, timeSliderWindow(Instant.fromEpochMilliseconds(1773480413901)))
        val far = Instant.fromEpochMilliseconds(8_640_000_000_000_000)
        assertEquals(timeSliderWindow(far), timeSliderWindow(far + 1.days))
    }
}
