package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class LocaleNumberTextTest {
    @Test
    fun aNumberIsWrittenInTheLocaleItNames() {
        assertEquals("1,531", localeNumberText(1_531.0, "en-US", 0, grouping = true))
        assertEquals("24,3", localeNumberText(24.3, "fr-FR", 1, grouping = true))
        assertEquals("-22", localeNumberText(-22.0, "en", 0, grouping = true))
    }

    @Test
    fun groupingIsAsked() {
        assertEquals("1531", localeNumberText(1_531.0, "en-US", 0, grouping = false))
    }

    @Test
    fun noFractionDigitIsWrittenPastTheOnesAskedOrWhereNoneIsNeeded() {
        assertEquals("0.9", localeNumberText(0.9, "en", 1, grouping = true))
        assertEquals("100", localeNumberText(100.0, "en", 1, grouping = true))
        assertEquals("0", localeNumberText(0.0, "en", 2, grouping = true))
    }

    /** A figure stated to a fixed place keeps its zero, in the locale's own digits and mark. */
    @Test
    fun aMinimumOfFractionDigitsIsAlwaysWritten() {
        assertEquals("1.0", localeNumberText(1.0, "en-US", 1, grouping = false, minimumFractionDigits = 1))
        assertEquals("1,0", localeNumberText(1.0, "fr-FR", 1, grouping = false, minimumFractionDigits = 1))
        assertEquals("1", localeNumberText(1.0, "en-US", 1, grouping = false))
    }

    /**
     * Foundation's own reading of each tag drops its numbering system and writes nothing; the
     * iOS seam reads the tag's components instead, so every target writes the digits named.
     */
    @Test
    fun aNumberingSystemTheTagNamesIsTheOneWritten() {
        assertEquals("১,৫৩১", localeNumberText(1_531.0, "bn-BD-u-nu-beng", 0, grouping = true))
        assertEquals("১,২৩৪.৫", localeNumberText(1_234.5, "bn-BD-u-nu-beng", 1, grouping = true))
        assertEquals("١٬٥٣١", localeNumberText(1_531.0, "ar-EG-u-nu-arab", 0, grouping = true))
        assertEquals("١٬٢٣٤٫٥", localeNumberText(1_234.5, "ar-EG-u-nu-arab", 1, grouping = true))
        assertEquals("१,५३१", localeNumberText(1_531.0, "hi-IN-u-nu-deva", 0, grouping = true))
        assertEquals("१,२३४.५", localeNumberText(1_234.5, "hi-IN-u-nu-deva", 1, grouping = true))
    }

    @Test
    fun aNumberingSystemThePlatformDoesNotCarryWritesTheLocalesOwnDigits() {
        assertEquals("1,531", localeNumberText(1_531.0, "en-US-u-nu-zzzz", 0, grouping = true))
        assertEquals("1,234.5", localeNumberText(1_234.5, "en-US-u-nu-zzzz", 1, grouping = true))
        assertEquals("1,531", localeNumberText(1_531.0, "en-u-nu-xxxx", 0, grouping = true))
    }
}
