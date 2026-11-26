package com.mountaya.geo

/** The places a snowfall is stated to in inches. */
private const val INCH_FRACTION_DIGITS = 1

/**
 * formatSnowfall states a depth of snow of [centimeters] for the reader [units] describes, as its `length` asks: whole
 * centimeters in the metric system, `35 cm`, and inches to one decimal in the imperial, a trailing zero dropped, so 35
 * cm is `13.8 in` and 25.4 cm `10 in`, as a readout states the band a snowfall or snow depth overlay draws.
 *
 * The number is rounded half away from zero on its decimal value, written by the locale services and set in the
 * language's own pattern, and one that rounds to zero is stated with no sign. The empty string for a depth that is
 * missing or no finite number.
 */
public fun formatSnowfall(
    centimeters: Double?,
    units: UnitPreferences,
): String {
    if (centimeters == null || !centimeters.isFinite()) return ""
    val patterns = unitPatterns(units.language)
    return if (isImperial(units.length)) {
        formatMeasurement(
            convertLength(centimeters, LengthUnit.Centimeter, LengthUnit.Inch),
            INCH_FRACTION_DIGITS,
            patterns.inch,
            units.language,
            patterns,
        )
    } else {
        formatMeasurement(centimeters, 0, patterns.centimeter, units.language, patterns)
    }
}
