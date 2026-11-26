package com.mountaya.geo

/**
 * MIN_CLIMB_GRADE_PERCENT is the average grade under which a stretch is never a categorized climb
 * whatever its length, the `minGradePercent` `geo/climbs.json` declares: a 2 % false flat scores no
 * category even over 50 km.
 */
public const val MIN_CLIMB_GRADE_PERCENT: Double = 3.0

/**
 * climbCategoryFor is the category of a climb [lengthMeters] long at an average grade of
 * [avgGradePercent]: the hardest whose [ClimbCategory.minimumScore] the score, the length times the
 * grade, reaches. 4 km at 4 % scores 16,000, Cat 3, and 8 km at 10 % 80,000, hors catégorie.
 *
 * Null under a [MIN_CLIMB_GRADE_PERCENT] grade, never categorized, under the Cat 4 score, too easy
 * to badge, and where either figure is no finite number.
 */
public fun climbCategoryFor(
    lengthMeters: Double,
    avgGradePercent: Double,
): ClimbCategory? {
    if (!lengthMeters.isFinite() || !avgGradePercent.isFinite()) return null
    if (avgGradePercent < MIN_CLIMB_GRADE_PERCENT) return null
    val score = lengthMeters * avgGradePercent
    return ClimbCategory.entries.lastOrNull { score >= it.minimumScore }
}
