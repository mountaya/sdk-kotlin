package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/** The meters in a kilometer. */
private const val METERS_PER_KILOMETER = 1_000.0

/**
 * buildInfoBands places the analysis entries [ranges] reads off each of [segments], in chain order,
 * on the itinerary's distance axis in kilometers, so each band stands under the points it colours.
 *
 * Each segment's lines are laid end to end and walked as the elevation series walks them: distance
 * accumulates within a line, the jump to the next line or the next segment costs none, and a vertex
 * that names no place keeps its index and stands at the distance of the last vertex that did. An
 * index resolves to the distance at that vertex, one past the line to its last vertex and one under
 * zero to its first. A band of no length is dropped, and a band that carries its predecessor's value
 * and starts where it ends or before is merged into it. Every segment advances the axis by the
 * length of its lines, whether or not it yields a band, and one with fewer than two vertices yields
 * none.
 *
 * [ranges] picks one analysis, as `{ segment -> segment.surface_info?.map { InfoRange(it.from_index,
 * it.to_index, it.value) } }` does; null reads as none. Values are compared by `equals`.
 */
public fun <T> buildInfoBands(
    segments: List<RestSchema.ItinerarySegment>,
    ranges: (RestSchema.ItinerarySegment) -> List<InfoRange<T>>?,
): List<InfoBand<T>> {
    val bands = ArrayList<InfoBand<T>>()
    var itineraryKm = 0.0
    for (segment in segments) {
        // The distance at each vertex of the segment's lines laid end to end, the array its indexes address.
        val vertexKm = ArrayList<Double>()
        var segmentKm = 0.0
        for (line in linesOf(segment.geojson)) {
            segmentKm = walkLine(line, segmentKm, ::haversineKilometers) { km, _, _ -> vertexKm += km }
        }
        val lastIndex = vertexKm.lastIndex
        if (lastIndex >= 1) {
            for (range in ranges(segment).orEmpty()) {
                val startKm = itineraryKm + vertexKm[range.fromIndex.coerceIn(0, lastIndex)]
                val endKm = itineraryKm + vertexKm[range.toIndex.coerceIn(0, lastIndex)]
                if (!(endKm - startKm > 0.0)) continue
                val previous = bands.lastOrNull()
                if (previous != null && previous.value == range.value && previous.endKm >= startKm) {
                    bands[bands.lastIndex] = InfoBand(previous.startKm, endKm, previous.value)
                    continue
                }
                bands += InfoBand(startKm, endKm, range.value)
            }
        }
        itineraryKm += segmentKm
    }
    return bands
}

/** The great-circle distance between two positions, in kilometers. */
private fun haversineKilometers(
    from: List<Double>,
    to: List<Double>,
): Double = haversineMeters(from, to) / METERS_PER_KILOMETER
