package com.mountaya.geo

/**
 * ElevationPoint is one sample of an elevation series: how far along the itinerary it stands, the
 * ground's altitude there, where it is, and its place in the series.
 *
 * Two are equal where they hold the same numbers, `NaN` the same as `NaN` and 0 another than -0, as
 * every value of the geo layer compares.
 */
public class ElevationPoint(
    /** Meters from the start of the itinerary, along the ground. */
    public val distance: Double,
    /** Meters above sea level. */
    public val elevation: Double,
    /** Where the sample stands, `[longitude, latitude]` in degrees, so a hover can place a marker. */
    public val lngLat: List<Double>,
    /** The sample's place in its series, from 0, which a chart quantizes a pointer position to. */
    public val index: Int,
) {
    override fun equals(other: Any?): Boolean =
        other is ElevationPoint &&
            sameNumber(other.distance, distance) &&
            sameNumber(other.elevation, elevation) &&
            sameNumbers(other.lngLat, lngLat) &&
            other.index == index

    override fun hashCode(): Int =
        ((distance.hashCode() * 31 + elevation.hashCode()) * 31 + lngLat.hashCode()) * 31 + index

    override fun toString(): String =
        "ElevationPoint(distance=$distance, elevation=$elevation, lngLat=$lngLat, index=$index)"
}
