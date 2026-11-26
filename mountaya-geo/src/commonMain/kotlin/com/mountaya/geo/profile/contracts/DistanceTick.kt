package com.mountaya.geo

/**
 * DistanceTick is one labelled stop of a distance axis: its value in the reader's distance unit, its
 * position in meters, and the fraction digits its label needs for neighbouring stops to read as
 * different numbers.
 *
 * Two are equal where they hold the same numbers, as every value of the geo layer compares.
 */
public class DistanceTick(
    /** The stop in the display unit, kilometers or miles, on the 1, 2, 5 lattice. */
    public val value: Double,
    /** The stop in meters from the itinerary's start, where a chart places it. */
    public val meters: Double,
    /** The fraction digits the label is written with, never under 0. */
    public val fractionDigits: Int,
) {
    override fun equals(other: Any?): Boolean =
        other is DistanceTick && sameNumber(other.value, value) && sameNumber(other.meters, meters) &&
            other.fractionDigits == fractionDigits

    override fun hashCode(): Int = (value.hashCode() * 31 + meters.hashCode()) * 31 + fractionDigits

    override fun toString(): String = "DistanceTick(value=$value, meters=$meters, fractionDigits=$fractionDigits)"
}
