package com.mountaya.geo

/** The hundredths a clock number carries its seconds in. */
private const val HUNDREDTHS = 100.0

/**
 * paceMinutesToClockNumber writes a pace of [minutes], decimal minutes, as the `m.ss` number a
 * clock-separated numeric input carries: the whole minutes plus the seconds over 100, so 13.333
 * minutes, the clock `13:20`, is 13.2. The seconds are the ones [formatPaceClock] states, rounded
 * before the split, so 13.999 minutes is 14.
 *
 * The number is positional, never linear: 13.2 is thirteen minutes twenty, not thirteen and a
 * fifth, so it never leaves the input it is shown in. Null where [formatPaceClock] states nothing.
 */
public fun paceMinutesToClockNumber(minutes: Double?): Double? {
    val seconds = clockSeconds(minutes) ?: return null
    return (seconds / 60).toDouble() + (seconds % 60).toDouble() / HUNDREDTHS
}
