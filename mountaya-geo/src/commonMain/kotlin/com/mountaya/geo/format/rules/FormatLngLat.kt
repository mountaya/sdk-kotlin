package com.mountaya.geo

import kotlin.math.abs

/**
 * formatLngLat writes [position], `[longitude, latitude]` as the wire orders it, for a reader:
 * latitude first with its hemisphere, then longitude with its own, each to four decimal places,
 * `45.9237°N 6.8652°E`.
 *
 * A longitude is read as an angle: one from -180 to 180 is printed as written, and one a map
 * counted past the antimeridian is stated as the meridian it names, so 197.5 is `162.5000°W`. A
 * hemisphere is chosen after rounding, so a value that prints as zero takes the positive one. The
 * digits and the decimal point are the same in every language, the contract's one exemption from
 * the locale services, since a locale's decimal comma beside the comma that parts two coordinates
 * is ambiguous.
 *
 * Answers null for a position that names no place: short of a longitude or a latitude, or with
 * either not finite. A latitude is printed as written, past a pole included.
 */
public fun formatLngLat(position: List<Double>): String? {
    if (!namesPlace(position)) return null
    val longitude = wrapLongitudePastRange(position[0])
    return "${coordinate(position[1], 'N', 'S')} ${coordinate(longitude, 'E', 'W')}"
}

private fun coordinate(
    value: Double,
    positive: Char,
    negative: Char,
): String {
    val digits = fourDecimalPlaces(abs(value))
    val printsAsZero = digits.all { it == '0' || it == '.' }
    return "$digits°${if (value < 0.0 && !printsAsZero) negative else positive}"
}
