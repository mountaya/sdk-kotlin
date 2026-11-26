package com.mountaya.geo

/**
 * Climb is one categorized climb on an itinerary's distance axis: the stretch from its foot to its
 * summit, the summit's altitude, its average grade and its category.
 *
 * Two are equal where they hold the same numbers and the same category, as every value of the geo
 * layer compares.
 */
public class Climb(
    /** Where the climb's foot stands, in kilometers from the itinerary's start. */
    public val startKm: Double,
    /** Where its summit stands, in kilometers from the itinerary's start. */
    public val endKm: Double,
    /** The summit's altitude, in meters. */
    public val peakElevation: Double,
    /** The rise over the run from the foot to the summit, in percent. */
    public val avgGradePercent: Double,
    /** The category its score reaches. */
    public val category: ClimbCategory,
) {
    override fun equals(other: Any?): Boolean =
        other is Climb &&
            sameNumber(other.startKm, startKm) &&
            sameNumber(other.endKm, endKm) &&
            sameNumber(other.peakElevation, peakElevation) &&
            sameNumber(other.avgGradePercent, avgGradePercent) &&
            other.category == category

    override fun hashCode(): Int = listOf(startKm, endKm, peakElevation, avgGradePercent, category).hashCode()

    override fun toString(): String =
        "Climb(startKm=$startKm, endKm=$endKm, peakElevation=$peakElevation, avgGradePercent=$avgGradePercent, category=$category)"
}
