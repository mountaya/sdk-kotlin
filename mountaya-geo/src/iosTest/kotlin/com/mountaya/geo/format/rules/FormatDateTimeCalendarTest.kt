package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * An instant is written in the Gregorian calendar whatever calendar the locale keeps or its tag names, a
 * date alone or beside a time, and each target holds its own answer: this is iOS's, Foundation's
 * `NSDateFormatter` given the Gregorian calendar after its locale.
 */
class FormatDateTimeCalendarTest {
    private val piDay = Instant.parse("2026-03-14T00:00:00Z")

    @Test
    fun thaiIsWrittenInTheGregorianCalendarWithTheEraThatNamesIt() {
        assertEquals("14 มีนาคม ค.ศ. 2026", formatDateTime(piDay, "th-TH", "UTC"))
    }

    @Test
    fun aCalendarTheTagNamesIsNotRead() {
        assertEquals("14 มีนาคม ค.ศ. 2026", formatDateTime(piDay, "th-TH-u-ca-buddhist", "UTC"))
    }

    @Test
    fun aCalendarTheTagNamesIsNotReadBesideATime() {
        assertEquals(
            "14 มี.ค. 2026 00:00:00",
            formatDateTime(piDay, "th-TH-u-ca-buddhist", "UTC", DateLength.Medium, TimeLength.Medium),
        )
    }
}
