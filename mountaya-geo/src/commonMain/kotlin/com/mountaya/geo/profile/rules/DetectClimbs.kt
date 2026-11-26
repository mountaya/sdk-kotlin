package com.mountaya.geo

/**
 * CLIMB_OPEN_WINDOW_KM is the run a climb's opening grade is measured over, the `openWindowKm`
 * `geo/climbs.json` declares: the grade from the last sample at least this far back.
 */
public const val CLIMB_OPEN_WINDOW_KM: Double = 0.3

/** CLIMB_OPEN_GRADE_PERCENT is the grade over the open window at which a climb opens, `openGradePercent`. */
public const val CLIMB_OPEN_GRADE_PERCENT: Double = 3.0

/**
 * CLIMB_CLOSE_GRADE_PERCENT is the grade under which a climb has levelled off, `closeGradePercent`:
 * lower than the opening grade, so a brief easing does not split one ascent in two.
 */
public const val CLIMB_CLOSE_GRADE_PERCENT: Double = 1.0

/**
 * CLIMB_CLOSE_WINDOW_KM is how far the grade must stay under [CLIMB_CLOSE_GRADE_PERCENT] before a
 * climb closes at its summit, `closeWindowKm`.
 */
public const val CLIMB_CLOSE_WINDOW_KM: Double = 0.5

/** MIN_CLIMB_LENGTH_KM is the shortest climb kept, foot to summit, `minClimbLengthKm`: a badge on a bump reads as noise. */
public const val MIN_CLIMB_LENGTH_KM: Double = 0.5

/** The meters in a kilometer. */
private const val METERS_PER_KILOMETER = 1_000.0

/**
 * detectClimbs is the categorized climbs on [points], ascending by distance, in order of their
 * start.
 *
 * A climb opens where the grade from the last sample at least [CLIMB_OPEN_WINDOW_KM] back reaches
 * [CLIMB_OPEN_GRADE_PERCENT], its foot at that sample, and tracks its summit, the highest sample so
 * far. It closes once that grade has stayed under [CLIMB_CLOSE_GRADE_PERCENT] for
 * [CLIMB_CLOSE_WINDOW_KM], or at the end of the series, and the span is cut at its summit, not at the
 * flat that followed. A span shorter than [MIN_CLIMB_LENGTH_KM], or one [climbCategoryFor] gives no
 * category, is dropped.
 *
 * Every window is read in kilometers as the samples carry them, the run a difference of two
 * distances, so 1.2 − 0.9, a hair under 0.3, is short of the open window.
 */
public fun detectClimbs(points: List<RouteProfilePoint>): List<Climb> {
    val climbs = ArrayList<Climb>()
    if (points.size < 2) return climbs
    var open: OpenClimb? = null
    var easingSinceKm: Double? = null
    for (index in 1 until points.size) {
        val point = points[index]
        val grade = rollingGradePercent(points, index) ?: continue
        val climb = open
        if (climb == null) {
            if (grade >= CLIMB_OPEN_GRADE_PERCENT) {
                val foot = footOf(points, index)
                open = OpenClimb(foot.distanceKm, foot.elevation, point.distanceKm, point.elevation)
            }
            continue
        }
        if (point.elevation > climb.peakElevation) {
            climb.peakElevation = point.elevation
            climb.peakKm = point.distanceKm
            easingSinceKm = null
        }
        if (grade < CLIMB_CLOSE_GRADE_PERCENT) {
            val since = easingSinceKm ?: point.distanceKm
            easingSinceKm = since
            if (point.distanceKm - since >= CLIMB_CLOSE_WINDOW_KM) {
                climb.closedAtSummit()?.let { climbs += it }
                open = null
                easingSinceKm = null
            }
        } else {
            easingSinceKm = null
        }
    }
    open?.closedAtSummit()?.let { climbs += it }
    return climbs
}

/** A climb while it is open: its foot, and the summit it has reached so far. */
private class OpenClimb(
    val startKm: Double,
    val startElevation: Double,
    var peakKm: Double,
    var peakElevation: Double,
) {
    /** The climb cut at its summit, or null where it is too short or too easy to keep. */
    fun closedAtSummit(): Climb? {
        val lengthKm = peakKm - startKm
        val lengthMeters = lengthKm * METERS_PER_KILOMETER
        val rise = peakElevation - startElevation
        val ratio = if (lengthKm > 0.0) rise / lengthMeters else 0.0
        val avgGradePercent = convertProportion(ratio, ProportionUnit.Ratio, ProportionUnit.Percent)
        val category = climbCategoryFor(lengthMeters, avgGradePercent) ?: return null
        if (!(lengthKm >= MIN_CLIMB_LENGTH_KM)) return null
        return Climb(startKm, peakKm, peakElevation, avgGradePercent, category)
    }
}

/**
 * The grade, in percent, from the last sample at least [CLIMB_OPEN_WINDOW_KM] before the sample at
 * [index] to it, or null while the series has not covered a full window.
 */
private fun rollingGradePercent(
    points: List<RouteProfilePoint>,
    index: Int,
): Double? {
    val end = points[index]
    for (back in index - 1 downTo 0) {
        val start = points[back]
        val runKm = end.distanceKm - start.distanceKm
        if (runKm >= CLIMB_OPEN_WINDOW_KM) {
            return convertProportion(
                (end.elevation - start.elevation) / (runKm * METERS_PER_KILOMETER),
                ProportionUnit.Ratio,
                ProportionUnit.Percent,
            )
        }
    }
    return null
}

/** The foot of a climb opening at the sample at [index]: the last sample at least the open window back. */
private fun footOf(
    points: List<RouteProfilePoint>,
    index: Int,
): RouteProfilePoint {
    val point = points[index]
    for (back in index - 1 downTo 0) {
        if (point.distanceKm - points[back].distanceKm >= CLIMB_OPEN_WINDOW_KM) return points[back]
    }
    return point
}
