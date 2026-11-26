package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocaleDateTextTest {
    /** 2026-03-14T00:00:00Z. */
    private val piDay = 1_773_446_400_000L

    @Test
    fun aDateIsWrittenInTheLocaleAndTheZoneItNames() {
        assertEquals("March 14, 2026", localeDateText(piDay, "en-US", "UTC", DateLength.Long, null))
        assertEquals("14 mars 2026", localeDateText(piDay, "fr-FR", "UTC", DateLength.Long, null))
        // Midnight in UTC is the evening before in New York.
        assertEquals("March 13, 2026", localeDateText(piDay, "en-US", "America/New_York", DateLength.Long, null))
    }

    @Test
    fun neitherLengthNamedWritesTheLongDate() {
        assertEquals("March 14, 2026", localeDateText(piDay, "en-US", "UTC", null, null))
    }

    /** New York keeps daylight saving time from 8 March 2026, and Kathmandu is 5:45 ahead of UTC. */
    @Test
    fun aTimeIsTheZonesWallClockInTheLocalesDigits() {
        assertEquals("20:00", localeDateText(piDay, "de-DE", "America/New_York", null, TimeLength.Short))
        assertEquals("०५:४५", localeDateText(piDay, "ne-NP", "Asia/Kathmandu", null, TimeLength.Short))
    }

    @Test
    fun aDateWithATimeIsJoinedAsTheLocaleJoinsADateOfThatLengthInTheGregorianCalendar() {
        assertEquals(
            "14/03/2026 05:45:00",
            localeDateText(piDay, "fr-FR", "Asia/Kathmandu", DateLength.Short, TimeLength.Medium),
        )
        assertEquals(
            "14 มี.ค. 2026 00:00:00",
            localeDateText(piDay, "th-TH-u-ca-buddhist", "UTC", DateLength.Medium, TimeLength.Medium),
        )
    }

    @Test
    fun aZoneThePlatformDoesNotKnowWritesNoDateAndNoTime() {
        assertNull(localeDateText(piDay, "en-US", "Not/AZone", DateLength.Long, null))
        assertNull(localeDateText(piDay, "en-US", "Not/AZone", null, TimeLength.Short))
        assertNull(localeDateText(piDay, "en-US", "Not/AZone", DateLength.Medium, TimeLength.Short))
    }

    /**
     * Foundation's own reading of each tag drops its numbering system and writes no date; the iOS
     * seam reads the tag's components instead, so every target writes the digits named.
     */
    @Test
    fun aNumberingSystemTheTagNamesIsTheOneWritten() {
        assertEquals("১৪ মার্চ, ২০২৬", localeDateText(piDay, "bn-BD-u-nu-beng", "UTC", DateLength.Long, null))
        assertEquals("१४ मार्च २०२६", localeDateText(piDay, "hi-IN-u-nu-deva", "UTC", DateLength.Long, null))
        // Egyptian Arabic writes its own digits already, so naming them writes the locale's own date.
        assertEquals(
            localeDateText(piDay, "ar-EG", "UTC", DateLength.Long, null),
            localeDateText(piDay, "ar-EG-u-nu-arab", "UTC", DateLength.Long, null),
        )
    }

    @Test
    fun aNumberingSystemThePlatformDoesNotCarryWritesTheLocalesOwnDigits() {
        assertEquals("March 14, 2026", localeDateText(piDay, "en-US-u-nu-zzzz", "UTC", DateLength.Long, null))
        assertEquals("March 14, 2026", localeDateText(piDay, "en-u-nu-xxxx", "UTC", DateLength.Long, null))
    }
}
