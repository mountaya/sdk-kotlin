package com.mountaya.geo

/** The placeholder of a range pattern's second number. */
private const val SECOND_NUMBER_PLACEHOLDER = "{1}"

/**
 * formatAltitudeRange states an altitude band, its lowest altitude [min] and its highest [max] in meters, as one
 * range in the reader's elevation unit, meters or feet as [units]' `length` asks, each bound whole
 * (`geo.format.altitude-range`): `1,035–1,850 m` in English and `1 035–1 850 m` in French, the bounds in the
 * order given.
 *
 * The range is the language's own, never two figures joined by hand: the separator and the approximation are its
 * range patterns ([rangePatterns]), the numbers its digits and grouping, written as `formatElevation` writes
 * each, and the unit is stated once, after the range. Two bounds written alike read as that figure,
 * approximately, `~1,000 m`, a bound that rounds to zero read as zero whatever its sign; and a band whose first
 * bound is below zero takes a space either side of the separator, so the sign stands apart from it, as the
 * locale services write it. The empty string where either bound is missing or no finite number, so a caller
 * renders the answer as it is.
 */
public fun formatAltitudeRange(
    min: Double?,
    max: Double?,
    units: UnitPreferences,
): String {
    if (min == null || max == null || !min.isFinite() || !max.isFinite()) return ""
    val patterns = unitPatterns(units.language)
    val imperial = isImperial(units.length)
    val wholes =
        listOf(min, max).map { meters ->
            roundHalfAwayFromZero(if (imperial) convertLength(meters, LengthUnit.Meter, LengthUnit.Foot) else meters, 0)
        }
    val bounds = wholes.map { formatMeasurement(it, 0, NUMBER_PLACEHOLDER, units.language, patterns) }
    val range = rangePatterns(units.language)
    val text =
        if (bounds[0] == bounds[1]) {
            range.approximately.replace(NUMBER_PLACEHOLDER, bounds[0])
        } else {
            (if (wholes[0] < 0.0) spaced(range.range) else range.range)
                .replace(NUMBER_PLACEHOLDER, bounds[0])
                .replace(SECOND_NUMBER_PLACEHOLDER, bounds[1])
        }
    return (if (imperial) patterns.foot else patterns.meter).replace(NUMBER_PLACEHOLDER, text)
}

/** A range [pattern] with a space either side of its separator, where it has none there. */
private fun spaced(pattern: String): String {
    val first = pattern.indexOf(NUMBER_PLACEHOLDER)
    val second = pattern.indexOf(SECOND_NUMBER_PLACEHOLDER)
    val separatorStart = first + NUMBER_PLACEHOLDER.length
    if (first < 0 || second < 0 || separatorStart > second) return pattern
    var separator = pattern.substring(separatorStart, second)
    if (separator.firstOrNull()?.let(::isWhiteSpace) != true) separator = " $separator"
    if (separator.lastOrNull()?.let(::isWhiteSpace) != true) separator = "$separator "
    return pattern.substring(0, separatorStart) + separator + pattern.substring(second)
}
