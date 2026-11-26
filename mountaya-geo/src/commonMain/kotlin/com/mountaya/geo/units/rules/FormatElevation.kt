package com.mountaya.geo

/**
 * formatElevation states an elevation or a height gain of [meters] for the reader [units]
 * describes, in meters or in feet as its `length` asks, to the whole unit: 1,531 m is `1,531 m`
 * and `5,023 ft` in English. Whole numbers, because a terrain sample is not accurate to a
 * decimeter.
 *
 * The number is rounded half away from zero on its decimal value, and one that rounds to zero is
 * stated with no sign, `0 m`. The empty string for a value that is missing or no finite number.
 */
public fun formatElevation(
    meters: Double?,
    units: UnitPreferences,
): String {
    if (meters == null || !meters.isFinite()) return ""
    val patterns = unitPatterns(units.language)
    return if (isImperial(units.length)) {
        formatMeasurement(
            convertLength(meters, LengthUnit.Meter, LengthUnit.Foot),
            0,
            patterns.foot,
            units.language,
            patterns,
        )
    } else {
        formatMeasurement(meters, 0, patterns.meter, units.language, patterns)
    }
}
