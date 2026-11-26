package com.mountaya.geo

/** The places a distance is stated to. */
private const val DISTANCE_FRACTION_DIGITS = 1

/**
 * formatDistance states a distance of [meters] for the reader [units] describes, in kilometers or
 * in miles as its `length` asks, to one decimal: 850 m is `0.9 km` and `0.5 mi` in English, and
 * 24,300 m is `24.3 km`. One decimal, because an itinerary's length is a planning figure.
 *
 * The number is rounded half away from zero on its decimal value, written with the reader's digits
 * and decimal mark, and set in the language's own pattern. The empty string for a distance that is
 * missing or no finite number, so a caller renders the answer as it is.
 */
public fun formatDistance(
    meters: Double?,
    units: UnitPreferences,
): String {
    if (meters == null || !meters.isFinite()) return ""
    val patterns = unitPatterns(units.language)
    return if (isImperial(units.length)) {
        formatMeasurement(
            convertLength(meters, LengthUnit.Meter, LengthUnit.Mile),
            DISTANCE_FRACTION_DIGITS,
            patterns.mile,
            units.language,
            patterns,
        )
    } else {
        formatMeasurement(
            convertLength(meters, LengthUnit.Meter, LengthUnit.Kilometer),
            DISTANCE_FRACTION_DIGITS,
            patterns.kilometer,
            units.language,
            patterns,
        )
    }
}
