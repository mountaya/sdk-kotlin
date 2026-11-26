package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class FormatNumberTest {
    @Test
    fun aNumberIsWrittenInTheLocalesOwnSeparators() {
        assertEquals("1,531", formatNumber(1_531.0, "en-US"))
        assertEquals("24,3", formatNumber(24.3, "fr-FR", maximumFractionDigits = 1))
        assertEquals("24.3", formatNumber(24.3, "en_US", maximumFractionDigits = 1))
    }

    @Test
    fun theValueIsRoundedHalfAwayFromZeroOnItsDecimalValueBeforeThePlatformSeesIt() {
        assertEquals("0.9", formatNumber(0.85, "en", maximumFractionDigits = 1))
        assertEquals("2.68", formatNumber(2.675, "en", maximumFractionDigits = 2))
        assertEquals("-3", formatNumber(-2.5, "en", maximumFractionDigits = 0))
        assertEquals("0", formatNumber(-0.3, "en", maximumFractionDigits = 0))
    }

    @Test
    fun threePlacesAreKeptByDefaultAndNoneThatIsNotNeeded() {
        assertEquals("1.235", formatNumber(1.23456, "en"))
        assertEquals("2", formatNumber(2.0, "en"))
        assertEquals("2", formatNumber(2.4, "en", maximumFractionDigits = -1))
    }

    @Test
    fun aValueThatIsNoFiniteNumberIsNothingAndALocaleThatIsNoTagIsEnglish() {
        assertEquals("", formatNumber(Double.NaN, "en"))
        assertEquals("", formatNumber(Double.POSITIVE_INFINITY, "en"))
        assertEquals("1,531", formatNumber(1_531.0, "not a tag"))
    }

    /**
     * CLDR groups no Spanish or Polish number under 10,000, Latin American Spanish from 1,000, as ICU writes
     * them on every platform.
     */
    @Test
    fun aNumberIsGroupedFromTheLocalesOwnMinimumGroupingDigits() {
        assertEquals("1234", formatNumber(1_234.0, "es"))
        assertEquals("12.345", formatNumber(12_345.0, "es-ES"))
        assertEquals("1531", formatNumber(1_531.0, "pl"))
        assertEquals("1531", formatNumber(1_531.0, "pl-PL"))
        assertEquals(6, formatNumber(15_310.0, "pl").length, "a Polish 15,310 is grouped")
        assertEquals("1,234", formatNumber(1_234.0, "es-MX"))
        assertEquals("1234,5", formatNumber(1_234.5, "es", maximumFractionDigits = 1))
        assertEquals("1.531", formatNumber(1_531.0, "de"))
    }
}
