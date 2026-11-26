package com.mountaya.geo

import kotlin.math.floor

/**
 * ascentSlopeRamp is the ramp an ascent paints from, in ascending degrees, the `ascentRampStops`
 * `geo/slope-ramp.json` declares: green to 2 degrees, then yellow, orange, red and a deep red from
 * 22 degrees.
 */
public val ascentSlopeRamp: List<SlopeRampStop> =
    listOf(
        SlopeRampStop("#66BB6A", 0.0),
        SlopeRampStop("#66BB6A", 2.0),
        SlopeRampStop("#FFEB3B", 3.0),
        SlopeRampStop("#FF9800", 6.0),
        SlopeRampStop("#F44336", 11.0),
        SlopeRampStop("#B71C1C", 22.0),
    )

/**
 * descentSlopeRamp is the ramp a descent paints from, by its magnitude, in ascending degrees, the
 * `descentRampStops` `geo/slope-ramp.json` declares: the green level ground shares with an ascent,
 * then blues that deepen to 22 degrees.
 */
public val descentSlopeRamp: List<SlopeRampStop> =
    listOf(
        SlopeRampStop("#66BB6A", 0.0),
        SlopeRampStop("#64B5F6", 2.0),
        SlopeRampStop("#29B6F6", 3.0),
        SlopeRampStop("#1E88E5", 6.0),
        SlopeRampStop("#1565C0", 11.0),
        SlopeRampStop("#0D47A1", 22.0),
    )

/** The colour a slope that is no number paints: a neutral grey, on no ramp. */
private const val NO_SLOPE_COLOR = "#94a3b8"

/** The hexadecimal radix colours are written in. */
private const val HEXADECIMAL = 16

/**
 * slopeRampColor is the colour a slope of [degrees] paints on a curve, `#rrggbb` in lower case. An
 * ascent reads [ascentSlopeRamp] and a descent [descentSlopeRamp] by its magnitude, and level
 * ground the first ascent stop. Between two stops each channel is the straight mix of theirs,
 * rounded to the nearest whole number, a half up; at or past the last stop it is the last colour.
 * A slope that is not a number paints a neutral grey, `#94a3b8`.
 */
public fun slopeRampColor(degrees: Double): String =
    when {
        degrees.isNaN() -> NO_SLOPE_COLOR
        degrees > 0.0 -> rampColor(ascentSlopeRamp, degrees).lowercase()
        degrees < 0.0 -> rampColor(descentSlopeRamp, -degrees).lowercase()
        else -> ascentSlopeRamp.first().color.lowercase()
    }

/** The colour [stops] paint at [magnitude]. */
private fun rampColor(
    stops: List<SlopeRampStop>,
    magnitude: Double,
): String {
    if (magnitude <= stops.first().degrees) return stops.first().color
    if (magnitude >= stops.last().degrees) return stops.last().color
    for (index in 1 until stops.size) {
        val lower = stops[index - 1]
        val upper = stops[index]
        if (magnitude <= upper.degrees) {
            val span = upper.degrees - lower.degrees
            val ratio = if (span == 0.0) 0.0 else (magnitude - lower.degrees) / span
            return mixed(lower.color, upper.color, ratio)
        }
    }
    return stops.last().color
}

/** The colour [ratio] of the way from [from] to [to], channel by channel. */
private fun mixed(
    from: String,
    to: String,
    ratio: Double,
): String =
    (0 until 3).joinToString("", "#") { channel ->
        val start = from.substring(1 + 2 * channel, 3 + 2 * channel).toInt(HEXADECIMAL)
        val end = to.substring(1 + 2 * channel, 3 + 2 * channel).toInt(HEXADECIMAL)
        val value = start + (end - start) * ratio
        val whole = floor(value)
        val rounded = if (value - whole >= 0.5) whole + 1.0 else whole
        rounded.toInt().toString(HEXADECIMAL).padStart(2, '0')
    }
