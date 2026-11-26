package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class PreferenceChoicesTest {
    /** The value of each choice's checked option, in order, null where none is. */
    private fun checked(choices: List<PreferenceChoice>): List<String?> =
        choices.map { choice ->
            choice.options
                .firstOrNull { it.isChecked }
                ?.value
                ?.id
        }

    @Test
    fun fivePreferencesEachOptionInTheContractsOrderAUnitByItsWord() {
        val choices =
            preferenceChoices(
                UnitPreferences(
                    language = "de",
                    length = RestSchema.UnitLength.Imperial,
                    pace = RestSchema.UnitPace.PerDistance,
                    slope = RestSchema.UnitSlope.Percentage,
                    temperature = RestSchema.UnitTemperature.Fahrenheit,
                ),
            )

        assertEquals(
            listOf(
                PreferenceField.Length,
                PreferenceField.Pace,
                PreferenceField.Slope,
                PreferenceField.Temperature,
                PreferenceField.Language,
            ),
            choices.map { it.field },
        )
        assertEquals(listOf("metric", "imperial"), choices[0].options.map { it.value.id })
        assertEquals(listOf("per-time", "per-distance"), choices[1].options.map { it.value.id })
        assertEquals(listOf("degrees", "percentage"), choices[2].options.map { it.value.id })
        assertEquals(listOf("celsius", "fahrenheit"), choices[3].options.map { it.value.id })
        assertEquals(listOf("en", "fr", "es", "it", "de", "zh", "ja"), choices[4].options.map { it.value.id })
        assertEquals(
            listOf("metric", "imperial", "perTime", "perDistance", "degrees", "percentage", "celsius", "fahrenheit")
                .map { PreferenceLabel.Word(it) },
            choices.take(4).flatMap { it.options }.map { it.label },
        )
        assertEquals(listOf("imperial", "per-distance", "percentage", "fahrenheit", "de"), checked(choices))
    }

    @Test
    fun aLanguageIsNamedInItselfWhateverLanguageTheReaderReads() {
        val choices = preferenceChoices(UnitPreferences(language = "fr"))

        assertEquals(
            listOf("English", "Français", "Español", "Italiano", "Deutsch", "中文", "日本語")
                .map { PreferenceLabel.Name(it) },
            choices[4].options.map { it.label },
        )
    }

    @Test
    fun aLanguageIsCheckedAsTheMapReadsItByItsPrimarySubtag() {
        assertEquals("fr", checked(preferenceChoices(UnitPreferences(language = "fr-CA")))[4])
        assertEquals("fr", checked(preferenceChoices(UnitPreferences(language = "FR")))[4])
        assertEquals("zh", checked(preferenceChoices(UnitPreferences(language = "zh-Hant-TW")))[4])
        assertEquals("en", checked(preferenceChoices(UnitPreferences(language = "pt-BR")))[4])
        assertEquals("en", checked(preferenceChoices(UnitPreferences(language = "")))[4])
    }

    @Test
    fun aUnitTheContractDoesNotListChecksNoneOfItsOptions() {
        val choices =
            preferenceChoices(
                UnitPreferences(
                    language = "en",
                    length = RestSchema.UnitLength("nautical"),
                    temperature = RestSchema.UnitTemperature("Celsius"),
                ),
            )

        assertEquals(listOf(null, "per-time", "degrees", null, "en"), checked(choices))
    }
}
