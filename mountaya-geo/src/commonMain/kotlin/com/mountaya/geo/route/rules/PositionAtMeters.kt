package com.mountaya.geo

/**
 * positionAtMeters is the position [meters] along [line], `[longitude, latitude]` in degrees:
 * interpolated between the two vertices whose [cumulativeMeters] bracket it, in proportion to the
 * distance between them. A distance at or under 0 is the first vertex, and one at or past the
 * line's length the last.
 *
 * A leg is interpolated the short way round, as its length is measured: on a leg written 179.9 then
 * -179.9 the position halfway is on the antimeridian, never on the prime meridian the two numbers
 * average to. Its longitude continues from the leg's first vertex, past 180 where the leg crosses
 * eastward and below -180 where it crosses westward, which a renderer reads as the same place, and
 * which `wrapLongitude` brings back where the range the wire writes is needed.
 *
 * A vertex that names no place is bridged, as [cumulativeMeters] reads it, and an altitude is not
 * carried into the answer. A line with no vertex that names a place, and a distance that is `NaN`,
 * have no position along them, and answer `[NaN, NaN]`, a position that names no place.
 *
 * Each call measures the line. A walk that reads a position along a route followed at every tick reads it with the
 * route instead, `positionAtMeters(route, meters)`, which measures nothing.
 */
public fun positionAtMeters(
    line: List<List<Double>>,
    meters: Double,
): List<Double> {
    val placed = line.filter(::namesPlace)
    if (placed.isEmpty() || meters.isNaN()) return listOf(Double.NaN, Double.NaN)
    val cumulative = cumulativeMeters(placed)
    return positionAlong(
        count = placed.size,
        meters = meters,
        distance = { cumulative[it] },
        longitude = { placed[it][0] },
        latitude = { placed[it][1] },
    )
}

/**
 * positionAtMeters is the position [meters] along [route]'s line, the very one `positionAtMeters(route.line, meters)`
 * answers, number for number, read off what the route holds rather than its line measured again: the distance to
 * each vertex, [FollowedRoute.measured], measured once as the route was followed, and the vertices that name a place,
 * kept then too, the two that bracket the distance found by a binary search over those distances. The distance a
 * vertex that names a place stands at on the whole line is the one it stands at on the line of those vertices alone,
 * since a vertex that names no place adds nothing to it, so the search reads the same distances the line's
 * measurement would.
 *
 * It measures nothing, and allocates nothing but its answer, so a replay or an engine of the host's own that reads a
 * position along the route followed at every tick pays a search, never a measurement of the whole route
 * (`replay-measure` in the contract's `sdk/hot-paths.yaml`).
 */
public fun positionAtMeters(
    route: FollowedRoute,
    meters: Double,
): List<Double> {
    val placed = route.placed
    if (placed.isEmpty() || meters.isNaN()) return listOf(Double.NaN, Double.NaN)
    val measured = route.meters
    val longitudes = route.longitudes
    val latitudes = route.latitudes
    return positionAlong(
        count = placed.size,
        meters = meters,
        distance = { measured[placed[it]] },
        longitude = { longitudes[it] },
        latitude = { latitudes[it] },
    )
}

/**
 * The position [meters] along the [count] vertices that name a place, one at least, each read by its index among
 * them: the [distance] to it along the line, ascending, and its [longitude] and [latitude]. Both readings of a line
 * place a position through it, so the two answer the same numbers.
 */
private inline fun positionAlong(
    count: Int,
    meters: Double,
    distance: (Int) -> Double,
    longitude: (Int) -> Double,
    latitude: (Int) -> Double,
): List<Double> {
    val last = count - 1
    val total = distance(last)
    if (meters <= 0.0 || total == 0.0) return listOf(longitude(0), latitude(0))
    if (meters >= total) return listOf(longitude(last), latitude(last))

    // The last vertex at or before the distance: distance(low) <= meters < distance(high)
    // holds throughout, so the leg found has a length.
    var low = 0
    var high = last
    while (low < high - 1) {
        val middle = (low + high) ushr 1
        if (distance(middle) <= meters) low = middle else high = middle
    }
    val from = longitude(low)
    val ratio = (meters - distance(low)) / (distance(high) - distance(low))
    return listOf(
        from + longitudeDelta(from, longitude(high)) * ratio,
        latitude(low) + (latitude(high) - latitude(low)) * ratio,
    )
}
