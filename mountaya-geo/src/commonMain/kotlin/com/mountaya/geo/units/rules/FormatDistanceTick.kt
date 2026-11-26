package com.mountaya.geo

/**
 * formatDistanceTick states a stop of a distance axis, [tick], for the reader [units] describes (`distanceTicks`):
 * its value in kilometers or miles, as its `length` asks, with the fraction digits the stop needs, kept even where
 * they are zeros, so neighbouring stops read as different numbers, `0.5 km` beside `1.0 km`, and a whole step reads
 * whole, `5 km`. The number is written by the locale services and set in the language's own pattern.
 */
public fun formatDistanceTick(
    tick: DistanceTick,
    units: UnitPreferences,
): String {
    val patterns = unitPatterns(units.language)
    return formatMeasurement(
        tick.value,
        tick.fractionDigits,
        if (isImperial(units.length)) patterns.mile else patterns.kilometer,
        units.language,
        patterns,
        minimumFractionDigits = tick.fractionDigits,
    )
}
