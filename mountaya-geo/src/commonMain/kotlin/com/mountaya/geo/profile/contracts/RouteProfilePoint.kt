package com.mountaya.geo

/**
 * RouteProfilePoint is one sample a climb is read on: how far along the itinerary it stands, in
 * kilometers, and the ground's altitude there.
 *
 * The distance is in kilometers because the climb's windows are read in kilometers, and a run
 * measured in kilometers is not always the same double as one measured in meters and divided:
 * 1.2 − 0.9 is under 0.3. Two are equal where they hold the same numbers, as every value of the
 * geo layer compares.
 */
public class RouteProfilePoint(
    /** Kilometers from the start of the itinerary. */
    public val distanceKm: Double,
    /** Meters above sea level. */
    public val elevation: Double,
) {
    override fun equals(other: Any?): Boolean =
        other is RouteProfilePoint && sameNumber(other.distanceKm, distanceKm) && sameNumber(other.elevation, elevation)

    override fun hashCode(): Int = distanceKm.hashCode() * 31 + elevation.hashCode()

    override fun toString(): String = "RouteProfilePoint(distanceKm=$distanceKm, elevation=$elevation)"
}
