package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * ItinerarySegmentRowMetrics is what one stretch of an itinerary measures, as a segment row states it
 * (`geo.profile.segment-rows`), and, any of its figures given, what a caller states over the figures a row
 * reads off the itinerary's geometry: the server's own, computed over the full track, where the geometry can
 * only estimate them.
 *
 * Meters and seconds throughout, a loss stated positive as the wire states it, and every figure null where
 * there is none, since an itinerary with no altitude has a length and nothing else. Two are equal where they
 * hold the same figures, each number by the same-number relation and the difficulty as written, as every
 * value of the geo layer compares.
 */
public class ItinerarySegmentRowMetrics(
    /** The meters from the start of the itinerary to the end of the stretch. */
    public val distance: Double? = null,
    /** The meters the stretch covers alone. */
    public val sectionDistance: Double? = null,
    /** The meters climbed from the start of the itinerary to the end of the stretch. */
    public val ascent: Double? = null,
    /** The meters lost from the start of the itinerary to the end of the stretch, stated positive. */
    public val descent: Double? = null,
    /** The meters the stretch climbs alone. */
    public val sectionAscent: Double? = null,
    /** The meters the stretch loses alone, stated positive. */
    public val sectionDescent: Double? = null,
    /** The lowest altitude on the stretch, in meters above sea level. */
    public val altitudeMin: Double? = null,
    /** The highest altitude on the stretch, in meters above sea level. */
    public val altitudeMax: Double? = null,
    /** The seconds the stretch is expected to take, which no geometry implies. */
    public val duration: Double? = null,
    /** The stretch's grade as the itinerary's taxonomy writes it, which no geometry implies and no element ranks. */
    public val difficulty: RestSchema.TrailDifficultyType? = null,
) {
    /** Every figure but the difficulty, in the order the type declares them. */
    private val numbers: List<Double?>
        get() =
            listOf(
                distance,
                sectionDistance,
                ascent,
                descent,
                sectionAscent,
                sectionDescent,
                altitudeMin,
                altitudeMax,
                duration,
            )

    override fun equals(other: Any?): Boolean =
        other is ItinerarySegmentRowMetrics &&
            other.numbers.zip(numbers).all { (a, b) -> sameNumberOrNull(a, b) } &&
            other.difficulty == difficulty

    override fun hashCode(): Int = numbers.hashCode() * 31 + difficulty.hashCode()

    override fun toString(): String =
        "ItinerarySegmentRowMetrics(distance=$distance, sectionDistance=$sectionDistance, ascent=$ascent, " +
            "descent=$descent, sectionAscent=$sectionAscent, sectionDescent=$sectionDescent, " +
            "altitudeMin=$altitudeMin, altitudeMax=$altitudeMax, duration=$duration, difficulty=$difficulty)"
}
