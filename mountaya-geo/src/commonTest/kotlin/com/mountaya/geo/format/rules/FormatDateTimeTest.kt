package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class FormatDateTimeTest {
    private val piDay = Instant.parse("2026-03-14T00:00:00Z")

    @Test
    fun aDateIsWrittenInTheLocaleAtTheLengthAskedAndAtLongWhereNoLengthIsNamed() {
        assertEquals("March 14, 2026", formatDateTime(piDay, "en-US", "UTC"))
        assertEquals("14 mars 2026", formatDateTime(piDay, "fr_FR", "UTC", DateLength.Long))
        assertEquals("3/14/26", formatDateTime(piDay, "en-US", "UTC", dateLength = DateLength.Short))
    }

    /** Midnight in UTC is 13:00 in Auckland: German keeps a 24-hour clock, Korean a 12-hour one. */
    @Test
    fun aTimeOfDayAloneIsWrittenOnTheLocalesOwnClock() {
        assertEquals("13:00", formatDateTime(piDay, "de-DE", "Pacific/Auckland", timeLength = TimeLength.Short))
        assertEquals("오후 1:00", formatDateTime(piDay, "ko-KR", "Pacific/Auckland", timeLength = TimeLength.Short))
        assertEquals("9:00:00", formatDateTime(piDay, "ja-JP", "Asia/Tokyo", timeLength = TimeLength.Medium))
        assertEquals("०५:४५", formatDateTime(piDay, "ne-NP", "Asia/Kathmandu", timeLength = TimeLength.Short))
    }

    /** German joins a medium date and a time with a comma, which android's java.text would join with one space. */
    @Test
    fun aDateWithATimeIsTheZonesDateJoinedAsTheLocaleJoinsThem() {
        assertEquals(
            "2026年3月13日 20:00",
            formatDateTime(piDay, "zh-CN", "America/New_York", DateLength.Medium, TimeLength.Short),
        )
        assertEquals(
            "13.03.2026, 20:00",
            formatDateTime(piDay, "de-DE", "America/New_York", DateLength.Medium, TimeLength.Short),
        )
    }

    @Test
    fun theDayIsTheZonesAndAZoneThePlatformDoesNotKnowWritesNothing() {
        assertEquals("March 13, 2026", formatDateTime(piDay, "en-US", "America/Los_Angeles"))
        assertNull(formatDateTime(piDay, "en-US", "Mars/Olympus_Mons"))
        assertNull(formatDateTime(piDay, "en-US", "Mars/Olympus_Mons", timeLength = TimeLength.Short))
    }
}
