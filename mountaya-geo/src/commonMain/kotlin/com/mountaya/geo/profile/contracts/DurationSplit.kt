package com.mountaya.geo

/**
 * DurationSplit is a duration as a reader is shown it: whole hours, never carried into days, and
 * the minutes past them, from 0 to 59.
 */
public class DurationSplit(
    /** The whole hours. */
    public val hours: Long,
    /** The minutes past [hours], from 0 to 59. */
    public val minutes: Int,
) {
    override fun equals(other: Any?): Boolean =
        other is DurationSplit && other.hours == hours && other.minutes == minutes

    override fun hashCode(): Int = hours.hashCode() * 31 + minutes

    override fun toString(): String = "DurationSplit(hours=$hours, minutes=$minutes)"
}
