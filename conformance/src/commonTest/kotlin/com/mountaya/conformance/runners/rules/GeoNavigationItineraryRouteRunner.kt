package com.mountaya.conformance

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.NavigationRoute
import com.mountaya.geo.WAYPOINT_PROXIMITY_METERS
import com.mountaya.geo.readItineraryRoute
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * `geo.navigation.itinerary-route`: the route the public `readItineraryRoute` joins over the case's `segments` and
 * `waypoints`, each built as the REST API answers it. A segment holds the `position`, `geojson`, `steps` and
 * `duration` the case writes, its steps decoded by the wire's own serializer from REST's JSON, and its `id` and
 * `mode`, a placeholder for every other member the wire requires; a waypoint holds the `position`, `name`, `geojson`,
 * `id` and `mode` the case writes. The answer is the route's `line`, its `steps` in the steps vocabulary and its
 * `duration`, or null where no route is joined. The fixture's `proximityMeters` is held to
 * `WAYPOINT_PROXIMITY_METERS`.
 */
object GeoNavigationItineraryRouteRunner : ConformanceRunner {
    private val json = Json

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(case, rules = mapOf("proximityMeters" to WAYPOINT_PROXIMITY_METERS))
        val input = case.fields
        val segments =
            input.items("segments").map { item ->
                val segment = item as? FixtureValue.Object ?: throw AssertionError("a segment is not an object")
                val steps =
                    segment["steps"]?.takeUnless { it is FixtureValue.Null }?.let { written ->
                        json.decodeFromJsonElement(
                            ListSerializer(RestSchema.ItineraryDirectionsStep.serializer()),
                            fixtureJson(written),
                        )
                    }
                fixtureSegment(segment, steps)
            }
        val waypoints =
            input["waypoints"]?.let { input.items("waypoints") }.orEmpty().map { item ->
                val waypoint = item as? FixtureValue.Object ?: throw AssertionError("a waypoint is not an object")
                fixtureWaypoint(
                    id = waypoint.textOrNull("id") ?: "waypoint",
                    name = waypoint.textOrNull("name"),
                    position = wholeOrNull(waypoint, "position") ?: 0,
                    geojson = waypoint["geojson"]?.takeUnless { it is FixtureValue.Null }?.let(::fixtureGeometry),
                    mode =
                        waypoint.textOrNull("mode")?.let { RestSchema.WaypointMode(it) }
                            ?: RestSchema.WaypointMode.Point,
                )
            }
        val route = readItineraryRoute(segments, waypoints) ?: return ConformanceOutcome.Value(FixtureValue.Null)
        return ConformanceOutcome.Value(routeValue(route))
    }

    /** [route] as the fixture writes one: its duration, its line and its steps. */
    private fun routeValue(route: NavigationRoute): FixtureValue =
        FixtureValue.Object(
            listOf(
                "duration" to numberOrNull(route.duration),
                "line" to
                    FixtureValue.Array(
                        route.line.map { vertex -> FixtureValue.Array(vertex.map { FixtureValue.Number(it) }) },
                    ),
                "steps" to FixtureValue.Array(route.steps.map(::navigationStepValue)),
            ),
        )
}
