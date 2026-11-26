package com.mountaya.geo

import kotlin.time.Instant

/**
 * TimeSliderReadout is what a time slider's thumb reads at a stop (`geo.layers.time-readout`): the instant the stop stands
 * for, and its offset from the stop of the clock's reading as a word of the elements' `layers` section, counted where it
 * counts hours or days. A slider writes the instant through the locale services, then ` · `, then the word, counted
 * through `countedText`, and the word alone where the stop stands for no instant.
 *
 * Two are equal where every member is.
 */
public class TimeSliderReadout(
    /** The instant the stop stands for, or null for a stop of a track of no instant. */
    public val instant: Instant?,
    /**
     * The path of the offset's word: `layers.timeNow` at the clock's reading, `layers.timeHoursAhead` or
     * `layers.timeHoursAgo` under a day, and `layers.timeDaysAhead` or `layers.timeDaysAgo` from a day on.
     */
    public val word: String,
    /** The hours or the days the counted word takes, never below one, or null for `layers.timeNow`. */
    public val count: Int?,
) {
    override fun equals(other: Any?): Boolean =
        other is TimeSliderReadout && other.instant == instant && other.word == word && other.count == count

    override fun hashCode(): Int = (instant.hashCode() * 31 + word.hashCode()) * 31 + (count ?: -1)

    override fun toString(): String = "TimeSliderReadout(instant=$instant, word=$word, count=$count)"
}
