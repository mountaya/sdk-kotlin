package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry
import kotlin.math.abs

/**
 * WAYPOINT_PROXIMITY_METERS is how near the line must come to a place for the approach to count as
 * a pass, the `proximityMeters` `geo/resolve-waypoints.json` declares. Tight on purpose: it is what
 * separates one pass from the next, so a lollipop's junction is marked twice. It is also how near to
 * where a segment's line ends a waypoint stands, at most, for the arrival a route joined over an
 * itinerary's segments passes there to be said by its name, the `proximityMeters`
 * `geo/navigation-itinerary-route.json` declares (`readItineraryRoute`).
 */
public const val WAYPOINT_PROXIMITY_METERS: Double = 60.0

/**
 * WAYPOINT_BELONGING_METERS is how far off the line a place may stand and still be one of the
 * itinerary's own, the `belongingMeters` `geo/resolve-waypoints.json` declares: an aid station in a
 * village the trail passes stands a few hundred meters off it.
 */
public const val WAYPOINT_BELONGING_METERS: Double = 2_000.0

/**
 * resolveWaypoints places [waypoints] on the distance axis of [geometry], walked as an elevation
 * series walks it, in meters from its start.
 *
 * A waypoint given at a distance stands there. One given at a position stands at every pass of the
 * line within [WAYPOINT_PROXIMITY_METERS], each run of near legs one pass at its nearest point; one
 * with no such pass stands at its single nearest approach within [WAYPOINT_BELONGING_METERS], and one
 * farther than that, or at a position that names no place, is not placed.
 *
 * A forward sweep in the order the waypoints are listed gives each its primary pass: the first at or
 * after the pass the waypoint before it took, or its last where every one lies behind. So a loop's
 * start and finish stand at zero and at the total. Any other pass of a waypoint is placed only where
 * no primary pass lies within [WAYPOINT_PROXIMITY_METERS] of it. Placements are ordered by distance,
 * then by the order the waypoints are listed, and each is keyed `id#pass`, counting that id's
 * passes from zero.
 *
 * Each waypoint is the one at its place in the list: two given the same id are two waypoints, and
 * their keys count passes of that id across both. A distance that is not a number places nothing.
 */
public fun resolveWaypoints(
    geometry: GeoJsonGeometry?,
    waypoints: List<ProfileWaypoint>,
): List<PlacedWaypoint> =
    waypointPasses(geometry, waypoints).map { pass ->
        PlacedWaypoint(pass.key, waypoints[pass.listed], pass.distance)
    }

/**
 * WaypointPass is one placement of a listed waypoint as [resolveWaypoints] places it: its key, the waypoint's
 * place in the list, its distance, and whether it is the waypoint's primary pass, the one the forward sweep
 * gives it, which the figures stated for a place belong to.
 */
internal class WaypointPass(
    val key: String,
    val listed: Int,
    val distance: Double,
    val isPrimary: Boolean,
)

/** waypointPasses places [waypoints] as [resolveWaypoints] states, and says of each placement whether it is its waypoint's primary pass. */
internal fun waypointPasses(
    geometry: GeoJsonGeometry?,
    waypoints: List<ProfileWaypoint>,
): List<WaypointPass> {
    val legs = walkLegs(linesOf(geometry))
    // Every distance each waypoint could stand at, ascending.
    val candidates =
        waypoints.map { waypoint ->
            when (waypoint) {
                is ProfileWaypoint.AtDistance -> {
                    listOfNotNull(waypoint.distance.takeUnless { it.isNaN() })
                }

                is ProfileWaypoint.AtPosition -> {
                    val near = markerPasses(legs, waypoint.position, WAYPOINT_PROXIMITY_METERS)
                    val passes =
                        near.ifEmpty {
                            // The radius says how many times the line passes a place, not whether it
                            // is one of its own: a place with no pass stands at its nearest approach.
                            listOfNotNull(
                                markerPasses(
                                    legs,
                                    waypoint.position,
                                    WAYPOINT_BELONGING_METERS,
                                ).minByOrNull { it.meters },
                            )
                        }
                    passes.map { it.distance }.sorted()
                }
            }
        }

    // The forward sweep: how far the itinerary has been read to, so a waypoint listed after another
    // cannot be placed before it while it has a later pass to take.
    val primaries = arrayOfNulls<Double>(waypoints.size)
    var reached = Double.NEGATIVE_INFINITY
    for (index in waypoints.indices) {
        val list = candidates[index]
        if (list.isEmpty()) continue
        val primary = list.firstOrNull { it >= reached } ?: list.last()
        primaries[index] = primary
        reached = maxOf(reached, primary)
    }
    val claimed = primaries.filterNotNull()

    val placed = ArrayList<Placement>()
    for (index in waypoints.indices) {
        val primary = primaries[index] ?: continue
        placed += Placement(index, primary, isPrimary = true)
        for (distance in candidates[index]) {
            if (distance != primary && claimed.none { abs(it - distance) <= WAYPOINT_PROXIMITY_METERS }) {
                placed += Placement(index, distance, isPrimary = false)
            }
        }
    }

    val passes = HashMap<String, Int>()
    return placed
        .sortedWith(compareBy<Placement> { it.distance }.thenBy { it.waypoint })
        .map { placement ->
            val waypoint = waypoints[placement.waypoint]
            val pass = passes[waypoint.id] ?: 0
            passes[waypoint.id] = pass + 1
            WaypointPass("${waypoint.id}#$pass", placement.waypoint, placement.distance, placement.isPrimary)
        }
}

/** One placement before it is keyed: the waypoint's place in the list, its distance, and whether it is the waypoint's primary pass. */
private class Placement(
    val waypoint: Int,
    val distance: Double,
    val isPrimary: Boolean,
)
