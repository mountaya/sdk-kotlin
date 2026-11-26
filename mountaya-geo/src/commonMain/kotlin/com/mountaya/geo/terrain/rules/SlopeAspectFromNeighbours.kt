package com.mountaya.geo

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot

/**
 * slopeAspectFromNeighbours answers the slope and the aspect of the ground at a point at
 * [latitude], in degrees, from [neighbours], the elevations [spacingDegrees] away to its north,
 * south, east and west: a central-difference gradient measured on the ground in the degree of the
 * mean sphere, the east-west span shrunk by the cosine of the latitude. The name is the one every
 * SDK gives the rule, beside the [SlopeAspect] it answers.
 *
 * The slope is the gradient's magnitude as an angle in degrees. The aspect is the compass bearing
 * of the descent within [0, 360), 0 north and 90 east, and null where the four neighbours leave no
 * gradient. Nothing is refused: a neighbour, a latitude or a spacing that is not finite answers
 * what the arithmetic answers, `NaN` for both where a neighbour is `NaN`.
 */
public fun slopeAspectFromNeighbours(
    neighbours: TerrainNeighbours,
    latitude: Double,
    spacingDegrees: Double,
): SlopeAspect {
    val metersNorth = spacingDegrees * METERS_PER_DEGREE_MEAN_SPHERE
    val metersEast = metersNorth * cos(latitude * RADIANS_PER_DEGREE)
    // The rise per meter going east, and going north.
    val riseEast = (neighbours.east - neighbours.west) / (2.0 * metersEast)
    val riseNorth = (neighbours.north - neighbours.south) / (2.0 * metersNorth)
    val slope = convertSlope(hypot(riseEast, riseNorth), SlopeUnit.Ratio, SlopeUnit.Degrees)

    // No gradient is flat ground, which falls in no direction. Read through atan2 it would answer
    // a bearing, since two zeros of either sign have an angle there.
    if (riseEast == 0.0 && riseNorth == 0.0) return SlopeAspect(slope, null)

    // atan2(east, north) of the descent is a compass bearing: 0 north, 90 east, clockwise.
    return SlopeAspect(slope, compassBearing(atan2(-riseEast, -riseNorth) * DEGREES_PER_RADIAN))
}
