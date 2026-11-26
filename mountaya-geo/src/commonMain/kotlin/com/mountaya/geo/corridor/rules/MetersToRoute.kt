package com.mountaya.geo

import com.mountaya.client.GeoJsonLineString

/**
 * metersToRoute is the shortest distance in meters from [point] to any segment of any of [lines],
 * the measure a corridor around an itinerary is tested by.
 *
 * Each segment is projected into a local plane centred on the point, a degree of latitude
 * [METERS_PER_DEGREE_MEAN_SPHERE] and a degree of longitude that times the cosine of the point's
 * latitude, and measured with the clamped segment distance. A longitude is read as an angle: a
 * segment's first end is placed the short way round from the point and its second the short way
 * round from the first, so a segment stays in one piece, and where it runs past the meridian half
 * a turn from the point its far stretch is measured from the point a turn the other way.
 *
 * A vertex that names no place, short of a longitude or a latitude or with either not finite,
 * ends no segment: the segment runs from the vertex before it that names a place to the next one
 * that does. With no segment to measure to, and for a point that names no place, the answer is
 * [Double.POSITIVE_INFINITY], which every corridor width refuses.
 */
public fun metersToRoute(
    point: List<Double>,
    lines: List<GeoJsonLineString>,
): Double {
    if (!namesPlace(point)) return Double.POSITIVE_INFINITY
    val metersPerDegreeLongitude = metersPerDegreeLongitudeAt(point[1])
    var nearest = Double.POSITIVE_INFINITY
    for (line in lines) {
        var previous: List<Double>? = null
        for (vertex in line.coordinates) {
            if (!namesPlace(vertex)) continue
            val from = previous
            previous = vertex
            if (from == null) continue
            val distance = footAround(point, metersPerDegreeLongitude, from, vertex) { distance, _ -> distance }
            if (distance < nearest) nearest = distance
        }
    }
    return nearest
}
