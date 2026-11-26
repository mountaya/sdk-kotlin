package com.mountaya.geo

import kotlin.math.abs

/**
 * nearestSampleIndex is the place in a series of the sample of [points] nearest [distance] meters from the
 * itinerary's start, found by halving (`geo.profile.scrub-grid`): the earlier of two standing equally near,
 * so a scrub that lands exactly between two resolves the same way every time, and the first or the last for
 * a distance before or past them. Null for a series with no sample.
 */
public fun nearestSampleIndex(
    points: List<ElevationPoint>,
    distance: Double,
): Int? {
    if (points.isEmpty()) return null
    var low = 0
    var high = points.size - 1
    while (low < high) {
        val middle = (low + high) / 2
        if (points[middle].distance < distance) low = middle + 1 else high = middle
    }
    val before = if (low > 0) low - 1 else 0
    val afterGap = abs(points[low].distance - distance)
    val beforeGap = abs(points[before].distance - distance)
    return if (beforeGap <= afterGap) before else low
}
