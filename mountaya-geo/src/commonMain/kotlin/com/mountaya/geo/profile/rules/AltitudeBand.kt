package com.mountaya.geo

/**
 * Returns the lowest and the highest altitude, in that order, the curve of [points] reaches between
 * the distances [from] and [to], `from` at or before `to`: the samples the stretch holds and the
 * curve read at its two ends, so a stretch shorter than the gap between two samples still has the
 * band between its ends. Only the part of the stretch the samples reach is read. Where two lines
 * meet at an end of the stretch, the band holds the sample of the line the stretch is on.
 *
 * Null for no sample, and for a stretch that lies wholly before the first sample or past the last.
 */
internal fun altitudeBand(
    points: List<ElevationPoint>,
    from: Double,
    to: Double,
): Pair<Double, Double>? {
    val first = points.firstOrNull() ?: return null
    val last = points.last()
    val low = maxOf(from, first.distance)
    val high = minOf(to, last.distance)
    val start = startElevationAt(points, low)
    val end = elevationAt(points, high)
    if (!(high >= low) || start == null || end == null) return null
    val setsOutFrom = startIndexAt(points, low)
    val firstHeld = if (setsOutFrom < 0) firstIndexAtOrPast(points, low) else setsOutFrom
    val lastHeld = firstIndexAtOrPast(points, high)
    var lowest = minOf(start, end)
    var highest = maxOf(start, end)
    var index = firstHeld
    while (index <= lastHeld && index < points.size) {
        val point = points[index]
        if (point.distance > high) break
        lowest = minOf(lowest, point.elevation)
        highest = maxOf(highest, point.elevation)
        index += 1
    }
    return lowest to highest
}
