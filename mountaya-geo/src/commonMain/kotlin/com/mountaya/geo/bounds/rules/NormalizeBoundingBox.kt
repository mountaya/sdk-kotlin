package com.mountaya.geo

/**
 * normalizeBoundingBox writes [bounds], `[west, south, east, north]` in degrees, in canonical form.
 *
 * Each axis is ordered low to high, so a west edge greater than the east one is a box drawn from
 * its east corner and never one crossing the antimeridian; the west edge is brought within
 * [-180, 180] by whole turns with the east edge following it, past 180 where the box crosses the
 * antimeridian, as [boundsOf] frames one; a box a turn or more wide, or with a longitude at an
 * infinity, is the whole world, `[-180, south, 180, north]`; and each latitude is held within
 * [WEB_MERCATOR_MAX_LATITUDE]. A box the API answers with its west edge greater than its east one
 * crosses the antimeridian, as RFC 7946 writes it, and is read as `[west, south, east + 360,
 * north]` where it is received, before it is handed here.
 *
 * Nothing is refused. A longitude that is `NaN`, or both at one infinity, answers `NaN` for both
 * longitudes, a latitude that is `NaN` answers itself, and a box that is not four numbers answers
 * four `NaN`s.
 */
public fun normalizeBoundingBox(bounds: List<Double>): List<Double> {
    if (bounds.size != 4) return List(4) { Double.NaN }
    val (first, second, third, fourth) = bounds
    val longitudes = canonicalLongitudes(minOf(first, third), maxOf(first, third))
    return listOf(
        longitudes[0],
        minOf(second, fourth).coerceIn(-WEB_MERCATOR_MAX_LATITUDE, WEB_MERCATOR_MAX_LATITUDE),
        longitudes[1],
        maxOf(second, fourth).coerceIn(-WEB_MERCATOR_MAX_LATITUDE, WEB_MERCATOR_MAX_LATITUDE),
    )
}
