package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * NavigationStep is one turn-by-turn step of a route, in the one vocabulary every navigation rule
 * reads: REST's, whichever surface wrote the route (`geo.navigation.steps`).
 *
 * A step covers the inclusive range of the route's vertices from [fromIndex] to [toIndex], and its
 * [maneuver] is the move a traveller makes where it starts. Its [distance] and [duration] are the
 * figures its surface sent: no rule reads [distance] as a distance, since every distance along a
 * route is measured on its line, and [duration] is what the time left is read from.
 *
 * A step read from either surface (`readDirectionsSteps`, `readItineraryDirectionsSteps`) names a
 * maneuver of the contract's set, `unknown` for one outside it, and no name for an unnamed way; a
 * step written by hand may hold anything, and the rules read it the same way.
 *
 * Two are equal where every member is, each figure as the same number, `NaN` the same as `NaN` and
 * 0 another than -0, as every value of the geo layer compares.
 */
public class NavigationStep(
    /** The first vertex the step covers, an index into the route's geometry. */
    public val fromIndex: Int,
    /** The last vertex the step covers, an index into the same geometry. */
    public val toIndex: Int,
    /** What the step asks the traveller to do where it starts, a member of REST's `ManeuverType`. */
    public val maneuver: RestSchema.ManeuverType,
    /** The name of the way the step follows, or null where the way has none. */
    public val name: String? = null,
    /** The step's length in meters as its surface states it, or null where it states none. */
    public val distance: Double? = null,
    /** The step's duration in seconds as its surface states it, or null where it states none. */
    public val duration: Double? = null,
    /** The roundabout exit to take as its surface states it, or null where it states none. */
    public val exitNumber: Int? = null,
) {
    override fun equals(other: Any?): Boolean =
        other is NavigationStep && other.fromIndex == fromIndex && other.toIndex == toIndex &&
            other.maneuver == maneuver && other.name == name && sameNumberOrNull(other.distance, distance) &&
            sameNumberOrNull(other.duration, duration) && other.exitNumber == exitNumber

    override fun hashCode(): Int = ((fromIndex * 31 + toIndex) * 31 + maneuver.hashCode()) * 31 + name.hashCode()

    override fun toString(): String =
        "NavigationStep($fromIndex..$toIndex, ${maneuver.value}, name=$name, distance=$distance, " +
            "duration=$duration, exitNumber=$exitNumber)"
}
