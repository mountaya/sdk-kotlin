package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class FormatDurationTest {
    private val english = UnitPreferences("en")

    @Test
    fun aDurationIsItsHoursAndMinutesEachWithItsNarrowUnit() {
        assertEquals("2h 35m", formatDuration(9_320.0, english))
        assertEquals("2h", formatDuration(7_170.0, english))
        assertEquals("1m", formatDuration(42.0, english))
        assertEquals("2h 35min", formatDuration(9_320.0, UnitPreferences("fr")))
        assertEquals("2小时 35分钟", formatDuration(9_320.0, UnitPreferences("zh")))
    }

    @Test
    fun noDurationOrOneUnderHalfAMinuteIsNothing() {
        for (seconds in listOf(
            null,
            0.0,
            -60.0,
            29.0,
            Double.NaN,
        )) {
            assertEquals("", formatDuration(seconds, english), "$seconds")
        }
    }
}
