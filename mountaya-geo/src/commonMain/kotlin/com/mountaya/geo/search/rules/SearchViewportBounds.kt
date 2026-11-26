package com.mountaya.geo

import kotlin.math.cos
import kotlin.math.pow

/**
 * SEARCH_BOUNDS_MAX_SPAN_DEGREES is the widest span, in degrees, a viewport-restricted search asks about on
 * either axis.
 */
public const val SEARCH_BOUNDS_MAX_SPAN_DEGREES: Double = 90.0

/**
 * SEARCH_BOUNDS_COSINE_FLOOR is the least the cosine of the centre's latitude narrows a search's latitude
 * span by, so a box near a pole still spans a twentieth of its longitude span rather than nothing.
 */
public const val SEARCH_BOUNDS_COSINE_FLOOR: Double = 0.05

/** The latitude of a pole. */
private const val LATITUDE_LIMIT = 90.0

/**
 * searchViewportBounds answers the box `[west, south, east, north]` a viewport-restricted search
 * sends for a camera at [center], `[longitude, latitude]`, and [zoom]: the centre's longitude
 * wrapped into [-180, 180) and its latitude held within [-90, 90]; a longitude span of `360 / 2^zoom`
 * degrees, at most [SEARCH_BOUNDS_MAX_SPAN_DEGREES]; a latitude span of that times the cosine of the
 * centre's latitude, the cosine at least [SEARCH_BOUNDS_COSINE_FLOOR]; and each edge half its span
 * from the centre, held within [-180, 180] and [-90, 90], so the box never crosses the antimeridian
 * or a pole. At zoom 12 over the Mont Blanc valley it spans 0.088 degrees of longitude.
 *
 * Four `NaN`s for a centre that names no place, and `NaN` wherever [zoom] is no number: the holds
 * keep a `NaN` rather than read it as an edge.
 */
public fun searchViewportBounds(
    center: List<Double>,
    zoom: Double,
): List<Double> {
    if (!namesPlace(center)) return List(4) { Double.NaN }
    val longitude = wrapLongitude(center[0])
    val latitude = center[1].coerceIn(-LATITUDE_LIMIT, LATITUDE_LIMIT)
    val span = minOf(DEGREES_PER_TURN / 2.0.pow(zoom), SEARCH_BOUNDS_MAX_SPAN_DEGREES)
    val latitudeSpan = span * maxOf(SEARCH_BOUNDS_COSINE_FLOOR, cos(latitude * RADIANS_PER_DEGREE))
    return listOf(
        (longitude - span / 2).coerceIn(-LONGITUDE_LIMIT, LONGITUDE_LIMIT),
        (latitude - latitudeSpan / 2).coerceIn(-LATITUDE_LIMIT, LATITUDE_LIMIT),
        (longitude + span / 2).coerceIn(-LONGITUDE_LIMIT, LONGITUDE_LIMIT),
        (latitude + latitudeSpan / 2).coerceIn(-LATITUDE_LIMIT, LATITUDE_LIMIT),
    )
}
