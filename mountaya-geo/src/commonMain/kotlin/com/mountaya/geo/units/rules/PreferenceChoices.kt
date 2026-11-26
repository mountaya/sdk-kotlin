package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.generated.languageEndonyms

/** The measurement systems in the order the contract lists them, each with the key of its word. */
private val lengthOptions =
    listOf(RestSchema.UnitLength.Metric to "metric", RestSchema.UnitLength.Imperial to "imperial")

/** The forms a travel rate takes in the order the contract lists them, each with the key of its word. */
private val paceOptions =
    listOf(RestSchema.UnitPace.PerTime to "perTime", RestSchema.UnitPace.PerDistance to "perDistance")

/** The slope notations in the order the contract lists them, each with the key of its word. */
private val slopeOptions =
    listOf(RestSchema.UnitSlope.Degrees to "degrees", RestSchema.UnitSlope.Percentage to "percentage")

/** The temperature scales in the order the contract lists them, each with the key of its word. */
private val temperatureOptions =
    listOf(
        RestSchema.UnitTemperature.Celsius to "celsius",
        RestSchema.UnitTemperature.Fahrenheit to "fahrenheit",
    )

/**
 * preferenceChoices is what a preferences picker offers over [preferences] (`geo.units.preference-choices`):
 * the length system, the form a travel rate takes, the slope notation, the temperature scale and the label
 * language, in that order, each option in the order the contract lists it, a unit labelled by its word of the
 * elements' `preferences` section and a language by its own name.
 *
 * An option is checked where the set holds it, a unit spelled the same, and a language as the map reads one,
 * by its primary subtag (`labelLanguage`): a set holding `fr-CA` checks French, and one holding a tag the map
 * labels in no language of, `pt-BR`, checks English. A unit the set holds that the contract does not list
 * checks none of its options.
 */
public fun preferenceChoices(preferences: UnitPreferences): List<PreferenceChoice> {
    val language = labelLanguage(preferences.language)
    return listOf(
        PreferenceChoice(
            PreferenceField.Length,
            lengthOptions.map { (value, word) ->
                unitOption(
                    PreferenceValue.Length(value),
                    word,
                    value == preferences.length,
                )
            },
        ),
        PreferenceChoice(
            PreferenceField.Pace,
            paceOptions.map { (value, word) ->
                unitOption(PreferenceValue.Pace(value), word, value == preferences.pace)
            },
        ),
        PreferenceChoice(
            PreferenceField.Slope,
            slopeOptions.map { (value, word) ->
                unitOption(
                    PreferenceValue.Slope(value),
                    word,
                    value == preferences.slope,
                )
            },
        ),
        PreferenceChoice(
            PreferenceField.Temperature,
            temperatureOptions.map { (value, word) ->
                unitOption(PreferenceValue.Temperature(value), word, value == preferences.temperature)
            },
        ),
        PreferenceChoice(
            PreferenceField.Language,
            labelLanguages.map { value ->
                PreferenceOption(
                    PreferenceValue.Language(value),
                    PreferenceLabel.Name(languageEndonyms[value] ?: value.value),
                    value == language,
                )
            },
        ),
    )
}

/** The option of a unit, labelled by its word. */
private fun unitOption(
    value: PreferenceValue,
    word: String,
    isChecked: Boolean,
): PreferenceOption = PreferenceOption(value, PreferenceLabel.Word(word), isChecked)
