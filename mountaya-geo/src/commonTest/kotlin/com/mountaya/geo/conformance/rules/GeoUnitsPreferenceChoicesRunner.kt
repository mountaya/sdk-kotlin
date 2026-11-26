package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope

/**
 * `geo.units.preference-choices`: what a preferences picker offers over the case's `preferences` and what a press on
 * its `press` asks for, the set it reports and the language the map relabels in. A word is named by its key in the
 * `preferences` section, as the fixture names it.
 */
object GeoUnitsPreferenceChoicesRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val written = input.objectOrNull("preferences") ?: throw AssertionError("the case names no `preferences`")
        val preferences =
            UnitPreferences(
                language = written.text("language"),
                length = RestSchema.UnitLength(written.text("length")),
                pace = RestSchema.UnitPace(written.text("pace")),
                slope = RestSchema.UnitSlope(written.text("slope")),
                temperature = RestSchema.UnitTemperature(written.text("temperature")),
            )
        val pressed =
            input.objectOrNull("press")?.let {
                preferencePress(preferences, valueOf(it.text("field"), it.text("value")))
            }
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "choices" to FixtureValue.Array(preferenceChoices(preferences).map(::choiceWritten)),
                    "relabel" to (pressed?.relabel?.let { FixtureValue.Text(it.value) } ?: FixtureValue.Null),
                    "reported" to (pressed?.next?.let(::preferencesWritten) ?: FixtureValue.Null),
                ),
            ),
        )
    }

    /** The value the option pressed names, of the preference [field] spells. */
    private fun valueOf(
        field: String,
        value: String,
    ): PreferenceValue =
        when (PreferenceField.entries.firstOrNull { it.id == field }) {
            PreferenceField.Length -> PreferenceValue.Length(RestSchema.UnitLength(value))
            PreferenceField.Pace -> PreferenceValue.Pace(RestSchema.UnitPace(value))
            PreferenceField.Slope -> PreferenceValue.Slope(RestSchema.UnitSlope(value))
            PreferenceField.Temperature -> PreferenceValue.Temperature(RestSchema.UnitTemperature(value))
            PreferenceField.Language -> PreferenceValue.Language(RestSchema.Language(value))
            null -> throw AssertionError("`$field` is no preference")
        }

    private fun choiceWritten(choice: PreferenceChoice): FixtureValue =
        FixtureValue.Object(
            listOf(
                "field" to FixtureValue.Text(choice.field.id),
                "options" to
                    FixtureValue.Array(
                        choice.options.map { option ->
                            val label =
                                when (val written = option.label) {
                                    is PreferenceLabel.Word -> {
                                        FixtureValue.Object(
                                            listOf("word" to FixtureValue.Text("preferences.${written.word}")),
                                        )
                                    }

                                    is PreferenceLabel.Name -> {
                                        FixtureValue.Object(listOf("name" to FixtureValue.Text(written.name)))
                                    }
                                }
                            FixtureValue.Object(
                                listOf(
                                    "checked" to FixtureValue.Bool(option.isChecked),
                                    "label" to label,
                                    "value" to FixtureValue.Text(option.value.id),
                                ),
                            )
                        },
                    ),
            ),
        )

    private fun preferencesWritten(preferences: UnitPreferences): FixtureValue =
        FixtureValue.Object(
            listOf(
                "language" to FixtureValue.Text(preferences.language),
                "length" to FixtureValue.Text(preferences.length.value),
                "pace" to FixtureValue.Text(preferences.pace.value),
                "slope" to FixtureValue.Text(preferences.slope.value),
                "temperature" to FixtureValue.Text(preferences.temperature.value),
            ),
        )
}
