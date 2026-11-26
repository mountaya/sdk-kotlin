package com.mountaya.geo

import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * TimeSliderWindow is the forecast window a time slider spans, laid out around one clock reading at one stop an hour
 * (`geo.layers.time-window`): a stop is a whole number of hours from the track's start, stop 0 the earliest instant the
 * time-aware overlays hold, [nowValue] the stop of the reading and [maxValue] the last.
 *
 * A track has an instant at every stop or at none, [start] saying which: `timeSliderWindow` lays out a track of no
 * instant where one of its ends would fall more than 8.64e15 milliseconds from the epoch, the range every SDK holds a
 * track to. Such a track still runs from 0 to [maxValue]; every stop stands for no instant, and every instant is placed
 * at [nowValue].
 *
 * Two are equal where they start at the same instant, or both hold none.
 */
public class TimeSliderWindow internal constructor(
    /** The instant stop 0 stands for, or null for a track of no instant. */
    public val start: Instant?,
) {
    /** The stop that stands for the clock's reading, [TIME_WINDOW_PAST_HOURS]. */
    public val nowValue: Int get() = TIME_WINDOW_PAST_HOURS

    /** The track's last stop, its whole length in hours. */
    public val maxValue: Int get() = TIME_WINDOW_PAST_HOURS + TIME_WINDOW_FUTURE_HOURS

    /**
     * Answers the instant the stop [value] stands for, the track's start plus its hours, or null on a track of no
     * instant and for a value that is no stop of the track.
     */
    public fun dateAt(value: Int): Instant? {
        val start = start ?: return null
        return if (value in 0..maxValue) start + value.hours else null
    }

    /**
     * Answers the stop [instant] is placed at: its nearest, a half hour going to the later one, held to the track, so a
     * pin the window has moved past still places a thumb at its nearest end. No pin, null, is placed at [nowValue], and
     * so is every instant on a track of no instant.
     */
    public fun valueAt(instant: Instant?): Int {
        val start = start ?: return nowValue
        if (instant == null) return nowValue
        if (instant <= start) return 0
        if (instant >= start + maxValue.hours) return maxValue
        // Within the track, the distance from its start is held to the nanosecond, which a Long counts for 292 years.
        val nanoseconds = (instant - start).inWholeNanoseconds
        val hour = 1.hours.inWholeNanoseconds
        return (nanoseconds + 30.minutes.inWholeNanoseconds).floorDiv(hour).toInt().coerceIn(0, maxValue)
    }

    override fun equals(other: Any?): Boolean = other is TimeSliderWindow && other.start == start

    override fun hashCode(): Int = start.hashCode()

    override fun toString(): String = "TimeSliderWindow(start=$start)"
}
