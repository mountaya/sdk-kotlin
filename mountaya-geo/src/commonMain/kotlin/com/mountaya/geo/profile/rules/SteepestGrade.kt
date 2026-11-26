package com.mountaya.geo

/**
 * Returns the steepest ascending [GRADE_WINDOW_METERS] of the stretch of [points] between the
 * distances [from] and [to], `from` at or before `to`, in degrees, never under 0.
 *
 * Every run measured is the window's full width, each end read on the curve between the two
 * samples either side of it, so two samples closer together than the noise they carry never set the
 * figure. The curve is straight between samples, so the steepest run starts or ends on a sample or
 * at an end of the stretch, and those are the runs measured, in two forward passes. A stretch
 * shorter than the window is measured end to end. Where two lines meet at an end of the stretch, a
 * run reads the line the stretch is on.
 *
 * Null for no sample, and for a stretch that holds no length of the curve.
 */
internal fun steepestGrade(
    points: List<ElevationPoint>,
    from: Double,
    to: Double,
): Double? {
    val first = points.firstOrNull() ?: return null
    val last = points.last()
    val low = maxOf(from, first.distance)
    val high = minOf(to, last.distance)
    val lowest = startElevationAt(points, low)
    val highest = elevationAt(points, high)
    if (!(high > low) || lowest == null || highest == null) return null

    val run = minOf(high - low, GRADE_WINDOW_METERS)
    val inside = firstIndexAtOrPast(points, low)

    // The runs that start at the stretch's own start and at each sample. One that reaches the
    // stretch's end reads the end's own altitude.
    val ahead = CurveCursor(points, inside - 1)

    fun aheadAt(distance: Double): Double = if (distance >= high) highest else ahead.at(distance)
    var rise = aheadAt(low + run) - lowest
    for (index in inside until points.size) {
        val point = points[index]
        if (point.distance > high - run) break
        if (point.distance > low) rise = maxOf(rise, aheadAt(point.distance + run) - point.elevation)
    }

    // The runs that end at each sample and at the stretch's own end.
    val behind = CurveCursor(points, inside - 1)
    for (index in inside until points.size) {
        val point = points[index]
        if (point.distance >= high) break
        if (point.distance >= low + run) rise = maxOf(rise, point.elevation - behind.at(point.distance - run))
    }
    rise = maxOf(rise, highest - behind.at(high - run))

    return maxOf(0.0, convertSlope(rise / run, SlopeUnit.Ratio, SlopeUnit.Degrees))
}

/**
 * Reads the curve of [points] at distances asked in ascending order, in one pass over the samples:
 * it only moves forward, from [startIndex]. A distance before the sample it stands on reads that
 * sample's altitude.
 */
private class CurveCursor(
    private val points: List<ElevationPoint>,
    startIndex: Int,
) {
    private var index = maxOf(0, startIndex)

    fun at(distance: Double): Double {
        while (index + 1 < points.size && points[index + 1].distance <= distance) index += 1
        val before = points.getOrNull(index) ?: return 0.0
        val after = points.getOrNull(index + 1)
        if (after == null || distance <= before.distance) return before.elevation
        return before.elevation +
            (after.elevation - before.elevation) * (distance - before.distance) / (after.distance - before.distance)
    }
}
