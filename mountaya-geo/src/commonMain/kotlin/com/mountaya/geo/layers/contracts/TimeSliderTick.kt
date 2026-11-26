package com.mountaya.geo

/**
 * TimeSliderTick is one tick a time slider marks on its track (`geo.layers.time-slider`): the stop it stands at, and the
 * word it carries, by its dotted path as `ElementsScope.word` reads one, `layers.timeNow`, or null for a bare tick.
 *
 * Two are equal where both members are.
 */
public class TimeSliderTick(
    /** The stop the tick stands at. */
    public val value: Int,
    /** The path of the word the tick carries, `layers.timeMinus2d`, or null for none. */
    public val word: String?,
) {
    override fun equals(other: Any?): Boolean = other is TimeSliderTick && other.value == value && other.word == word

    override fun hashCode(): Int = value * 31 + word.hashCode()

    override fun toString(): String = "TimeSliderTick(value=$value, word=$word)"
}
