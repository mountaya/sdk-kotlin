package com.mountaya.geo

import kotlin.math.floor

/** The hundredths a clock number carries its seconds in. */
private const val HUNDREDTHS = 100.0

/** The seconds in a minute. */
private const val SECONDS_PER_MINUTE = 60.0

/**
 * paceClockNumberToMinutes reads an `m.ss` clock number, the whole minutes plus the seconds over
 * 100, back into decimal minutes: 13.2 is 13 minutes 20 seconds, 13.333 minutes. Its hundredths are
 * read as whole seconds, rounded, and seconds past 59 carry into the minutes, since a numeric input
 * steps in decimal: `13:55` plus five seconds arrives as 13.6, fourteen minutes flat.
 *
 * Null for a value that is missing or no positive finite number.
 */
public fun paceClockNumberToMinutes(value: Double?): Double? {
    if (value == null || !value.isFinite() || value <= 0.0) return null
    val minutes = floor(value)
    return minutes + roundHalfUp((value - minutes) * HUNDREDTHS) / SECONDS_PER_MINUTE
}
