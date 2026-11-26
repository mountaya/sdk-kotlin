package com.mountaya.geo

/** The seconds in a minute. */
private const val SECONDS_PER_MINUTE = 60.0

/** The first count of seconds a double holds no longer to the second: 2^53. */
private const val EXACT_SECONDS_LIMIT = 9_007_199_254_740_992.0

/**
 * formatPaceClock writes a pace of [minutes], decimal minutes per unit of distance, as the `m:ss`
 * clock a reader reads it in: 13.333 minutes is `13:20`. The pace is rounded to whole seconds
 * before it is split, so a remainder never reads 60: 13.999 minutes is `14:00`. The minutes are
 * never capped, so a 1 km/h rate is `60:00` a kilometer.
 *
 * The clock is the pace's own notation, the same in every language: the digits are Latin, and the
 * seconds always two of them. The empty string for a pace that is missing, no positive finite
 * number, or one that rounds to no second, since `0:00` states a pace of no time at all, and for
 * one of 2^53 seconds or more, which no longer counts to the second.
 */
public fun formatPaceClock(minutes: Double?): String {
    val seconds = clockSeconds(minutes) ?: return ""
    return "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
}

/**
 * Returns the whole seconds a pace of [minutes] reads as, a half second up, or null where it is
 * missing, no positive finite number, rounds to no second, or is so long that a double no longer
 * counts it to the second.
 */
internal fun clockSeconds(minutes: Double?): Long? {
    if (minutes == null || !minutes.isFinite() || minutes <= 0.0) return null
    val seconds = roundHalfUp(minutes * SECONDS_PER_MINUTE)
    return if (seconds > 0.0 && seconds < EXACT_SECONDS_LIMIT) seconds.toLong() else null
}
