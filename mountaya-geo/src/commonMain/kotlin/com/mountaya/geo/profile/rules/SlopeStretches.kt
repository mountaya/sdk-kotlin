package com.mountaya.geo

/**
 * slopeBuckets is the ramp a profile's own steepness is painted from where its caller names no other, the
 * `buckets` `geo/profile-slope-stretches.json` declares: steep descent, descent, flat, ascent and steep
 * ascent at -10, -3, 3 and 10 degrees, the angles a walker feels the difference at, coloured green through
 * orange in the direction difficulty runs.
 */
public val slopeBuckets: List<SlopeBucket> =
    listOf(
        SlopeBucket(Double.NEGATIVE_INFINITY, "steepDescent", "#e8590c"),
        SlopeBucket(-10.0, "descent", "#f59f00"),
        SlopeBucket(-3.0, "flat", "#2f9e44"),
        SlopeBucket(3.0, "ascent", "#f59f00"),
        SlopeBucket(10.0, "steepAscent", "#e8590c"),
    )

/**
 * slopeStretches is an itinerary's own steepness as the stretches its profile's curve is painted in, in
 * distance order (`geo.profile.slope-stretches`), each labelled with its bucket's key.
 *
 * Each sample of [points] after the first ends a stretch from the sample before it, and the first reads the
 * stretch it starts. A stretch at least [GRADE_WINDOW_METERS] long reads as its own slope, its rise over its
 * run, since a thinned long itinerary carries a sample every few hundred meters and a window centred on it
 * would straddle the stretches either side; a shorter one reads [gradeAt] at its sample, since two noisy
 * samples a few meters apart are no cliff. The slope falls in the last of [buckets] whose `from` it reaches,
 * the first where it reaches none. Neighbours of one bucket merge into one stretch, each stretch starts where
 * the one before it ends, and one of no length is left out. A series of fewer than two samples, or a ramp of
 * no bucket, has none.
 */
public fun slopeStretches(
    points: List<ElevationPoint>,
    buckets: List<SlopeBucket> = slopeBuckets,
): List<ProfileClass> {
    val lowest = buckets.firstOrNull()
    if (points.size < 2 || lowest == null) return emptyList()
    val stretches = ArrayList<ProfileClass>()
    for ((index, point) in points.withIndex()) {
        val grade = stretchGrade(points, index) ?: continue
        val bucket = buckets.lastOrNull { grade >= it.from } ?: lowest
        val open = stretches.lastOrNull()
        if (open != null && open.label == bucket.key) {
            stretches[stretches.size - 1] = open.spanning(open.from, point.distance)
            continue
        }
        stretches += ProfileClass(open?.to ?: point.distance, point.distance, bucket.color, bucket.key)
    }
    return stretches.filter { it.to > it.from }
}

/**
 * The slope of the stretch of [points] that ends at the sample at [index], in degrees: its own where the
 * sample before it stands at least the grade window back, else the grade at the sample. The first sample reads
 * the stretch it starts.
 */
private fun stretchGrade(
    points: List<ElevationPoint>,
    index: Int,
): Double? {
    val start = points[maxOf(0, index - 1)]
    val end = points[maxOf(1, index)]
    val run = end.distance - start.distance
    if (run >= GRADE_WINDOW_METERS) {
        return convertSlope((end.elevation - start.elevation) / run, SlopeUnit.Ratio, SlopeUnit.Degrees)
    }
    return gradeAt(points, points[index].distance)
}
