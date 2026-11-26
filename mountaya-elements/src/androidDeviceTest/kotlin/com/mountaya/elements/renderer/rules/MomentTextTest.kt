package com.mountaya.elements

import java.util.TimeZone
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * The moment a time slider's readout writes, through the platform's locale services on a device: the weekday, the day,
 * the month, the hour and the minute, in the reader's language and the zone named.
 */
class MomentTextTest {
    private val instant = Instant.parse("2026-03-14T09:26:53.900Z")

    @Test
    fun aMomentIsWrittenInTheReadersLanguageWithItsWeekdayDayMonthHourAndMinute() {
        val english = momentText(instant, "en-US", TimeZone.getTimeZone("UTC"))
        val french = momentText(instant, "fr-FR", TimeZone.getTimeZone("UTC"))

        assertTrue("Sat" in english && "Mar" in english && "14" in english && "26" in english, english)
        assertTrue("sam" in french && "mars" in french && "09:26" in french, french)
    }

    @Test
    fun aMomentIsWrittenInTheZoneNamed() {
        val utc = momentText(instant, "fr-FR", TimeZone.getTimeZone("UTC"))
        val paris = momentText(instant, "fr-FR", TimeZone.getTimeZone("Europe/Paris"))

        assertTrue("10:26" in paris, paris)
        assertNotEquals(utc, paris)
    }
}
