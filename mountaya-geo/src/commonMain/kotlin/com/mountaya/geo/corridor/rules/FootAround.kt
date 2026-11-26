package com.mountaya.geo

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sign

/**
 * The ground length in meters of one degree of longitude in the local plane placed around a point
 * at [latitude], in degrees: the mean sphere's degree times the cosine of the latitude.
 */
internal fun metersPerDegreeLongitudeAt(latitude: Double): Double =
    METERS_PER_DEGREE_MEAN_SPHERE * cos(latitude * RADIANS_PER_DEGREE)

/**
 * Places the segment from [from] to [to] in the local plane around [point], as a corridor places
 * every segment it measures, and hands [answer] the point's foot on it: the foot's distance from the
 * point in ground meters, and the fraction of the segment it stands at from [from], in [0, 1].
 *
 * The plane's degree of latitude is [METERS_PER_DEGREE_MEAN_SPHERE], and its degree of longitude
 * [metersPerDegreeLongitude], [metersPerDegreeLongitudeAt] the point's latitude, handed in so a walk
 * over many segments reads the cosine once. A longitude is read as an angle, and the segment is kept
 * in one piece: its first end is placed the short way round from the point and its second the short
 * way round from the first. Where the segment runs past the meridian half a turn from the point, the
 * stretch beyond it is nearer the point written a whole turn that way, and the foot there is the one
 * answered where it is the nearer, the direct one where the two are as near.
 *
 * The one placement of a segment that [metersToRoute] measures and a traveller's progress stands on,
 * so a traveller is off a route by the distance their progress is read at. Every position names a
 * place here: a caller skips a vertex that names none, as every walk of a line does. [answer] is
 * inlined, so a walk over a long line allocates nothing per segment.
 */
internal inline fun <R> footAround(
    point: List<Double>,
    metersPerDegreeLongitude: Double,
    from: List<Double>,
    to: List<Double>,
    answer: (distance: Double, fraction: Double) -> R,
): R = footAround(point[0], point[1], metersPerDegreeLongitude, from[0], from[1], to[0], to[1], answer)

/**
 * Places the segment from ([fromLongitude], [fromLatitude]) to ([toLongitude], [toLatitude]) in the local
 * plane around ([pointLongitude], [pointLatitude]), as the form over positions places it, and hands
 * [answer] the point's foot on it, so a walk over coordinates held unboxed reads the same numbers. Every
 * coordinate is in degrees.
 */
internal inline fun <R> footAround(
    pointLongitude: Double,
    pointLatitude: Double,
    metersPerDegreeLongitude: Double,
    fromLongitude: Double,
    fromLatitude: Double,
    toLongitude: Double,
    toLatitude: Double,
    answer: (distance: Double, fraction: Double) -> R,
): R {
    // Both ends the short way round from the point, then the second moved by the whole turn that
    // brings it within half a turn of the first, so the segment stays in one piece.
    val startDegrees = longitudeDelta(pointLongitude, fromLongitude)
    val shortDegrees = longitudeDelta(pointLongitude, toLongitude)
    val step = shortDegrees - startDegrees
    val endDegrees = if (abs(step) <= LONGITUDE_LIMIT) shortDegrees else shortDegrees - sign(step) * DEGREES_PER_TURN

    val startX = startDegrees * metersPerDegreeLongitude
    val startY = (fromLatitude - pointLatitude) * METERS_PER_DEGREE_MEAN_SPHERE
    val endX = endDegrees * metersPerDegreeLongitude
    val endY = (toLatitude - pointLatitude) * METERS_PER_DEGREE_MEAN_SPHERE
    var fraction = planarFootFraction(0.0, 0.0, startX, startY, endX, endY)
    var distance = planarDistanceToFoot(0.0, 0.0, startX, startY, endX, endY, fraction)

    // The segment runs past the meridian half a turn from the point: the stretch beyond it is
    // nearer the point written a whole turn that way.
    if (abs(endDegrees) > LONGITUDE_LIMIT) {
        val turnedX = sign(endDegrees) * DEGREES_PER_TURN * metersPerDegreeLongitude
        val turnedFraction = planarFootFraction(turnedX, 0.0, startX, startY, endX, endY)
        val turnedDistance = planarDistanceToFoot(turnedX, 0.0, startX, startY, endX, endY, turnedFraction)
        if (turnedDistance < distance) {
            fraction = turnedFraction
            distance = turnedDistance
        }
    }
    return answer(distance, fraction)
}
