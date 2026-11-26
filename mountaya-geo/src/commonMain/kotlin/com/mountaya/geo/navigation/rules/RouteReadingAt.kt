package com.mountaya.geo

import kotlin.math.max

/**
 * routeReadingAt reads a traveller at [position] against the [route] they follow in one walk of its
 * line: the progress [navigationProgressAt] reads, from [previous], and the distance to the line
 * `metersToRoute` measures, which [isOffRoute] then reads. Each stretch is placed around the position
 * once, by the one placement both rules stand on, over the line's coordinates as the route holds them
 * unboxed, so the walk allocates nothing per stretch and answers both rules' own numbers.
 *
 * The walk is the whole line, every fix. The distance is a minimum over every stretch, and a stretch
 * far along the route can pass as near the traveller as one beside their last progress wherever the
 * route loops back, so no window opened around [previous] holds the full walk's answer for every route;
 * the progress already reads only the stretches that end at or past [previous].
 *
 * Where [measuring] is false the distance is not measured, so a stretch behind [previous] is never
 * placed, and it answers [Double.POSITIVE_INFINITY]; the distance is that too for a position that names
 * no place, which every corridor refuses.
 */
internal fun routeReadingAt(
    route: FollowedRoute,
    position: List<Double>,
    previous: Double,
    measuring: Boolean = true,
): RouteReading {
    val steps = route.steps
    val meters = route.meters
    val placed = route.placed
    val longitudes = route.longitudes
    val latitudes = route.latitudes
    val total = if (meters.isEmpty()) 0.0 else meters[meters.size - 1]
    val behind = if (previous.isNaN()) 0.0 else previous.coerceIn(0.0, total)
    // A position written short is read as one at no number, so every figure it measures is `NaN`.
    val pointLongitude = position.getOrElse(0) { Double.NaN }
    val pointLatitude = position.getOrElse(1) { Double.NaN }
    val metersPerDegreeLongitude = metersPerDegreeLongitudeAt(pointLatitude)
    val measures = measuring && namesPlace(position)

    var stretchStart = -1
    var stretchEnd = -1
    var nearest = Double.POSITIVE_INFINITY
    var fraction = 0.0
    var closest = Double.POSITIVE_INFINITY
    for (end in 1 until placed.size) {
        val start = end - 1
        // A stretch already passed is never projected onto, and is placed for the distance alone.
        val ahead = !(meters[placed[end]] < behind)
        if (!ahead && !measures) continue
        footAround(
            pointLongitude,
            pointLatitude,
            metersPerDegreeLongitude,
            longitudes[start],
            latitudes[start],
            longitudes[end],
            latitudes[end],
        ) { distance, at ->
            if (measures && distance < closest) closest = distance
            // Strictly nearer, so of feet equally near the earliest stretch's stands.
            if (ahead && (stretchStart < 0 || distance < nearest)) {
                stretchStart = placed[start]
                stretchEnd = placed[end]
                nearest = distance
                fraction = at
            }
        }
    }

    var along = 0.0
    if (stretchStart >= 0) {
        val start = meters[stretchStart]
        val end = meters[stretchEnd]
        // A foot on a stretch's far end stands exactly where the next stretch starts, so a traveller
        // on a maneuver's vertex is told no distance to it, rather than a rounding of one.
        val foot = if (fraction == 1.0) end else start + fraction * (end - start)
        along = max(foot, behind)
    }

    val holding = steps.indexOfFirst { it.fromIndex <= stretchStart && stretchStart < it.toIndex }
    val step = if (holding < 0) steps.lastIndex else holding
    val current = steps.getOrNull(step)
    val stepStart = if (current == null) 0.0 else meters.getOrNull(current.fromIndex) ?: 0.0
    val stepEnd = if (current == null) total else meters.getOrNull(current.toIndex) ?: total
    val toManeuverDistance = max(0.0, stepEnd - along)
    val stepLength = stepEnd - stepStart
    val share = if (stepLength > 0.0) toManeuverDistance / stepLength else 0.0
    val toManeuverDuration = current?.duration?.let { it * share }

    var toArrivalDuration = toManeuverDuration
    for (later in step + 1 until steps.size) {
        val duration = steps[later].duration
        toArrivalDuration = if (toArrivalDuration == null || duration == null) null else toArrivalDuration + duration
    }

    val progress =
        NavigationProgress(
            along = along,
            step = step,
            next = if (step + 1 < steps.size) step + 1 else null,
            toManeuver = NavigationRemainder(toManeuverDistance, toManeuverDuration),
            toArrival = NavigationRemainder(max(0.0, total - along), toArrivalDuration),
        )
    return RouteReading(progress, closest)
}
