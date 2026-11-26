package com.mountaya.geo

/**
 * ELEVATION_NOISE_THRESHOLD_METERS is the rise or fall a stretch must reach before it counts as
 * ascent or descent, the `elevationNoiseThresholdMeters` `geo/elevation-totals.json` declares: a
 * sample this far above or below the last one counted is counted, and any nearer is noise. Summed
 * straight from sampled terrain, a flat road reports hundreds of meters of climb.
 */
public const val ELEVATION_NOISE_THRESHOLD_METERS: Double = 5.0

/**
 * GRADE_WINDOW_METERS is the run a maximum grade is measured over, the `gradeWindowMeters`
 * `geo/elevation-totals.json` declares, so the figure means the steepest hundred meters rather
 * than the ratio of two noisy altitudes a few meters apart.
 */
public const val GRADE_WINDOW_METERS: Double = 100.0

/**
 * profileTotals derives an itinerary's figures from its [series]: its length, the whole series'
 * total distance; its ascent and descent, accumulated through the
 * [ELEVATION_NOISE_THRESHOLD_METERS] hysteresis; its altitude band, the series' own; and its
 * steepest ascending [GRADE_WINDOW_METERS], in degrees and never under 0.
 *
 * The ascent and the descent are estimates: the hysteresis removes the double counting raw
 * terrain samples produce and clips up to the threshold off each genuine climb, so an itinerary
 * holding its server's metrics states those. A series with no sample states its length alone.
 */
public fun profileTotals(series: ElevationSeries): ProfileTotals {
    val points = series.points
    if (points.isEmpty()) return ProfileTotals(series.totalDistance, null, null, null, null, null)
    val cumulative = cumulativeElevation(points)
    return ProfileTotals(
        distance = series.totalDistance,
        ascent = cumulative.ascent.last(),
        descent = cumulative.descent.last(),
        elevationMin = series.minElevation,
        elevationMax = series.maxElevation,
        gradeMax = steepestGrade(points, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
    )
}
