package com.mountaya.geo

/**
 * ElevationDomain is the elevation range a profile's curve is drawn against, in meters above sea level: its
 * floor is drawn at the bottom of the plot and its ceiling under its top inset (`geo.profile.plot-scale`).
 *
 * Two are equal where they hold the same numbers, as every value of the geo layer compares.
 */
public class ElevationDomain(
    /** The floor, in meters above sea level. */
    public val min: Double,
    /** The ceiling, in meters above sea level. */
    public val max: Double,
) {
    override fun equals(other: Any?): Boolean =
        other is ElevationDomain && sameNumber(other.min, min) && sameNumber(other.max, max)

    override fun hashCode(): Int = min.hashCode() * 31 + max.hashCode()

    override fun toString(): String = "ElevationDomain(min=$min, max=$max)"
}
