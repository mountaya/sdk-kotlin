package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * ProfileCharacteristics is the summary an itinerary's profile states, folded from its segments'
 * stored metrics and its waypoints. Each figure is null until a segment states it.
 *
 * Two are equal where they hold the same figures, numbers compared as every value of the geo layer
 * compares.
 */
public class ProfileCharacteristics(
    /** The segments' distances summed, in meters. */
    public val distance: Double?,
    /** The segments' durations summed, in seconds: moving time, the stops apart. */
    public val duration: Double?,
    /** Whether some segments state a duration and others do not, so [duration] is not the whole route's. */
    public val durationPartial: Boolean,
    /** The segments' ascents summed, in meters. */
    public val ascent: Int?,
    /** The segments' descents summed, in meters. */
    public val descent: Int?,
    /** The lowest altitude any segment states, in meters. */
    public val altitudeMin: Int?,
    /** The highest altitude any segment states, in meters. */
    public val altitudeMax: Int?,
    /** The steepest descending gradient any segment states, in degrees. */
    public val slopeMin: Double?,
    /** The steepest ascending gradient any segment states, in degrees. */
    public val slopeMax: Double?,
    /** The waypoints' planned stops summed, in seconds, apart from [duration] since it is no moving time. */
    public val stopDuration: Int?,
    /** The easiest stretch's trail difficulty, by its rank within its scale. */
    public val trailDifficultyMin: RestSchema.TrailDifficultyType?,
    /** The hardest stretch's trail difficulty, by its rank within its scale. */
    public val trailDifficultyMax: RestSchema.TrailDifficultyType?,
) {
    override fun equals(other: Any?): Boolean =
        other is ProfileCharacteristics &&
            sameNumberOrNull(other.distance, distance) &&
            sameNumberOrNull(other.duration, duration) &&
            other.durationPartial == durationPartial &&
            other.ascent == ascent &&
            other.descent == descent &&
            other.altitudeMin == altitudeMin &&
            other.altitudeMax == altitudeMax &&
            sameNumberOrNull(other.slopeMin, slopeMin) &&
            sameNumberOrNull(other.slopeMax, slopeMax) &&
            other.stopDuration == stopDuration &&
            other.trailDifficultyMin == trailDifficultyMin &&
            other.trailDifficultyMax == trailDifficultyMax

    override fun hashCode(): Int =
        listOf(
            distance,
            duration,
            durationPartial,
            ascent,
            descent,
            altitudeMin,
            altitudeMax,
            slopeMin,
            slopeMax,
            stopDuration,
            trailDifficultyMin,
            trailDifficultyMax,
        ).hashCode()

    override fun toString(): String =
        "ProfileCharacteristics(distance=$distance, duration=$duration, durationPartial=$durationPartial, " +
            "ascent=$ascent, descent=$descent, altitudeMin=$altitudeMin, altitudeMax=$altitudeMax, " +
            "slopeMin=$slopeMin, slopeMax=$slopeMax, stopDuration=$stopDuration, " +
            "trailDifficultyMin=$trailDifficultyMin, trailDifficultyMax=$trailDifficultyMax)"
}
