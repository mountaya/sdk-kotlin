package com.mountaya.geo

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * EARTH_RADIUS_METERS is the radius of the mean Earth sphere every distance of the geo layer is
 * measured on, the `earthRadiusMeters` `geo/geodesy.json` declares: the IUGG mean radius, never a
 * platform's ellipsoid.
 */
public const val EARTH_RADIUS_METERS: Double = 6_371_000.0

/**
 * METERS_PER_DEGREE_MEAN_SPHERE is the ground length of one degree of arc on the sphere
 * [EARTH_RADIUS_METERS] describes, about 111,194.93 m: a degree of latitude anywhere, and a degree
 * of longitude at the equator. It is the radius times pi over 180, and the degree every planar
 * span on the ground is measured in.
 */
public const val METERS_PER_DEGREE_MEAN_SPHERE: Double = EARTH_RADIUS_METERS * (PI / 180.0)

/**
 * haversineMeters is the great-circle distance between two positions, `[longitude, latitude]` in
 * degrees, in meters on the sphere [EARTH_RADIUS_METERS] describes. Every other distance of the
 * geo layer is this function or a sum of it.
 *
 * Two positions that name places measure a finite distance, from zero to half the circumference,
 * across the antimeridian the short way round. A position that names no place, short of a
 * longitude or a latitude or with either not finite, measures `NaN`.
 */
public fun haversineMeters(
    from: List<Double>,
    to: List<Double>,
): Double {
    if (!namesPlace(from) || !namesPlace(to)) return Double.NaN
    val latitudeFrom = from[1] * RADIANS_PER_DEGREE
    val latitudeTo = to[1] * RADIANS_PER_DEGREE
    val sinLatitude = sin((to[1] - from[1]) * RADIANS_PER_DEGREE / 2.0)
    val sinLongitude = sin((to[0] - from[0]) * RADIANS_PER_DEGREE / 2.0)
    val term = sinLatitude * sinLatitude + cos(latitudeFrom) * cos(latitudeTo) * sinLongitude * sinLongitude
    // The term lies in 0 to 1 for any two angles, and rounding can leave it a hair outside: past
    // one for two antipodes, under zero for one place written on both sides of a pole. The root
    // and the arcsine have no answer out there.
    return 2.0 * EARTH_RADIUS_METERS * asin(sqrt(term.coerceIn(0.0, 1.0)))
}
