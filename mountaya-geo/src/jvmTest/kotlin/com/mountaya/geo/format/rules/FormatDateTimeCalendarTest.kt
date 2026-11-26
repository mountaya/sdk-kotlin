package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * An instant is written in the Gregorian calendar whatever calendar the locale keeps or its tag names, a
 * date alone or beside a time, and each target holds its own answer: this is the jvm's, `java.text` over a
 * locale read with `ca=gregory`.
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
        assertEquals("2026年3月14日", formatDateTime(piDay, "ja-JP-u-ca-japanese", "UTC"))
    }

    @Test
    fun aCalendarTheTagNamesIsNotReadBesideATime() {
        assertEquals(
            "14 มี.ค. 2026 00:00:00",
            formatDateTime(piDay, "th-TH-u-ca-buddhist", "UTC", DateLength.Medium, TimeLength.Medium),
        )
    }

    @Test
    fun persianIsTheGregorianDateInPersiansWordsAndDigits() {
        assertEquals("۱۴ مارس ۲۰۲۶", formatDateTime(piDay, "fa-IR", "UTC"))
    }
}
