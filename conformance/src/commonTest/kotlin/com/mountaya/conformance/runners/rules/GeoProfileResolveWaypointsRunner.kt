package com.mountaya.conformance

import com.mountaya.geo.ProfileWaypoint
import com.mountaya.geo.WAYPOINT_BELONGING_METERS
import com.mountaya.geo.WAYPOINT_PROXIMITY_METERS
import com.mountaya.geo.resolveWaypoints
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.resolve-waypoints`: the case's `waypoints` placed on its `geometry`, each given at a
 * `lngLat` or at a `distance`, answered as the waypoint's id, the placement's key and its distance.
 */
object GeoProfileResolveWaypointsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            mapOf("belongingMeters" to WAYPOINT_BELONGING_METERS, "proximityMeters" to WAYPOINT_PROXIMITY_METERS),
        )
        val input = case.fields
        val waypoints =
            input.items("waypoints").map { item ->
                val waypoint = item as? FixtureValue.Object ?: throw AssertionError("a waypoint is not an object")
                val id = waypoint.text("id")
                if (waypoint["lngLat"] != null) {
                    ProfileWaypoint.AtPosition(id, fixturePosition(waypoint["lngLat"], "lngLat"))
                } else {
                    ProfileWaypoint.AtDistance(id, waypoint.number("distance"))
                }
            }
        val geometry = input["geometry"]?.takeUnless { it is FixtureValue.Null }?.let(::fixtureGeometry)
        return ConformanceOutcome.Value(
            FixtureValue.Array(
                resolveWaypoints(geometry, waypoints).map { placed ->
                    FixtureValue.Object(
                        listOf(
                            "distance" to FixtureValue.Number(placed.distance),
                            "id" to FixtureValue.Text(placed.waypoint.id),
                            "key" to FixtureValue.Text(placed.key),
                        ),
                    )
                },
            ),
        )
    }
}
