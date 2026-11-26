package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class PreferencePressTest {
    private val held =
        UnitPreferences(
            language = "de",
            length = RestSchema.UnitLength.Imperial,
            pace = RestSchema.UnitPace.PerDistance,
            slope = RestSchema.UnitSlope.Percentage,
            temperature = RestSchema.UnitTemperature.Fahrenheit,
        )

    /** [held] with the language [language], every unit as it holds it. */
    private fun speaking(language: String): UnitPreferences =
        UnitPreferences(language, held.length, held.pace, held.slope, held.temperature)

    @Test
    fun aPressOnAUnitReportsTheSetWithThatPreferenceAloneChanged() {
        assertEquals(
            PreferencePress(
                UnitPreferences("de", RestSchema.UnitLength.Metric, held.pace, held.slope, held.temperature),
                null,
            ),
            preferencePress(held, PreferenceValue.Length(RestSchema.UnitLength.Metric)),
        )
        assertEquals(
            PreferencePress(
                UnitPreferences(
                    "de",
                    held.length,
                    held.pace,
                    held.slope,
                    RestSchema.UnitTemperature.Celsius,
                ),
                null,
            ),
            preferencePress(held, PreferenceValue.Temperature(RestSchema.UnitTemperature.Celsius)),
        )
    }

    @Test
    fun aPressOnALanguageReportsTheSetAndRelabelsTheMapInThatLanguage() {
        assertEquals(
            PreferencePress(speaking("fr"), RestSchema.Language.Fr),
            preferencePress(held, PreferenceValue.Language(RestSchema.Language.Fr)),
        )
    }

    @Test
    fun aPressOnALanguageTheMapDoesNotLabelInRelabelsItAsTheMapReadsIt() {
        assertEquals(
            PreferencePress(speaking("fr-CA"), RestSchema.Language.Fr),
            preferencePress(held, PreferenceValue.Language(RestSchema.Language("fr-CA"))),
        )
        assertEquals(
            PreferencePress(speaking("pt-BR"), RestSchema.Language.En),
            preferencePress(held, PreferenceValue.Language(RestSchema.Language("pt-BR"))),
        )
    }

    @Test
    fun aPressOnTheOptionTheSetHoldsReportsTheSetAsItStands() {
        assertEquals(
            PreferencePress(held, null),
            preferencePress(held, PreferenceValue.Slope(RestSchema.UnitSlope.Percentage)),
        )
    }
}
