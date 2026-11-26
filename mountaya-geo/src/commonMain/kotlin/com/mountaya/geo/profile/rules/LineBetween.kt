package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.GeoJsonLineString

/**
 * lineBetween is the stretch of an itinerary between [from] and [to] meters from its start, cut from the samples
 * of its series, [points], as the line a map lights (`geo.profile.line-between`): the samples the chart drew, so a
 * leg emphasised on a map is the leg stated in the row beside it.
 *
 * The stretch is held to the samples, so one past them stops at the first or the last. The line starts where the
 * stretch starts and ends where it ends, each read on the line between the two samples either side of it, the
 * short way round across the antimeridian with its longitude brought within [-180, 180], and holds every sample
 * strictly between the two; so a stretch shorter than the gap between two samples is still a line. Where two
 * lines meet at one distance, a stretch starting there starts on the later line and one ending there ends on the
 * earlier. Null where the stretch holds no length of the curve: given backwards, of no length or wholly past the
 * samples, and for an empty series.
 */
public fun lineBetween(
    points: List<ElevationPoint>,
    from: Double,
    to: Double,
): GeoJsonGeometry? {
    val first = points.firstOrNull() ?: return null
    val last = points.last()
    val low = maxOf(from, first.distance)
    val high = minOf(to, last.distance)
    if (!(high > low)) return null
    val setsOutFrom = startIndexAt(points, low)
    val start =
        if (setsOutFrom >= 0) {
            points[setsOutFrom].lngLat.take(2)
        } else {
            positionAt(points, low)
        } ?: return null
    val end = positionAt(points, high) ?: return null
    val coordinates = ArrayList<List<Double>>()
    coordinates += start
    var index = firstIndexAtOrPast(points, low)
    while (index < points.size && points[index].distance < high) {
        if (points[index].distance > low) coordinates += points[index].lngLat.take(2)
        index += 1
    }
    coordinates += end
    return GeoJsonLineString(coordinates)
}

/**
 * positionAt is where the curve of [points] stands on the ground [distance] meters from the itinerary's start,
 * `[longitude, latitude]`, read on the line between the two samples either side of it as [elevationAt] reads the
 * altitude there, the short way round across the antimeridian, its longitude brought within [-180, 180]. Null for
 * no sample, and for a distance that is not a number.
 */
internal fun positionAt(
    points: List<ElevationPoint>,
    distance: Double,
): List<Double>? {
    val first = points.firstOrNull() ?: return null
    val last = points.last()
    if (distance.isNaN()) return null
    if (distance <= first.distance) return first.lngLat.take(2)
    if (distance >= last.distance) return last.lngLat.take(2)
    val index = firstIndexAtOrPast(points, distance)
    if (index <= 0 || index >= points.size) return null
    val after = points[index]
    val before = points[index - 1]
    if (after.distance == distance) return after.lngLat.take(2)
    val run = after.distance - before.distance
    val ratio = if (run > 0.0) (distance - before.distance) / run else 0.0
    if (ratio == 0.0) return before.lngLat.take(2)
    return listOf(
        wrapLongitudePastRange(before.lngLat[0] + longitudeDelta(before.lngLat[0], after.lngLat[0]) * ratio),
        before.lngLat[1] + (after.lngLat[1] - before.lngLat[1]) * ratio,
    )
}
