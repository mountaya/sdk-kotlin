package com.mountaya.geo

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * bearingDegrees is the initial great-circle bearing from one position to another, `[longitude,
 * latitude]` in degrees: the compass heading to set out on, within [0, 360), 0 north and 90 east.
 *
 * The longitudes are read as angles, the short way round, so a position written 180 and one
 * written -180 are one place. Two positions that are one place have no heading and answer 0. A
 * position that names no place, short of a longitude or a latitude or with either not finite,
 * answers `NaN`. An altitude is ignored.
 */
public fun bearingDegrees(
    from: List<Double>,
    to: List<Double>,
): Double {
    if (!namesPlace(from) || !namesPlace(to)) return Double.NaN
    val longitudeSpan = longitudeDelta(from[0], to[0]) * RADIANS_PER_DEGREE
    if (longitudeSpan == 0.0 && from[1] == to[1]) return 0.0
    val latitudeFrom = from[1] * RADIANS_PER_DEGREE
    val latitudeTo = to[1] * RADIANS_PER_DEGREE
    val east = sin(longitudeSpan) * cos(latitudeTo)
    val north = cos(latitudeFrom) * sin(latitudeTo) - sin(latitudeFrom) * cos(latitudeTo) * cos(longitudeSpan)
    return compassBearing(atan2(east, north) * DEGREES_PER_RADIAN)
}

/**
 * The compass bearing [degrees], an angle from north within [-180, 180] as `atan2` answers one,
 * written within [0, 360): one under zero is brought into the turn, a negative zero reads as zero,
 * and one so close under zero that a turn added to it rounds to a whole turn is north. `NaN` stays
 * `NaN`.
 */
internal fun compassBearing(degrees: Double): Double {
    val bearing = if (degrees < 0.0) degrees + DEGREES_PER_TURN else degrees + 0.0
    return if (bearing >= DEGREES_PER_TURN) 0.0 else bearing
}
