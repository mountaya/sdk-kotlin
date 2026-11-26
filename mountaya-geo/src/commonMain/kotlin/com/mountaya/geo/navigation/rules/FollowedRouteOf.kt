package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * followedRouteOf answers the route a traveller follows: [route]'s geometry, and the steps that tile
 * it (`geo.navigation.stepless-route`).
 *
 * A route whose steps tile its geometry is followed by them, unchanged. One with none, a `linestring`
 * segment's or one whose list was refused, is followed as two steps in the same vocabulary: a
 * `depart` from vertex 0 to the last, carrying the route's own duration, none where it states none,
 * and the geometry's great-circle length as `routeLengthMeters` measures it, then a `goal` on the
 * last vertex, of no distance and no duration. Neither names a way or an exit, so both are said in
 * their `Plain` form. Steps that do not tile the geometry are refused as `readDirectionsSteps` refuses
 * them, and the route is followed as one with none.
 *
 * The geometry is measured once, the departure's length read off the measure the route then holds, so following a
 * route costs one measurement of its line and no more (`route-measure` in the contract's `sdk/hot-paths.yaml`).
 *
 * @return The route followed, or null for a geometry with no vertex, which is no route to follow.
 */
public fun followedRouteOf(route: NavigationRoute): FollowedRoute? {
    val line = route.line
    val last = line.lastIndex
    if (last < 0) return null
    val meters = cumulativeMeters(line).toDoubleArray()
    if (stepsTile(route.steps, line.size)) return FollowedRoute(line, route.steps, meters)
    val departure =
        NavigationStep(
            fromIndex = 0,
            toIndex = last,
            maneuver = RestSchema.ManeuverType.Depart,
            distance = meters[last],
            duration = route.duration,
        )
    val arrival =
        NavigationStep(
            fromIndex = last,
            toIndex = last,
            maneuver = RestSchema.ManeuverType.Goal,
            distance = 0.0,
            duration = 0.0,
        )
    return FollowedRoute(line, listOf(departure, arrival), meters)
}
