package com.mountaya.geo

/**
 * timeSliderRelease answers what a time slider commits as a reader lets its thumb go on the stop [value] of [window]'s
 * track (`geo.layers.time-slider`): no pin on the stop of the clock's reading, so every surface that reads the time
 * follows the clock again rather than holding the moment the clock read as the slider was drawn; the instant the stop
 * stands for on any other stop; and nothing, null, on a track of no instant, which holds the slider under
 * `layers.timeUnavailable`, and for a value that is no stop of the track.
 */
public fun timeSliderRelease(
    window: TimeSliderWindow,
    value: Int,
): TimeSliderRelease? {
    val instant = window.dateAt(value) ?: return null
    return TimeSliderRelease(if (value == window.nowValue) null else instant)
}
