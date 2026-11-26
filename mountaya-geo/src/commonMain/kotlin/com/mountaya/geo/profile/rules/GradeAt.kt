package com.mountaya.geo

/**
 * gradeAt is how steep an itinerary is [distance] meters from its start, in degrees, negative for a descent
 * (`geo.profile.grade-at`): the slope across the [GRADE_WINDOW_METERS] of curve centred on the distance, cut
 * to the part of that window [points] hold, each end read on the line between the two samples either side of
 * it, so the run is the window's width whatever the samples' spacing and two noisy samples ten meters apart
 * are no cliff.
 *
 * Null where the window holds no length of the curve: a series of fewer than two samples, and a distance
 * farther from the curve than half the window.
 */
public fun gradeAt(
    points: List<ElevationPoint>,
    distance: Double,
): Double? {
    val first = points.firstOrNull() ?: return null
    val last = points.last()
    val half = GRADE_WINDOW_METERS / 2.0
    val from = maxOf(first.distance, distance - half)
    val to = minOf(last.distance, distance + half)
    if (!(to > from)) return null
    val start = elevationAt(points, from) ?: return null
    val end = elevationAt(points, to) ?: return null
    return convertSlope((end - start) / (to - from), SlopeUnit.Ratio, SlopeUnit.Degrees)
}
