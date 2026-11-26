package com.mountaya.geo

/**
 * formatHumidity states a relative humidity of [percent] for the reader [units] describes, whole, in the language's
 * `percent` pattern whatever its preferences: 54.6 is `55%` in English and `55 %` in French, as a readout states the
 * band a humidity overlay draws.
 *
 * The number is rounded half away from zero on its decimal value and written by the locale services, and one that
 * rounds to zero is stated with no sign. The empty string for a humidity that is missing or no finite number.
 */
public fun formatHumidity(
    percent: Double?,
    units: UnitPreferences,
): String {
    if (percent == null || !percent.isFinite()) return ""
    val patterns = unitPatterns(units.language)
    return formatMeasurement(percent, 0, patterns.percent, units.language, patterns)
}
