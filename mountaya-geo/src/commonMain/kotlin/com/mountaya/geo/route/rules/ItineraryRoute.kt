package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * itineraryRoute is the one route a fly-through follows over an itinerary's [segments], as the API
 * answers them, or over the one segment [segmentId] names; null names none, and flies them all.
 *
 * Every line a segment holds is laid end to end, the segments in chain order, by their `position`,
 * and their lines in the order their `geojson` writes them: a LineString is one line and each line
 * of a MultiLineString is one, a GeometryCollection holds the lines of what it holds, and a
 * MultiPoint, a directions segment's vias, is no line and is left out, as is a segment with no
 * geometry. A vertex that names no place, short of a longitude or a latitude or with either not
 * finite, is left out with its elevation.
 *
 * The route's line keeps each vertex's longitude and latitude. The first vertex of each line after
 * the first is a jump: the camera flies the leg that reaches it, while the itinerary's distance
 * charges nothing for it. The elevations are the vertices' third values, stated only where every
 * vertex kept carries a finite one. The answer is null where fewer than two vertices name a place:
 * an unknown segment, segments holding no line, or a single vertex, since a flight needs a
 * direction.
 */
public fun itineraryRoute(
    segments: List<RestSchema.ItinerarySegment>,
    segmentId: String? = null,
): RouteGeometry? {
    val flown =
        if (segmentId == null) {
            // The chain order; a sort that keeps the listed order of two segments at one position.
            segments.sortedBy { it.position }
        } else {
            listOfNotNull(segments.firstOrNull { it.id == segmentId })
        }
    val line = ArrayList<List<Double>>()
    val elevations = ArrayList<Double>()
    val jumps = ArrayList<Int>()
    var everyVertexHasElevation = true
    for (segment in flown) {
        for (vertices in linesOf(segment.geojson)) {
            var opensLine = true
            for (vertex in vertices) {
                if (!namesPlace(vertex)) continue
                // The first vertex a line contributes is where the flight lands from the line before.
                if (opensLine && line.isNotEmpty()) jumps += line.size
                opensLine = false
                line += listOf(vertex[0], vertex[1])
                val elevation = vertex.getOrNull(2)?.takeIf { it.isFinite() }
                if (elevation == null) everyVertexHasElevation = false else elevations += elevation
            }
        }
    }
    if (line.size < 2) return null
    return RouteGeometry(line, if (everyVertexHasElevation) elevations else emptyList(), jumps)
}
