package com.mountaya.geo

import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/**
 * TIME_WINDOW_PAST_HOURS is how far before the clock's reading a time slider's track reaches, in hours: an hour
 * short of the three days the Tile API draws a stamped overlay back to, so a request asked at the first stop is
 * still inside that window when it arrives (`geo.layers.time-window`).
 */
public const val TIME_WINDOW_PAST_HOURS: Int = 71

/**
 * TIME_WINDOW_FUTURE_HOURS is how far after the clock's reading a time slider's track reaches, in hours: the six
 * days the Tile API draws ahead.
 */
public const val TIME_WINDOW_FUTURE_HOURS: Int = 144

/** The farthest from the epoch either end of a track may fall, in milliseconds: 100,000,000 days. */
private const val TRACK_RANGE_MILLISECONDS: Long = 8_640_000_000_000_000

/**
 * timeSliderWindow lays a time slider's track out around [now], the clock's reading (`geo.layers.time-window`): from
 * [TIME_WINDOW_PAST_HOURS] before it to [TIME_WINDOW_FUTURE_HOURS] after it, one stop an hour, stop
 * `TIME_WINDOW_PAST_HOURS` standing for the reading itself.
 *
 * Where either end of the track would fall more than 8.64e15 milliseconds from the epoch, the range an ECMAScript or a
 * Dart instant holds, which every SDK holds a track to, it lays out the same track with no instant
 * ([TimeSliderWindow.start] null). It reads no clock: a panel hands it its clock's reading each time it draws, so the
 * window follows the clock with no timer of its own.
 */
public fun timeSliderWindow(now: Instant): TimeSliderWindow {
    val start = now - TIME_WINDOW_PAST_HOURS.hours
    val end = now + TIME_WINDOW_FUTURE_HOURS.hours
    val held =
        start >= Instant.fromEpochMilliseconds(-TRACK_RANGE_MILLISECONDS) &&
            end <= Instant.fromEpochMilliseconds(TRACK_RANGE_MILLISECONDS)
    return TimeSliderWindow(if (held) start else null)
}
