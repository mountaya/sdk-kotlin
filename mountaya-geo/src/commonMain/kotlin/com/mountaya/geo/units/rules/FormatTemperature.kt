package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * formatTemperature states a temperature of [celsius], degrees Celsius, for the reader [units]
 * describes, in the scale its `temperature` asks, to the whole degree: 4 °C is `4°C`, or `39°F`.
 * It reads `temperature` alone and never `length`, so an imperial reader who stored Celsius is
 * shown Celsius, `4 °C` for a French one.
 *
 * A temperature that rounds to zero is stated with no sign, `0°C`. The empty string for a
 * temperature that is missing or no finite number. Any scale other than Fahrenheit reads as
 * Celsius.
 */
public fun formatTemperature(
    celsius: Double?,
    units: UnitPreferences,
): String {
    if (celsius == null || !celsius.isFinite()) return ""
    val patterns = unitPatterns(units.language)
    return if (units.temperature == RestSchema.UnitTemperature.Fahrenheit) {
        formatMeasurement(
            convertTemperature(celsius, TemperatureUnit.Celsius, TemperatureUnit.Fahrenheit),
            0,
            patterns.fahrenheit,
            units.language,
            patterns,
        )
    } else {
        formatMeasurement(celsius, 0, patterns.celsius, units.language, patterns)
    }
}
