package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class ParseRetryAfterTest {
    /** 2027-01-15T08:00:00Z. */
    private val now = Instant.fromEpochMilliseconds(1_800_000_000_000)

    /** RFC 9110's own example date, 1994-11-06T08:49:37Z, and an instant ten seconds before it. */
    private val example = Instant.fromEpochSeconds(784_111_777)
    private val beforeExample = Instant.fromEpochSeconds(784_111_767)

    @Test
    fun aCountOfSecondsIsTheWait() {
        assertEquals(30.seconds, parseRetryAfter("30", now))
        assertEquals(Duration.ZERO, parseRetryAfter("0", now))
        assertEquals(30.seconds, parseRetryAfter("030", now))
    }

    @Test
    fun theWhitespaceAroundAFieldValueIsNotPartOfIt() {
        assertEquals(30.seconds, parseRetryAfter(" 30\t", now))
    }

    @Test
    fun aCountIsDigitsAndNothingElse() {
        for (header in listOf("-5", "+5", "3.5", "1e3", "30s", "", " ", "not-a-number", "99999999999999999999")) {
            assertNull(parseRetryAfter(header, now), "\"$header\"")
        }
    }

    @Test
    fun anHttpDateIsTheWaitUntilIt() {
        assertEquals(60.seconds, parseRetryAfter("Fri, 15 Jan 2027 08:01:00 GMT", now))
        assertEquals(10.seconds, parseRetryAfter("Sun, 06 Nov 1994 08:49:37 GMT", beforeExample))
    }

    @Test
    fun aDateIsWaitedForInWholeSecondsRoundedUpSoNoRepeatComesBackEarly() {
        assertEquals(10.seconds, parseRetryAfter("Sun, 06 Nov 1994 08:49:37 GMT", beforeExample + 500.milliseconds))
        assertEquals(1.seconds, parseRetryAfter("Sun, 06 Nov 1994 08:49:37 GMT", example - 1.milliseconds))
        assertEquals(4.seconds, parseRetryAfter("Sun, 06 Nov 1994 08:49:37 GMT", example - 3_500.milliseconds))
    }

    @Test
    fun aDateAlreadyPastAsksForNoWait() {
        assertEquals(Duration.ZERO, parseRetryAfter("Thu, 14 Jan 2027 08:00:00 GMT", now))
        assertEquals(Duration.ZERO, parseRetryAfter("Sun, 06 Nov 1994 08:49:37 GMT", example))
    }

    @Test
    fun theTwoObsoleteDateFormatsAreReadToo() {
        assertEquals(10.seconds, parseRetryAfter("Sunday, 06-Nov-94 08:49:37 GMT", beforeExample))
        assertEquals(10.seconds, parseRetryAfter("Sun Nov  6 08:49:37 1994", beforeExample))
        assertEquals(60.seconds, parseRetryAfter("Fri Jan 15 08:01:00 2027", now))
        assertEquals(60.seconds, parseRetryAfter("Friday, 15-Jan-27 08:01:00 GMT", now))
        assertEquals(2.days, parseRetryAfter("Sun Jan 17 08:00:00 2027", now))
        // The space pads a one-digit day only, so two spaces before two digits are neither format.
        assertNull(parseRetryAfter("Sat Jan  16 08:00:00 2027", now))
    }

    @Test
    fun aTwoDigitYearIsTheOneNoMoreThanFiftyYearsAhead() {
        // Fifty years from 2027-01-15 holds thirteen leap days, 2028 to 2076.
        assertEquals((50 * 365 + 13).days, parseRetryAfter("Friday, 15-Jan-77 08:00:00 GMT", now))
        // 2078 would be more than fifty years ahead, so the year is 1978, which is past.
        assertEquals(Duration.ZERO, parseRetryAfter("Sunday, 15-Jan-78 08:00:00 GMT", now))
    }

    @Test
    fun aComponentOutOfRangeIsNeitherSpelling() {
        val headers =
            listOf(
                "Mon, 29 Feb 2027 08:00:00 GMT",
                "Fri, 32 Jan 2027 08:00:00 GMT",
                "Fri, 15 Jan 2027 24:00:00 GMT",
                "Fri, 15 Jan 2027 08:60:00 GMT",
                "Fri, 15 Jan 2027 08:00:61 GMT",
            )
        for (header in headers) assertNull(parseRetryAfter(header, now), header)
    }

    @Test
    fun aLeapDayAndALeapSecondAreDates() {
        // From 2027-01-15T08:00Z: sixteen hours to midnight, 350 days to the new year, 59 to 29 February.
        assertEquals(409.days + 16.hours, parseRetryAfter("Tue, 29 Feb 2028 00:00:00 GMT", now))
        assertEquals(60.seconds, parseRetryAfter("Fri, 15 Jan 2027 08:00:60 GMT", now))
    }

    @Test
    fun aDateIsSpelledExactlyAsTheFormatSpellsIt() {
        val headers =
            listOf(
                "fri, 15 jan 2027 08:01:00 gmt",
                "fri, 15 Jan 2027 08:01:00 GMT",
                "Fri, 15 jan 2027 08:01:00 GMT",
                "Fri, 15 Jan 2027 08:01:00 UTC",
                "Fri, 15 Jan 2027 08:01:00 +0000",
                "Fri, 5 Jan 2027 08:01:00 GMT",
                "Fri, 15 Jan 27 08:01:00 GMT",
                "Fri,  15 Jan 2027 08:01:00 GMT",
                "Fri,15 Jan 2027 08:01:00 GMT",
                "Fri, 15-Jan-27 08:01:00 GMT",
                "Friday, 15-Jan-2027 08:01:00 GMT",
                "Friday, 15 Jan 2027 08:01:00 GMT",
                "Fri Jan 15 08:01:00 27",
                "Fri Jan 15 08:01:00 2027 GMT",
                "2027-01-15T08:01:00Z",
            )
        for (header in headers) assertNull(parseRetryAfter(header, now), header)
    }
}
