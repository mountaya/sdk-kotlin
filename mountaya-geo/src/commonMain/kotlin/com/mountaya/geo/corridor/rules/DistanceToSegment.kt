package com.mountaya.geo

import kotlin.math.hypot

/**
 * distanceToSegment is the shortest distance from [point] to the segment from [start] to [end],
 * in the plane of the positions as written: their longitudes and latitudes read as plain
 * coordinates, so the answer is in degrees, with no cosine and no wrap. A degenerate segment, its
 * two ends one vertex, answers the distance to that vertex.
 *
 * It answers what `geo/corridor.json`'s cases answer, which is planar degrees; the distance on the
 * ground in meters is [metersToRoute]. A position that names no place, short of a longitude or a
 * latitude or with either not finite, answers `NaN`.
 */
public fun distanceToSegment(
    point: List<Double>,
    start: List<Double>,
    end: List<Double>,
): Double {
    if (!namesPlace(point) || !namesPlace(start) || !namesPlace(end)) return Double.NaN
    return planarDistanceToSegment(point[0], point[1], start[0], start[1], end[0], end[1])
}

/**
 * The shortest distance in a plane from the point ([pointX], [pointY]) to the segment from
 * ([startX], [startY]) to ([endX], [endY]): to the foot of the perpendicular where it falls on
 * the segment, to the nearer end where it falls past one, and to the start where the segment is
 * one point.
 */
internal fun planarDistanceToSegment(
    pointX: Double,
    pointY: Double,
    startX: Double,
    startY: Double,
    endX: Double,
    endY: Double,
): Double =
    planarDistanceToFoot(
        pointX,
        pointY,
        startX,
        startY,
        endX,
        endY,
        planarFootFraction(pointX, pointY, startX, startY, endX, endY),
    )

/**
 * Where the foot of the point ([pointX], [pointY]) stands on the segment from ([startX], [startY])
 * to ([endX], [endY]), as a fraction of the segment from its start: the projection onto the line
 * through it, clamped into [0, 1], so a point past an end has its foot on that end. A segment that is
 * one point has its foot there, at 0.
 *
 * The one statement of the projection [planarDistanceToSegment] measures and a traveller's progress
 * along a route stands on, so the two read one foot.
 */
internal fun planarFootFraction(
    pointX: Double,
    pointY: Double,
    startX: Double,
    startY: Double,
    endX: Double,
    endY: Double,
): Double {
    val spanX = endX - startX
    val spanY = endY - startY
    val lengthSquared = spanX * spanX + spanY * spanY
    if (lengthSquared == 0.0) return 0.0
    return (((pointX - startX) * spanX + (pointY - startY) * spanY) / lengthSquared).coerceIn(0.0, 1.0)
}

/**
 * The distance in a plane from the point ([pointX], [pointY]) to the place [fraction] of the way
 * along the segment from ([startX], [startY]) to ([endX], [endY]).
 */
internal fun planarDistanceToFoot(
    pointX: Double,
    pointY: Double,
    startX: Double,
    startY: Double,
    endX: Double,
    endY: Double,
    fraction: Double,
): Double = hypot(pointX - (startX + fraction * (endX - startX)), pointY - (startY + fraction * (endY - startY)))
