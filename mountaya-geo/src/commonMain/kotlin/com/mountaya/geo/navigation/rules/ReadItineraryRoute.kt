package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.GeoJsonGeometryCollection
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.generated.RestSchema

/**
 * readItineraryRoute joins an itinerary's [segments], as the REST API answers them, into the one route a traveller
 * follows over all of them, its arrivals before the last said as the [waypoints] passed on the way
 * (`geo.navigation.itinerary-route`).
 *
 * The segments are taken in chain order, a stable order by `position`, two at one position keeping the order they
 * are listed in, and so are the waypoints. A segment's line is its LineString: its geometry where it is one, else the
 * first LineString its GeometryCollection holds, the MultiPoint of its vias beside it left out. A segment holding no
 * line, or a line of no vertex, is passed over, its duration with it. Each segment holding a line is followed as
 * `followedRouteOf` follows a route: by its own steps, read as `readItineraryDirectionsSteps` reads them against its
 * line, where they tile it, and otherwise as a departure over its line and an arrival on its last vertex.
 *
 * The last step of every segment holding a line but the last, where it is a `goal`, is said as a waypoint passed on
 * the way, [waypointManeuver], keeping its range and its figures, its name the one of the waypoint standing where
 * the segment's line ends: the nearest whose Point lies within [WAYPOINT_PROXIMITY_METERS] of that vertex, of two as
 * near the first in chain order, among those whose name is neither empty nor `-`, and none where none stands within
 * reach. A segment's first step that is a `depart` naming no way, right after such a waypoint, says nothing the
 * waypoint did not, since a step carries no heading: it is folded into the waypoint, which runs on to where the
 * departure ends, its distance and its duration the two summed, none where either is none. A departure naming a way
 * is kept.
 *
 * The lines are laid end to end, every vertex as written. A line that starts on the very longitude and latitude the
 * line before it ends on drops that first vertex, so the joint is one vertex, the earlier line's, its elevation
 * included; a line that starts anywhere else keeps it, and its first step reaches back across the gap to the vertex
 * the line before it ends on. Every step is moved on by the index its line starts at in the joined line, so the joined
 * steps tile the joined line. The duration is the segments' durations summed in chain order, none where a segment
 * holding a line states none.
 *
 * @return The joined route, or null where no segment holds a line, which is no route to follow.
 */
public fun readItineraryRoute(
    segments: List<RestSchema.ItinerarySegment>,
    waypoints: List<RestSchema.ItineraryWaypoint>,
): NavigationRoute? {
    // The chain order, a sort that keeps the listed order of two at one position, for segments and waypoints alike.
    val followed =
        segments.sortedBy { it.position }.mapNotNull { segment ->
            val line = segmentLine(segment.geojson) ?: return@mapNotNull null
            val steps = readItineraryDirectionsSteps(segment.steps, line.size).steps
            followedRouteOf(NavigationRoute(line, steps, segment.duration))?.let { route -> route to segment.duration }
        }
    if (followed.isEmpty()) return null
    val places =
        waypoints.sortedBy { it.position }.mapNotNull { waypoint ->
            val name = wayName(waypoint.name) ?: return@mapNotNull null
            val point = (waypoint.geojson as? GeoJsonPoint)?.coordinates ?: return@mapNotNull null
            name to point
        }

    val line = ArrayList<List<Double>>()
    val steps = ArrayList<NavigationStep>()
    var duration: Double? = 0.0
    for ((chained, pair) in followed.withIndex()) {
        val (route, own) = pair
        duration = if (own == null) null else duration?.plus(own)
        val end = line.lastOrNull()
        val start = route.line.first()
        val offset: Int
        val reachesBack: Boolean
        if (end == null) {
            offset = 0
            reachesBack = false
            line += route.line
        } else if (isSamePlace(end, start)) {
            offset = line.size - 1
            reachesBack = false
            line += route.line.drop(1)
        } else {
            offset = line.size
            reachesBack = true
            line += route.line
        }
        val moved =
            route.steps.mapIndexed { index, step ->
                val from = if (index == 0 && reachesBack) offset - 1 else step.fromIndex + offset
                step.spanning(from, step.toIndex + offset)
            }
        val said =
            if (chained == followed.lastIndex || moved.last().maneuver != RestSchema.ManeuverType.Goal) {
                moved
            } else {
                val arrival = moved.last()
                moved.dropLast(1) + arrival.said(waypointManeuver, nearestNamed(places, route.line.last()))
            }
        val previous = steps.lastOrNull()
        val opening = said.first()
        if (previous != null && previous.maneuver == waypointManeuver &&
            opening.maneuver == RestSchema.ManeuverType.Depart && opening.name == null
        ) {
            steps[steps.lastIndex] =
                NavigationStep(
                    fromIndex = previous.fromIndex,
                    toIndex = opening.toIndex,
                    maneuver = waypointManeuver,
                    name = previous.name,
                    distance = sumOrNull(previous.distance, opening.distance),
                    duration = sumOrNull(previous.duration, opening.duration),
                    exitNumber = previous.exitNumber,
                )
            steps += said.drop(1)
        } else {
            steps += said
        }
    }
    return NavigationRoute(line, steps, duration)
}

/**
 * The line of a segment's [geometry]: the geometry where it is a LineString, else the first LineString its
 * GeometryCollection holds, or null where it holds none or one of no vertex.
 */
private fun segmentLine(geometry: GeoJsonGeometry?): List<List<Double>>? {
    val line =
        when (geometry) {
            is GeoJsonLineString -> {
                geometry.coordinates
            }

            is GeoJsonGeometryCollection -> {
                geometry.geometries.firstNotNullOfOrNull { (it as? GeoJsonLineString)?.coordinates }
            }

            else -> {
                null
            }
        }
    return line?.takeIf { it.isNotEmpty() }
}

/** Whether two vertices stand on the very same longitude and latitude, whatever their elevations. */
private fun isSamePlace(
    first: List<Double>,
    second: List<Double>,
): Boolean = first.size >= 2 && second.size >= 2 && first[0] == second[0] && first[1] == second[1]

/**
 * The name of the place of [places], each a name and its Point in chain order, nearest [vertex] within
 * [WAYPOINT_PROXIMITY_METERS], of two as near the first, or null where none stands within reach.
 */
private fun nearestNamed(
    places: List<Pair<String, List<Double>>>,
    vertex: List<Double>,
): String? {
    var nearest: String? = null
    var reach = WAYPOINT_PROXIMITY_METERS
    for ((name, point) in places) {
        val meters = haversineMeters(point, vertex)
        if (!(meters <= reach)) continue
        if (nearest != null && meters == reach) continue
        nearest = name
        reach = meters
    }
    return nearest
}

/** The two figures summed, or null where either is none. */
private fun sumOrNull(
    first: Double?,
    second: Double?,
): Double? = if (first == null || second == null) null else first + second

/** [this] step over the range [fromIndex] to [toIndex], every other member as it was. */
private fun NavigationStep.spanning(
    fromIndex: Int,
    toIndex: Int,
): NavigationStep = NavigationStep(fromIndex, toIndex, maneuver, name, distance, duration, exitNumber)

/** [this] step said as [maneuver] by [name], its range and its figures kept. */
private fun NavigationStep.said(
    maneuver: RestSchema.ManeuverType,
    name: String?,
): NavigationStep = NavigationStep(fromIndex, toIndex, maneuver, name, distance, duration, exitNumber)
