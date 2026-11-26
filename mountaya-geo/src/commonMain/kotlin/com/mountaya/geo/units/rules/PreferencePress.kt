package com.mountaya.geo

/**
 * preferencePress is what a press on the option [value] of a preferences picker made over [preferences] asks
 * for (`geo.units.preference-choices`): the whole set, the pressed preference changed and every other as it
 * was, and, for a language, the language the map relabels in, read as a label language (`labelLanguage`),
 * since a language is a concern of the map's labels as well.
 *
 * The set is reported rather than kept: units are an app's own concern, read by surfaces a picker cannot see,
 * so a picker holds no set of its own. A press on the option the set holds reports the set as it stands.
 */
public fun preferencePress(
    preferences: UnitPreferences,
    value: PreferenceValue,
): PreferencePress =
    when (value) {
        is PreferenceValue.Length -> {
            PreferencePress(
                UnitPreferences(
                    preferences.language,
                    value.value,
                    preferences.pace,
                    preferences.slope,
                    preferences.temperature,
                ),
                relabel = null,
            )
        }

        is PreferenceValue.Pace -> {
            PreferencePress(
                UnitPreferences(
                    preferences.language,
                    preferences.length,
                    value.value,
                    preferences.slope,
                    preferences.temperature,
                ),
                relabel = null,
            )
        }

        is PreferenceValue.Slope -> {
            PreferencePress(
                UnitPreferences(
                    preferences.language,
                    preferences.length,
                    preferences.pace,
                    value.value,
                    preferences.temperature,
                ),
                relabel = null,
            )
        }

        is PreferenceValue.Temperature -> {
            PreferencePress(
                UnitPreferences(
                    preferences.language,
                    preferences.length,
                    preferences.pace,
                    preferences.slope,
                    value.value,
                ),
                relabel = null,
            )
        }

        is PreferenceValue.Language -> {
            PreferencePress(
                UnitPreferences(
                    value.value.value,
                    preferences.length,
                    preferences.pace,
                    preferences.slope,
                    preferences.temperature,
                ),
                relabel = labelLanguage(value.value.value),
            )
        }
    }
