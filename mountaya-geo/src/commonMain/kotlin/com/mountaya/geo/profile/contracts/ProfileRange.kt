package com.mountaya.geo

/**
 * ProfileRange is a stretch of an itinerary's distance axis, its two ends in meters from the itinerary's
 * start: the window a profile is drawn over, and the stretch a reader points at.
 *
 * Its ends may be given in either order, and every rule that reads one reads it in order. Two are equal
 * where they hold the same numbers, as every value of the geo layer compares.
 */
public class ProfileRange(
    /** Where the stretch starts, in meters from the start of the itinerary. */
    override val from: Double,
    /** Where the stretch ends, in meters from the start of the itinerary. */
    override val to: Double,
) : ProfileSpan<ProfileRange> {
    override fun spanning(
        from: Double,
        to: Double,
    ): ProfileRange = ProfileRange(from, to)

    override fun equals(other: Any?): Boolean =
        other is ProfileRange && sameNumber(other.from, from) && sameNumber(other.to, to)

    override fun hashCode(): Int = from.hashCode() * 31 + to.hashCode()

    override fun toString(): String = "ProfileRange(from=$from, to=$to)"
}
