package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class UnitPatternsTest {
    @Test
    fun theTableHoldsTheSevenLanguagesTheContractsLocalesAreKeyedBy() {
        assertEquals(setOf("de", "en", "es", "fr", "it", "ja", "zh"), unitPatternLanguages)
    }

    @Test
    fun aLanguageIsReadByItsPrimarySubtagAndAnyOtherWritesEnglish() {
        assertSame(unitPatterns("fr"), unitPatterns("FR_ca"))
        assertSame(unitPatterns("en"), unitPatterns("frr"))
        assertSame(unitPatterns("en"), unitPatterns("ar-EG"))
        assertSame(unitPatterns("en"), unitPatterns("not a tag"))
    }

    @Test
    fun eachLanguageSpacesItsUnitItsOwnWay() {
        assertEquals("{0} km", unitPatterns("en").kilometer)
        assertEquals("{0} km", unitPatterns("fr").kilometer)
        assertEquals("{0}°C", unitPatterns("en").celsius)
        assertEquals("{0} °C", unitPatterns("fr").celsius)
        assertEquals("{0} min/mi", unitPatterns("fr").minutePerMile)
        assertEquals("{0}公里", unitPatterns("zh").kilometer)
        assertEquals("{0} mph", unitPatterns("en").milePerHour)
        assertEquals("{0} mi/h", unitPatterns("fr").milePerHour)
    }

    @Test
    fun everyPatternHoldsTheNumberOnce() {
        for (language in unitPatternLanguages) {
            val row = unitPatterns(language)
            val patterns =
                listOf(
                    row.kilometer,
                    row.mile,
                    row.meter,
                    row.foot,
                    row.percent,
                    row.kilometerPerHour,
                    row.milePerHour,
                    row.minutePerKilometer,
                    row.minutePerMile,
                    row.celsius,
                    row.fahrenheit,
                    row.hour,
                    row.minute,
                )
            for (pattern in patterns) assertEquals(1, pattern.split("{0}").size - 1, "$language: `$pattern`")
            assertEquals(if (language == "es" || language == "it") 2 else 1, row.minimumGroupingDigits, language)
            assertEquals(minimumGroupingDigitsOf(language), row.minimumGroupingDigits, "$language is CLDR's own")
        }
    }
}
