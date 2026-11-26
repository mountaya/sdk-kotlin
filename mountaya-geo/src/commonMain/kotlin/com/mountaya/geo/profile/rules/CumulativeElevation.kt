package com.mountaya.geo

/**
 * The meters climbed and the meters dropped from an itinerary's start to each sample of its series,
 * index for index with the series' points.
 */
internal class CumulativeElevation(
    val ascent: List<Double>,
    val descent: List<Double>,
)

/**
 * Runs the [ELEVATION_NOISE_THRESHOLD_METERS] hysteresis once over [points] and records it at every
 * sample. A reference altitude is carried forward: a sample at least the threshold above it counts
 * the rise and becomes the reference, one at least the threshold below counts the fall, and any
 * other leaves both, so a jitter of a few meters is never counted.
 *
 * Every figure of a stretch is a difference of these readings, never the hysteresis run over the
 * stretch alone, which would reset the reference at each boundary and clip the threshold off each
 * end: the stretches of an itinerary then sum to its totals exactly.
 */
internal fun cumulativeElevation(points: List<ElevationPoint>): CumulativeElevation {
    val ascent = ArrayList<Double>(points.size)
    val descent = ArrayList<Double>(points.size)
    val first = points.firstOrNull() ?: return CumulativeElevation(ascent, descent)
    var climbed = 0.0
    var dropped = 0.0
    var reference = first.elevation
    for (point in points) {
        val delta = point.elevation - reference
        if (delta >= ELEVATION_NOISE_THRESHOLD_METERS) {
            climbed += delta
            reference = point.elevation
        } else if (delta <= -ELEVATION_NOISE_THRESHOLD_METERS) {
            dropped -= delta
            reference = point.elevation
        }
        ascent += climbed
        descent += dropped
    }
    return CumulativeElevation(ascent, descent)
}
