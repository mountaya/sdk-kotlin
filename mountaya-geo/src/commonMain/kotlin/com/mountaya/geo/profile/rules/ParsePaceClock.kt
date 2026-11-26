package com.mountaya.geo

/** A clock: whole minutes, a colon, and two digits of seconds under 60. */
private val clockPattern = Regex("^([0-9]+):([0-5][0-9])$")

/** A bare number of minutes, with a dot or a comma before its decimals. */
private val bareMinutesPattern = Regex("^[0-9]+(?:[.,][0-9]+)?$")

/** The seconds in a minute. */
private const val SECONDS_PER_MINUTE = 60.0

/**
 * parsePaceClock reads a pace typed as text back into decimal minutes, after trimming the white
 * space at its ends: `m:ss`, the minutes any number of digits and the seconds exactly two under 60,
 * so `13:20` is 13.333 and `4:05` is 4.083, or a bare number of minutes with a dot or a comma
 * before its decimals, so `13` is 13 and `6,5` is 6.5.
 *
 * Anything else is null: `13:5`, which could mean 5 seconds or 50, `13:60`, `1:02:03`, a sign, text,
 * and a pace of no time at all, such as `0:00`. Null rather than a fallback, because a pace the
 * caller could not read must never silently become another one. The digits read are ASCII digits.
 */
public fun parsePaceClock(text: String): Double? {
    val trimmed = trimWhiteSpace(text)
    val clock = clockPattern.matchEntire(trimmed)
    val minutes =
        when {
            clock != null -> {
                val (whole, seconds) = clock.destructured
                whole.toDouble() + seconds.toDouble() / SECONDS_PER_MINUTE
            }

            bareMinutesPattern.matches(trimmed) -> {
                trimmed.replace(',', '.').toDouble()
            }

            else -> {
                return null
            }
        }
    return minutes.takeIf { it.isFinite() && it > 0.0 }
}
