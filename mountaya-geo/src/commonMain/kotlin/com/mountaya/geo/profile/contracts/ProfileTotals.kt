package com.mountaya.geo

/**
 * ProfileTotals is the figures of an itinerary, or of one stretch of it, read off its elevation
 * series: its length, the meters it climbs and drops, its altitude band and its steepest grade.
 * Every figure but the length is null where the series has no sample to read it from, since a 2D
 * itinerary has no ascent.
 *
 * Two are equal where they hold the same numbers, as every value of the geo layer compares.
 */
public class ProfileTotals(
    /** The length, in meters: the itinerary's, or the stretch's own. */
    public val distance: Double,
    /** The meters climbed, rises under the noise threshold left out. */
    public val ascent: Double?,
    /** The meters dropped, falls under the noise threshold left out. */
    public val descent: Double?,
    /** The lowest altitude, in meters. */
    public val elevationMin: Double?,
    /** The highest altitude, in meters. */
    public val elevationMax: Double?,
    /** The steepest ascending grade over a run of the grade window, in degrees, never under 0. */
    public val gradeMax: Double?,
) {
    override fun equals(other: Any?): Boolean =
        other is ProfileTotals &&
            sameNumber(other.distance, distance) &&
            sameNumberOrNull(other.ascent, ascent) &&
            sameNumberOrNull(other.descent, descent) &&
            sameNumberOrNull(other.elevationMin, elevationMin) &&
            sameNumberOrNull(other.elevationMax, elevationMax) &&
            sameNumberOrNull(other.gradeMax, gradeMax)

    override fun hashCode(): Int = listOf(distance, ascent, descent, elevationMin, elevationMax, gradeMax).hashCode()

    override fun toString(): String =
        "ProfileTotals(distance=$distance, ascent=$ascent, descent=$descent, elevationMin=$elevationMin, " +
            "elevationMax=$elevationMax, gradeMax=$gradeMax)"
}
