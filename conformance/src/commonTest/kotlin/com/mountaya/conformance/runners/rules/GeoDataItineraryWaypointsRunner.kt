package com.mountaya.conformance

import com.mountaya.geo.itineraryWaypoints
import kotlinx.coroutines.test.TestScope

/**
 * `geo.data.itinerary-waypoints`: the places of the case's `waypoints`. Each waypoint is built as the REST
 * API's, every member read in the shape the wire's type holds and left out in any other, as no decoder of
 * the wire's hands one over in another shape (`CONTRACT-GAPS.md`): what the fixture states of a member in
 * another shape is what the type itself holds of it, and the rule reads the rest.
 */
object GeoDataItineraryWaypointsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val waypoints =
            case.fields.items("waypoints").map {
                fixtureItineraryWaypoint(
                    it as? FixtureValue.Object ?: throw AssertionError("a waypoint is not an object"),
                )
            }
        return ConformanceOutcome.Value(FixtureValue.Array(itineraryWaypoints(waypoints).map(::writtenPlace)))
    }
}
