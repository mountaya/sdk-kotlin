package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * ResolvedCharacteristics is an itinerary's figures as a surface states them, each the one stated over the one
 * derived (`resolveCharacteristics`): what the server computed where it computed it, and what the profile reads
 * off the geometry where it did not, plus the three no geometry answers, the duration, the steepest descent and
 * the trail difficulty.
 *
 * Meters, seconds and degrees, a loss stated positive, every figure null where there is none. Two are equal
 * where they hold the same figures, each number by the same-number relation and each difficulty as written, as
 * every value of the geo layer compares.
 */
public class ResolvedCharacteristics(
    /** The itinerary's length, in meters. */
    public val distance: Double? = null,
    /** The seconds the itinerary is expected to take. */
    public val duration: Double? = null,
    /** The meters climbed. */
    public val ascent: Double? = null,
    /** The meters lost, stated positive. */
    public val descent: Double? = null,
    /** The lowest altitude, in meters above sea level. */
    public val altitudeMin: Double? = null,
    /** The highest altitude, in meters above sea level. */
    public val altitudeMax: Double? = null,
    /** The steepest descending grade, in degrees, negative. */
    public val slopeMin: Double? = null,
    /** The steepest ascending grade, in degrees. */
    public val slopeMax: Double? = null,
    /** The easiest stretch's difficulty, as the itinerary writes it. */
    public val trailDifficultyMin: RestSchema.TrailDifficultyType? = null,
    /** The hardest stretch's difficulty, as the itinerary writes it. */
    public val trailDifficultyMax: RestSchema.TrailDifficultyType? = null,
) {
    /** Every figure but the difficulties, in the order the type declares them. */
    private val numbers: List<Double?>
        get() = listOf(distance, duration, ascent, descent, altitudeMin, altitudeMax, slopeMin, slopeMax)

    override fun equals(other: Any?): Boolean =
        other is ResolvedCharacteristics &&
            other.numbers.zip(numbers).all { (a, b) -> sameNumberOrNull(a, b) } &&
            other.trailDifficultyMin == trailDifficultyMin &&
            other.trailDifficultyMax == trailDifficultyMax

    override fun hashCode(): Int =
        (numbers.hashCode() * 31 + trailDifficultyMin.hashCode()) * 31 + trailDifficultyMax.hashCode()

    override fun toString(): String =
        "ResolvedCharacteristics(distance=$distance, duration=$duration, ascent=$ascent, descent=$descent, " +
            "altitudeMin=$altitudeMin, altitudeMax=$altitudeMax, slopeMin=$slopeMin, slopeMax=$slopeMax, " +
            "trailDifficultyMin=$trailDifficultyMin, trailDifficultyMax=$trailDifficultyMax)"
}
