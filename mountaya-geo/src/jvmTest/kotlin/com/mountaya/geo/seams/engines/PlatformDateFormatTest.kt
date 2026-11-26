package com.mountaya.geo

import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * The jvm writes an instant through `java.text`, which reads no setting of the machine it runs on, so a time
 * keeps the locale's own clock, and joins a date and a time as the JDK's data joins the shorter of their two
 * lengths.
 */
class PlatformDateFormatTest {
    /** 2026-03-14T00:00:00Z: 13:00 in Auckland, 05:45 in Kathmandu. */
    private val piDay = Date(1_773_446_400_000L)

    private fun written(
        tag: String,
        timeZone: String,
        dateLength: DateLength?,
        timeLength: TimeLength?,
    ): String {
        val format = platformDateFormat(gregorian(Locale.forLanguageTag(tag)), timeZone, dateLength, timeLength)
        assertIs<DateFormat>(format)
        return format.format(piDay)
    }

    @Test
    fun aTimeIsWrittenOnTheLocalesOwnClock() {
        assertEquals("오후 1:00", written("ko-KR", "Pacific/Auckland", null, TimeLength.Short))
        assertEquals("13:00:00", written("de-DE", "Pacific/Auckland", null, TimeLength.Medium))
    }

    @Test
    fun neitherLengthNamedWritesTheLongDate() {
        assertEquals("14 mars 2026", written("fr-FR", "Pacific/Auckland", null, null))
    }

    /**
     * ICU joins a French medium date and a time with a comma, the join of a medium date; the JDK joins them as
     * a short date is joined, with a space, and the fixture holds no case where the two differ.
     */
    @Test
    fun theJdkJoinsADateAndATimeAsItJoinsTheShorterOfTheirLengths() {
        assertEquals("14 mars 2026 05:45", written("fr-FR", "Asia/Kathmandu", DateLength.Medium, TimeLength.Short))
        assertEquals("14/03/2026 05:45:00", written("fr-FR", "Asia/Kathmandu", DateLength.Short, TimeLength.Medium))
    }
}
