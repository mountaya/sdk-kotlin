package com.mountaya.geo

/**
 * ElevationSeries is an itinerary read for its profile: cumulative distance against altitude, the
 * input every other profile rule takes.
 *
 * [totalDistance] is the whole itinerary's, whether or not it carried elevation, so a flat 2D
 * itinerary still states how long it is, and [points] holds only the vertices that carried an
 * altitude, thinned to the series' ceiling. The altitude range is the whole line's, thinned or not.
 * Two are equal where they hold the same samples and the same numbers, as every value of the geo
 * layer compares.
 */
public class ElevationSeries(
    /** The samples, ascending by distance, each numbered by its place here. */
    public val points: List<ElevationPoint>,
    /** Meters, over every vertex walked, including the ones that carried no altitude. */
    public val totalDistance: Double,
    /** The lowest altitude any vertex carried, or null where none carried one. */
    public val minElevation: Double?,
    /** The highest altitude any vertex carried, or null where none carried one. */
    public val maxElevation: Double?,
) {
    /** Whether any vertex carried an altitude, which is whether the series has a sample. */
    public val hasElevation: Boolean get() = points.isNotEmpty()

    override fun equals(other: Any?): Boolean =
        other is ElevationSeries &&
            other.points == points &&
            sameNumber(other.totalDistance, totalDistance) &&
            sameNumberOrNull(other.minElevation, minElevation) &&
            sameNumberOrNull(other.maxElevation, maxElevation)

    override fun hashCode(): Int =
        ((points.hashCode() * 31 + totalDistance.hashCode()) * 31 + minElevation.hashCode()) * 31 +
            maxElevation.hashCode()

    override fun toString(): String =
        "ElevationSeries(points=$points, totalDistance=$totalDistance, minElevation=$minElevation, maxElevation=$maxElevation)"
}
