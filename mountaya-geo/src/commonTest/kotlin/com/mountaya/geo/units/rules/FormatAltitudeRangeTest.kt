package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatAltitudeRangeTest {
    private fun units(
        language: String,
        imperial: Boolean = false,
    ): UnitPreferences =
        UnitPreferences(
            language,
            length = if (imperial) RestSchema.UnitLength.Imperial else RestSchema.UnitLength.Metric,
        )

    @Test
    fun aBandIsTheLanguagesRangeTheUnitStatedOnce() {
        assertEquals("1,035\u20131,850 m", formatAltitudeRange(1_035.0, 1_850.0, units("en")))
        assertEquals("3,396\u20136,070 ft", formatAltitudeRange(1_035.0, 1_850.0, units("en", imperial = true)))
        assertEquals("1\u202F035\u20131\u202F850\u202Fm", formatAltitudeRange(1_035.0, 1_850.0, units("fr")))
        assertEquals("1,035\u20131,850 m", formatAltitudeRange(1_034.6, 1_850.4, units("en")))
        assertEquals("1035-1850 m", formatAltitudeRange(1_035.0, 1_850.0, units("es")))
        assertEquals("1,035\uFF5E1,850 m", formatAltitudeRange(1_035.0, 1_850.0, units("ja")))
        assertEquals("1,850\u20131,035 m", formatAltitudeRange(1_850.0, 1_035.0, units("en")))
    }

    @Test
    fun twoBoundsWrittenAlikeReadApproximatelyABoundRoundingToZeroAsZero() {
        assertEquals("~1,000 m", formatAltitudeRange(999.6, 1_000.4, units("en")))
        assertEquals("\u22431\u202F000\u202Fm", formatAltitudeRange(999.6, 1_000.4, units("fr")))
        assertEquals("~0 m", formatAltitudeRange(-0.4, 0.4, units("en")))
        assertEquals("\u2248-430 m", formatAltitudeRange(-430.0, -430.0, units("de")))
    }

    @Test
    fun aFirstBoundBelowZeroStandsApartFromTheSeparator() {
        assertEquals("-3 \u2013 1,200 m", formatAltitudeRange(-3.0, 1_200.0, units("en")))
        assertEquals("3\u2013-1,200 m", formatAltitudeRange(3.0, -1_200.0, units("en")))
    }

    @Test
    fun eitherBoundMissingOrNoFiniteNumberGivesNothing() {
        assertEquals("", formatAltitudeRange(null, 1_850.0, units("en")))
        assertEquals("", formatAltitudeRange(1_035.0, null, units("en")))
        assertEquals("", formatAltitudeRange(1_035.0, Double.NaN, units("fr")))
        assertEquals("", formatAltitudeRange(Double.POSITIVE_INFINITY, 1.0, units("en")))
    }
}
