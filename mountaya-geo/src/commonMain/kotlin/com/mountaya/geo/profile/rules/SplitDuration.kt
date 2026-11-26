package com.mountaya.geo

import kotlin.math.floor

/** The seconds in a minute. */
private const val SECONDS_PER_MINUTE = 60.0

/** The minutes in an hour. */
private const val MINUTES_PER_HOUR = 60L

/**
 * splitDuration splits a duration of [seconds] into the whole hours and the minutes a reader is
 * shown. The total is rounded to whole minutes first, a half minute rounding up, and then split, so
 * a remainder that rounds to sixty is carried into the hours: 7,170 s is 2 h 0 min, never 1 h
 * 60 min. Hours are not carried into days, so a day is 24 h.
 *
 * A duration under thirty seconds splits into no hours and no minutes, a real duration too short to
 * state in minutes, which a surface renders as nothing. Null for a missing, zero, negative or
 * non-finite duration, which is no duration at all.
 */
public fun splitDuration(seconds: Double?): DurationSplit? {
    if (seconds == null || !seconds.isFinite() || seconds <= 0.0) return null
    val minutes = roundHalfUp(seconds / SECONDS_PER_MINUTE).toLong()
    return DurationSplit(minutes / MINUTES_PER_HOUR, (minutes % MINUTES_PER_HOUR).toInt())
}

/**
 * Returns [value], zero or more, rounded to the nearest whole number, a half up. The fraction is
 * read exactly, so 0.49999999999999994 rounds down where adding a half first would not.
 */
internal fun roundHalfUp(value: Double): Double {
    val whole = floor(value)
    return if (value - whole >= 0.5) whole + 1.0 else whole
}
