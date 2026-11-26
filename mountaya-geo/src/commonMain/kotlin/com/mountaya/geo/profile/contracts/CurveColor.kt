package com.mountaya.geo

/**
 * CurveColor is a stretch of an itinerary its profile's curve is painted in a colour of its own
 * (`geo.profile.curve-stops`): its two ends in meters from the itinerary's start, in either order, and its
 * colour, any CSS colour, painted as given, since it is data and not a theme token.
 *
 * Two are equal where they hold the same numbers and the colours are written alike, as every value of the
 * geo layer compares.
 */
public class CurveColor(
    /** Where the stretch starts, in meters from the start of the itinerary. */
    override val from: Double,
    /** Where the stretch ends, in meters from the start of the itinerary. */
    override val to: Double,
    /** The colour the stretch is painted in. */
    public val color: String,
) : ProfileSpan<CurveColor> {
    override fun spanning(
        from: Double,
        to: Double,
    ): CurveColor = CurveColor(from, to, color)

    override fun equals(other: Any?): Boolean =
        other is CurveColor && sameNumber(other.from, from) && sameNumber(other.to, to) && other.color == color

    override fun hashCode(): Int = (from.hashCode() * 31 + to.hashCode()) * 31 + color.hashCode()

    override fun toString(): String = "CurveColor(from=$from, to=$to, color=$color)"
}
