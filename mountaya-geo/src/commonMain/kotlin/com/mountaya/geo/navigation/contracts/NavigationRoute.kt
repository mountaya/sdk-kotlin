package com.mountaya.geo

/**
 * NavigationRoute is a route as its source answers it: the geometry to follow, its steps as an
 * adapter read them, and the route's own estimate of its duration.
 *
 * A host's route source answers one, built from the Data API's `directions` or from a saved
 * itinerary's segment, its steps read by `readDirectionsSteps` or `readItineraryDirectionsSteps`; an
 * engine follows it as `followedRouteOf` answers it.
 *
 * Two are equal where every vertex, every step and the duration are, each figure as the same number,
 * `NaN` the same as `NaN` and 0 another than -0.
 */
public class NavigationRoute(
    /**
     * The route's geometry, `[longitude, latitude]` vertices in degrees with an optional altitude,
     * as GeoJSON writes a line: the index basis of its steps.
     */
    public val line: List<List<Double>>,
    /** The route's steps, in travel order: empty where it has none or its list was refused. */
    public val steps: List<NavigationStep> = emptyList(),
    /** The route's own estimate of its duration in seconds, or null where it states none. */
    public val duration: Double? = null,
) {
    override fun equals(other: Any?): Boolean =
        this === other ||
            (
                other is NavigationRoute && other.line.size == line.size &&
                    line.indices.all { sameNumbers(other.line[it], line[it]) } && other.steps == steps &&
                    sameNumberOrNull(other.duration, duration)
            )

    override fun hashCode(): Int = line.hashCode() * 31 + steps.hashCode()

    override fun toString(): String = "NavigationRoute(line=$line, steps=$steps, duration=$duration)"
}
