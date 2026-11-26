package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * An instant is written in the Gregorian calendar whatever calendar the locale keeps or its tag names, a
 * date alone or beside a time, and each target holds its own answer: this is android's, `android.icu` over
 * a Gregorian calendar and a locale read with `ca=gregory`. Its words and digits are the device's ICU data,
 * which moves with the release, so the year is what is held: Gregorian, and never the Buddhist one.
 */
class FormatDateTimeCalendarTest {
    private val piDay = Instant.parse("2026-03-14T00:00:00Z")

    @Test
    fun thaiIsWrittenInTheGregorianCalendarADateAloneOrBesideATime() {
        for (tag in listOf("th-TH", "th-TH-u-ca-buddhist")) {
            for (written in listOf(
                formatDateTime(piDay, tag, "UTC"),
                formatDateTime(piDay, tag, "UTC", DateLength.Medium, TimeLength.Medium),
            )) {
                val text = written.orEmpty()
                assertTrue("2026" in text, "$tag: $text")
                assertFalse("2569" in text, "$tag: $text")
            }
        }
    }
}
