package com.mountaya.geo

/**
 * navigationProgressAt reads where a traveller at [position] stands on the [route] they follow, and
 * how far and how long they have left, to the next maneuver and to the arrival
 * (`geo.navigation.progress`).
 *
 * Every distance along the route is its great-circle measurement, the [cumulativeMeters] of its line,
 * and a step's own `distance` is never read, so the two remainders are lengths of one line and the
 * second is never shorter than the first. Each stretch of the line, from one vertex to the next, is
 * placed in the local plane on the mean sphere around the position, as a corridor is measured, and
 * the position is projected onto it, clamped to its ends; a vertex that names no place ends no
 * stretch, which runs from the vertex before it that names one to the next that does, as every walk
 * of a line reads it. Only a stretch that ends at or past [previous] is considered, so the return leg
 * of an out-and-back is not mistaken for the way out and a loop's start stays its start until the
 * traveller has come round. Of those, the nearest foot wins, and of feet equally near, the one on the
 * earliest stretch, so a traveller standing on a maneuver's vertex is told that maneuver rather than
 * the one after it. A foot at fraction t of the stretch from vertex i to vertex j stands at
 * `d[i] + t * (d[j] - d[i])`, and exactly at `d[j]` at its far end, and progress never moves back: a
 * foot behind [previous] stands at [previous].
 *
 * The current step is the one whose range holds the foot's stretch, its `fromIndex` at most i and its
 * `toIndex` greater than i, so a step of one vertex is never current, and the next maneuver is the
 * following step's, none after the last. A route with no stretch, one of a single vertex, stands at
 * 0, in its last step. A remainder's duration is the current step's duration times the share of its
 * measured length still ahead, none where it measures nothing, plus, to the arrival, the whole
 * duration of every later step, added in order, and it is null where a step it reads states none.
 *
 * The line is read as the route holds it, measured and unboxed once when it was followed, so a reading
 * allocates nothing per stretch.
 *
 * @param route The route followed, as [followedRouteOf] answers it.
 * @param position The traveller's `[longitude, latitude]`, in degrees. One that names no place has
 * no foot to measure from: it reads `NaN` for every figure it measures, standing in the step that
 * holds the first stretch ahead of [previous].
 * @param previous The progress the last position read, in meters along the route, 0 on a route just
 * received. It is held within the route's length, and `NaN` reads as 0.
 */
public fun navigationProgressAt(
    route: FollowedRoute,
    position: List<Double>,
    previous: Double,
): NavigationProgress = routeReadingAt(route, position, previous, measuring = false).progress
