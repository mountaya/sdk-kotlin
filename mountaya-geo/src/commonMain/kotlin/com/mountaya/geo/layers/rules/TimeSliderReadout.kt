package com.mountaya.geo

import kotlin.math.abs

/** The hours of a day, the offset from which a readout counts days rather than hours. */
private const val HOURS_PER_DAY: Int = 24

/**
 * timeSliderReadout answers what a time slider's thumb reads at the stop [value] of [window]'s track
 * (`geo.layers.time-readout`): the instant the stop stands for, and its offset from the stop of the clock's reading in
 * hours, `layers.timeNow` at none, its whole hours under a day, `layers.timeHoursAhead` after the reading and
 * `layers.timeHoursAgo` before it, and its whole days from a day on, truncated toward the reading, so 47 hours ahead
 * reads one day ahead, `layers.timeDaysAhead` or `layers.timeDaysAgo`. A day here is 24 stops, never a calendar day, so
 * a day ahead reads `in 1 day` and never `tomorrow`. A stop that stands for no instant reads its offset alone.
 */
public fun timeSliderReadout(
    window: TimeSliderWindow,
    value: Int,
): TimeSliderReadout {
    val instant = window.dateAt(value)
    val hours = value - window.nowValue
    val ahead = hours > 0
    return when {
        hours == 0 -> {
            TimeSliderReadout(instant, "layers.timeNow", null)
        }

        abs(hours) < HOURS_PER_DAY -> {
            TimeSliderReadout(instant, if (ahead) "layers.timeHoursAhead" else "layers.timeHoursAgo", abs(hours))
        }

        else -> {
            TimeSliderReadout(
                instant,
                if (ahead) "layers.timeDaysAhead" else "layers.timeDaysAgo",
                abs(hours) / HOURS_PER_DAY,
            )
        }
    }
}
