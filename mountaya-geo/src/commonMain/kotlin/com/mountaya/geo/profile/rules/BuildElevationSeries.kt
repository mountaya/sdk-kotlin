package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry

/**
 * MAX_PROFILE_POINTS is how many samples an elevation series holds where its caller names no other
 * ceiling, the `maxPoints` `geo/elevation-series.json` declares: a chart a few hundred pixels wide
 * shows no more, and the thinning is a uniform stride that changes the curve's resolution and not
 * its shape.
 */
public const val MAX_PROFILE_POINTS: Int = 500

/**
 * buildElevationSeries projects [geometry] onto an elevation series: the great-circle distance
 * along it, in meters, against the altitude its vertices carry, their third value.
 *
 * Every line the geometry holds is walked in the order it writes them and laid end to end on one
 * distance axis: a LineString is one line, each line of a MultiLineString one, a GeometryCollection
 * holds the lines of what it holds, and anything else, a directions segment's vias included, is
 * none. The jump between two lines costs no distance, since it is a seam in the storage and not
 * ground anyone covers. A vertex with no finite altitude advances the distance and is no sample,
 * so a hole in the terrain data is bridged by the curve; a vertex that names no place is no sample
 * and charges no distance, the line measured across it. No geometry is an itinerary of no length.
 *
 * A series of more than [maxPoints] samples is thinned by a uniform stride, keeping the first and
 * the last: slot `k` of the others keeps the sample nearest `k × (count − 1) / (maxPoints − 1)`,
 * the later of two where it stands halfway between them, a half rounding up exactly as the rational
 * reads. A ceiling under two keeps every sample, since a curve needs both its ends. The altitude
 * range and the total distance stay the whole line's.
 */
public fun buildElevationSeries(
    geometry: GeoJsonGeometry?,
    maxPoints: Int = MAX_PROFILE_POINTS,
): ElevationSeries {
    val samples = ArrayList<ElevationPoint>()
    var distance = 0.0
    var minElevation: Double? = null
    var maxElevation: Double? = null
    for (line in linesOf(geometry)) {
        distance =
            walkLine(line, distance, ::haversineMeters) { at, vertex, _ ->
                val elevation = vertex?.getOrNull(2)
                if (vertex != null && elevation != null && elevation.isFinite()) {
                    samples += ElevationPoint(at, elevation, listOf(vertex[0], vertex[1]), samples.size)
                    minElevation = minElevation?.let { minOf(it, elevation) } ?: elevation
                    maxElevation = maxElevation?.let { maxOf(it, elevation) } ?: elevation
                }
            }
    }
    return ElevationSeries(downsampleElevation(samples, maxPoints), distance, minElevation, maxElevation)
}
