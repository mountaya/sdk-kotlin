package com.mountaya.geo

/**
 * formatDuration states a duration of [seconds] for the reader [units] describes, as the whole
 * hours and the minutes [splitDuration] splits it into, each with the language's narrow hour and
 * minute, joined by a space: 9,320 s is `2h 35m` in English. A whole number of hours drops the
 * minutes, `2h`, and a duration under an hour states its minutes alone, `1m`.
 *
 * The empty string for a duration that is missing, zero, negative or no finite number, and for one
 * under thirty seconds, which rounds to no minute: printing a zero is a claim, not an absence. Only
 * the language of [units] is read.
 */
public fun formatDuration(
    seconds: Double?,
    units: UnitPreferences,
): String {
    val split = splitDuration(seconds) ?: return ""
    val patterns = unitPatterns(units.language)
    val minutes = formatMeasurement(split.minutes.toDouble(), 0, patterns.minute, units.language, patterns)
    if (split.hours == 0L) return if (split.minutes == 0) "" else minutes
    val hours = formatMeasurement(split.hours.toDouble(), 0, patterns.hour, units.language, patterns)
    return if (split.minutes == 0) hours else "$hours $minutes"
}
