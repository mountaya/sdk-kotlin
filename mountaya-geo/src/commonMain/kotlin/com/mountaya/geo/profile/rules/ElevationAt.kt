package com.mountaya.geo

/**
 * Returns the altitude the curve of [points] stands at [distance] meters from the itinerary's
 * start, read on the straight line between the two samples either side of it, the line a chart
 * draws. A distance that is a sample's own reads that sample, the first of those standing there,
 * short of the last sample's distance, which reads the last sample; one before the first sample or
 * past the last reads that end's altitude. Null for no sample, and for a distance that is not a
 * number.
 */
internal fun elevationAt(
    points: List<ElevationPoint>,
    distance: Double,
): Double? {
    val first = points.firstOrNull() ?: return null
    val last = points.last()
    if (distance.isNaN()) return null
    if (distance <= first.distance) return first.elevation
    if (distance >= last.distance) return last.elevation
    val index = firstIndexAtOrPast(points, distance)
    val after = points.getOrNull(index) ?: return null
    val before = points.getOrNull(index - 1) ?: return null
    if (after.distance == distance) return after.elevation
    val run = after.distance - before.distance
    val ratio = if (run > 0.0) (distance - before.distance) / run else 0.0
    return before.elevation + (after.elevation - before.elevation) * ratio
}

/**
 * Returns the index of the first of [points] standing at or past [distance], or the count of them
 * where every one stands before it. A distance that is not a number answers 0.
 */
internal fun firstIndexAtOrPast(
    points: List<ElevationPoint>,
    distance: Double,
): Int {
    var low = 0
    var high = points.size
    while (low < high) {
        val middle = (low + high) ushr 1
        if (points[middle].distance < distance) low = middle + 1 else high = middle
    }
    return low
}

/**
 * Returns the index of the sample a stretch starting at [distance] sets out from: the last of
 * [points] standing at that distance, or -1 where none stands there and the start is read between
 * two. Where two lines meet, the earlier line's last sample and the later line's first stand at one
 * distance, at two places and two altitudes, and a stretch starting there is on the later line.
 */
internal fun startIndexAt(
    points: List<ElevationPoint>,
    distance: Double,
): Int {
    var index = firstIndexAtOrPast(points, distance)
    if (points.getOrNull(index)?.distance != distance) return -1
    while (points.getOrNull(index + 1)?.distance == distance) index += 1
    return index
}

/** Returns the altitude a stretch starting at [distance] sets out from, as [startIndexAt] reads it. */
internal fun startElevationAt(
    points: List<ElevationPoint>,
    distance: Double,
): Double? = points.getOrNull(startIndexAt(points, distance))?.elevation ?: elevationAt(points, distance)
