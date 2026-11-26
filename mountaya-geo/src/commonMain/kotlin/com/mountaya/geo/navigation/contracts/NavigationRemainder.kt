package com.mountaya.geo

/**
 * NavigationRemainder is what is left of a stretch of a route, to its next maneuver or to its
 * arrival: the measured length, and the time the steps state for it (`geo.navigation.progress`).
 *
 * Two are equal where both members are, each as the same number, `NaN` the same as `NaN` and 0
 * another than -0.
 */
public class NavigationRemainder(
    /** The length left, in meters, measured along the route's line. */
    public val distance: Double,
    /** The time left in seconds, or null where a step it reads states no duration. */
    public val duration: Double?,
) {
    override fun equals(other: Any?): Boolean =
        other is NavigationRemainder && sameNumber(other.distance, distance) &&
            sameNumberOrNull(other.duration, duration)

    override fun hashCode(): Int = distance.hashCode() * 31 + duration.hashCode()

    override fun toString(): String = "NavigationRemainder(distance=$distance, duration=$duration)"
}
