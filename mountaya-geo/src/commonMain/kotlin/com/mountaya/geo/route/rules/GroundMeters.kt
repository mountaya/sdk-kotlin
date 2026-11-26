package com.mountaya.geo

/**
 * The distance covered on the ground to reach each vertex of [route]'s line, in meters: the
 * itinerary's distance axis read at the route's vertices, one distance per vertex, ascending.
 *
 * A flight charges every leg of its line, the leg that joins two lines included, since the camera
 * travels it. The ground charges nothing for the leg that reaches a jump, so the vertex a jump
 * lands on stands at the distance of the vertex it left. A route with no jump answers its
 * [cumulativeMeters], and a jump at an index the line has no leg to, zero, under it or past the
 * line's last vertex, is no jump.
 */
internal fun groundMeters(route: RouteGeometry): List<Double> {
    val cumulative = cumulativeMeters(route.line)
    val jumps = route.jumps.filterTo(HashSet()) { it in 1..cumulative.lastIndex }
    val ground = ArrayList<Double>(cumulative.size)
    var jumped = 0.0
    for (index in cumulative.indices) {
        if (index in jumps) jumped += cumulative[index] - cumulative[index - 1]
        ground += cumulative[index] - jumped
    }
    return ground
}
