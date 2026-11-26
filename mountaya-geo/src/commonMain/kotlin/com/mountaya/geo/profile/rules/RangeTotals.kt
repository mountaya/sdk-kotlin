package com.mountaya.geo

import kotlin.math.abs

/** A difference this near zero is a floating-point tail of two large accumulations, and is zero. */
private const val ACCUMULATION_TAIL = 1e-6

/**
 * rangeTotals states the stretch of [series] between the distances [from] and [to], in meters from
 * the itinerary's start, read off one cumulative scan of the whole series. A range given backwards
 * is read in order.
 *
 * Its ascent and descent are differences of the series' cumulative readings at its two ends, each
 * reading interpolated linearly between the samples either side of it, so the stretches of an
 * itinerary sum to its totals exactly and the noise threshold never restarts at a boundary. Its
 * distance is the stretch's own length, and its altitude band and steepest grade are its own, read
 * on the curve between samples, so a stretch shorter than the gap between two samples still states
 * a band and a grade. Over the whole itinerary it answers what [profileTotals] answers.
 *
 * A stretch the samples do not reach states no band and no grade, and a series with no sample
 * states the stretch's length alone.
 */
public fun rangeTotals(
    series: ElevationSeries,
    from: Double,
    to: Double,
): ProfileTotals {
    val points = series.points
    val start = minOf(from, to)
    val end = maxOf(from, to)
    if (points.isEmpty()) return ProfileTotals(end - start, null, null, null, null, null)
    val cumulative = cumulativeElevation(points)
    val ascent = readCumulative(points, cumulative.ascent, end) - readCumulative(points, cumulative.ascent, start)
    val descent = readCumulative(points, cumulative.descent, end) - readCumulative(points, cumulative.descent, start)
    val band = altitudeBand(points, start, end)
    return ProfileTotals(
        distance = end - start,
        ascent = nonNegative(ascent),
        descent = nonNegative(descent),
        elevationMin = band?.first,
        elevationMax = band?.second,
        gradeMax = steepestGrade(points, start, end),
    )
}

/** A difference of two readings, zero where it is a floating-point tail of zero and never under it. */
private fun nonNegative(value: Double): Double = if (abs(value) < ACCUMULATION_TAIL) 0.0 else maxOf(0.0, value)

/**
 * Reads the cumulative [totals] of [points] at [distance], linearly between the two samples either
 * side of it rather than snapped to the nearer, so a boundary a few meters from a sample never
 * gives that sample's whole step to one side. Before the first sample it reads the first, past the
 * last the last.
 */
private fun readCumulative(
    points: List<ElevationPoint>,
    totals: List<Double>,
    distance: Double,
): Double {
    if (distance <= points.first().distance) return totals.first()
    if (distance >= points.last().distance) return totals.last()
    val after = firstIndexAtOrPast(points, distance)
    if (after <= 0 || after >= points.size) return totals.last()
    val start = points[after - 1]
    val end = points[after]
    val low = totals[after - 1]
    val high = totals[after]
    val run = end.distance - start.distance
    return if (run <= 0.0) high else low + (high - low) * (distance - start.distance) / run
}
