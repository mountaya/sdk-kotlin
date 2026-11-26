package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/** An angle's pattern: the degree sign follows the number in every language. */
private const val DEGREES_PATTERN = "{0}°"

/**
 * formatSlope states a slope of [degrees] for the reader [units] describes, as an angle or as a
 * percentage grade as its `slope` asks, to the whole unit: 12.5° is `13°`, or `22%` as a grade.
 * A grade is `tan(θ) × 100`, never θ, so 45° is `100%`.
 *
 * A descent keeps its sign, `-22%`, and a slope that rounds to zero is level to the precision
 * stated and takes none, `0°`. The degree sign is the same in every language; a percentage takes
 * the language's own pattern. The empty string for a slope that is missing or no finite number.
 * Any slope unit other than a percentage reads as degrees.
 */
public fun formatSlope(
    degrees: Double?,
    units: UnitPreferences,
): String {
    if (degrees == null || !degrees.isFinite()) return ""
    return if (units.slope == RestSchema.UnitSlope.Percentage) {
        val patterns = unitPatterns(units.language)
        formatMeasurement(
            convertSlope(degrees, SlopeUnit.Degrees, SlopeUnit.Percentage),
            0,
            patterns.percent,
            units.language,
            patterns,
        )
    } else {
        formatMeasurement(degrees, 0, DEGREES_PATTERN, units.language)
    }
}
