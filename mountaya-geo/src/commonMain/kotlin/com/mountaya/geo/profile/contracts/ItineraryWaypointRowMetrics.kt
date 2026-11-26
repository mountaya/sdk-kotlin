package com.mountaya.geo

/**
 * ItineraryWaypointRowMetrics is what one waypoint row states (`geo.profile.waypoint-rows`): the running distance
 * to its place, the figures of the stretch that reaches it, the running climb and loss, and the place's own
 * altitude, a single reading, since a place is a point.
 *
 * Meters throughout, a loss stated positive, every figure but the distance null where there is none. Two are
 * equal where they hold the same figures, each number by the same-number relation, as every value of the geo
 * layer compares.
 */
public class ItineraryWaypointRowMetrics(
    /** The meters from the start of the itinerary to the place. */
    public val distance: Double,
    /** The meters covered since the previous place, or since the start for the first row. */
    public val sectionDistance: Double? = null,
    /** The meters climbed from the start of the itinerary to the place. */
    public val ascent: Double? = null,
    /** The meters lost from the start of the itinerary to the place, stated positive. */
    public val descent: Double? = null,
    /** The meters climbed since the previous place. */
    public val sectionAscent: Double? = null,
    /** The meters lost since the previous place, stated positive. */
    public val sectionDescent: Double? = null,
    /** The curve's altitude at the place, in meters above sea level. */
    public val altitude: Double? = null,
) {
    /** Every figure but the distance, in the order the type declares them. */
    private val numbers: List<Double?>
        get() = listOf(sectionDistance, ascent, descent, sectionAscent, sectionDescent, altitude)

    override fun equals(other: Any?): Boolean =
        other is ItineraryWaypointRowMetrics &&
            sameNumber(other.distance, distance) &&
            other.numbers.zip(numbers).all { (a, b) -> sameNumberOrNull(a, b) }

    override fun hashCode(): Int = distance.hashCode() * 31 + numbers.hashCode()

    override fun toString(): String =
        "ItineraryWaypointRowMetrics(distance=$distance, sectionDistance=$sectionDistance, ascent=$ascent, " +
            "descent=$descent, sectionAscent=$sectionAscent, sectionDescent=$sectionDescent, altitude=$altitude)"
}
