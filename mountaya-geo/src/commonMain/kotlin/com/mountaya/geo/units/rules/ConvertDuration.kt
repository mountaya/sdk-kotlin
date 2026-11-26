package com.mountaya.geo

/**
 * convertDuration converts [value] from one duration unit to another through the whole number of
 * milliseconds each holds, a day being 86,400 s. Nothing is clamped, and a value that is not
 * finite converts to itself.
 */
public fun convertDuration(
    value: Double,
    from: DurationUnit,
    to: DurationUnit,
): Double = scaleByRatio(value, from.milliseconds, to.milliseconds)

/** The unit's length in whole milliseconds. */
internal val DurationUnit.milliseconds: Long
    get() =
        when (this) {
            DurationUnit.Day -> 24L * HOUR
            DurationUnit.Hour -> HOUR
            DurationUnit.Millisecond -> 1L
            DurationUnit.Minute -> MINUTE
            DurationUnit.Second -> SECOND
        }

private const val SECOND = 1_000L
private const val MINUTE = 60L * SECOND
private const val HOUR = 60L * MINUTE
