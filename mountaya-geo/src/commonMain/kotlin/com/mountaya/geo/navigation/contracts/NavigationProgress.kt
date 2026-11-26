package com.mountaya.geo

/**
 * NavigationProgress is where a traveller stands on the route they follow, and what is left of it,
 * to the next maneuver and to the arrival (`geo.navigation.progress`): every number a reader is told
 * about a navigation, the same in every SDK for the same route and position.
 *
 * Two are equal where every member is, each figure as the same number, `NaN` the same as `NaN` and 0
 * another than -0.
 */
public class NavigationProgress(
    /** How far along the route the traveller stands, in meters from its first vertex. */
    public val along: Double,
    /** The index of the step the traveller is in, into the followed route's steps. */
    public val step: Int,
    /** The index of the step after it, whose maneuver is the next, or null after the last. */
    public val next: Int?,
    /** What is left to the end of the current step, where the next maneuver is. */
    public val toManeuver: NavigationRemainder,
    /** What is left to the end of the route. */
    public val toArrival: NavigationRemainder,
) {
    override fun equals(other: Any?): Boolean =
        other is NavigationProgress && sameNumber(other.along, along) && other.step == step && other.next == next &&
            other.toManeuver == toManeuver && other.toArrival == toArrival

    override fun hashCode(): Int = ((along.hashCode() * 31 + step) * 31 + (next ?: -1)) * 31 + toArrival.hashCode()

    override fun toString(): String =
        "NavigationProgress(along=$along, step=$step, next=$next, toManeuver=$toManeuver, toArrival=$toArrival)"
}
