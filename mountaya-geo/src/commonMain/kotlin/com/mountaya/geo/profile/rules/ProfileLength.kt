package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry

/**
 * profileLength is the length of [geometry] as an elevation series walks it, and nothing else: distance
 * accumulates within each line, the jump from one line's end to the next line's start costs none, and a vertex
 * that names no place is measured across. So a leg's span on its itinerary's axis is computable from the leg's
 * own geometry alone, and is the stretch the series built from the whole chain draws it on. No geometry has no
 * length.
 */
internal fun profileLength(geometry: GeoJsonGeometry?): Double =
    linesOf(geometry).fold(0.0) { distance, line -> walkLine(line, distance, ::haversineMeters) }
