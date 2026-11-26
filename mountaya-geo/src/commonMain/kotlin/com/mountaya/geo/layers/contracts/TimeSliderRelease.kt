package com.mountaya.geo

import kotlin.time.Instant

/**
 * TimeSliderRelease is what a time slider commits as a reader lets its thumb go (`geo.layers.time-slider`): the instant
 * to pin, which a layers selection pins (`LayersSelectionEngine.setInstant`), or null to pin none, so every surface that
 * reads the time follows the clock again.
 *
 * Two are equal where they pin the same.
 */
public class TimeSliderRelease(
    /** The instant the release pins, or null where it pins none. */
    public val pin: Instant?,
) {
    override fun equals(other: Any?): Boolean = other is TimeSliderRelease && other.pin == pin

    override fun hashCode(): Int = pin.hashCode()

    override fun toString(): String = "TimeSliderRelease(pin=$pin)"
}
