package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * profileCharacteristics folds the summary an itinerary's profile states from its [segments]'
 * stored metrics and its [waypoints].
 *
 * Distance, duration, ascent and descent are summed, the altitude band and the slope range,
 * `gradient_min` to `gradient_max`, are the extremes, and each stays null until a segment states
 * it, a figure a segment leaves null being left out. A duration some segments state and others do
 * not is marked partial; none stated is no duration, and not partial. The trail-difficulty range is
 * the easiest and the hardest stretch by [trailDifficultyRank], a grade of no rank, `unknown`
 * included, taking no place on it, and of two grades of one rank the first listed holds. The
 * planned stops are the waypoints' `stop_duration` summed, null where none states one.
 */
public fun profileCharacteristics(
    segments: List<RestSchema.ItinerarySegment>,
    waypoints: List<RestSchema.ItineraryWaypoint> = emptyList(),
): ProfileCharacteristics {
    var distance: Double? = null
    var duration: Double? = null
    var ascent: Int? = null
    var descent: Int? = null
    var altitudeMin: Int? = null
    var altitudeMax: Int? = null
    var slopeMin: Double? = null
    var slopeMax: Double? = null
    var withDuration = 0
    var easiest: Pair<Int, RestSchema.TrailDifficultyType>? = null
    var hardest: Pair<Int, RestSchema.TrailDifficultyType>? = null
    for (segment in segments) {
        distance = sumOrNull(distance, segment.distance)
        duration = sumOrNull(duration, segment.duration)
        if (segment.duration != null) withDuration += 1
        ascent = sumOrNull(ascent, segment.ascent)
        descent = sumOrNull(descent, segment.descent)
        altitudeMin = extremeOrNull(altitudeMin, segment.altitude_min) { x, y -> minOf(x, y) }
        altitudeMax = extremeOrNull(altitudeMax, segment.altitude_max) { x, y -> maxOf(x, y) }
        slopeMin = extremeOrNull(slopeMin, segment.gradient_min) { x, y -> minOf(x, y) }
        slopeMax = extremeOrNull(slopeMax, segment.gradient_max) { x, y -> maxOf(x, y) }
        for (entry in segment.trail_difficulty_info.orEmpty()) {
            val rank = trailDifficultyRank(entry.value) ?: continue
            if (hardest == null || rank > hardest.first) hardest = rank to entry.value
            if (easiest == null || rank < easiest.first) easiest = rank to entry.value
        }
    }
    var stopDuration: Int? = null
    for (waypoint in waypoints) stopDuration = sumOrNull(stopDuration, waypoint.timing?.stop_duration)
    return ProfileCharacteristics(
        distance = distance,
        duration = duration,
        durationPartial = withDuration > 0 && withDuration < segments.size,
        ascent = ascent,
        descent = descent,
        altitudeMin = altitudeMin,
        altitudeMax = altitudeMax,
        slopeMin = slopeMin,
        slopeMax = slopeMax,
        stopDuration = stopDuration,
        trailDifficultyMin = easiest?.second,
        trailDifficultyMax = hardest?.second,
    )
}

/** The sum of [a] and [b], either left out where it is null, and null where both are. */
private fun sumOrNull(
    a: Double?,
    b: Double?,
): Double? =
    if (a == null) {
        b
    } else if (b == null) {
        a
    } else {
        a + b
    }

/** The sum of [a] and [b], either left out where it is null, and null where both are. */
private fun sumOrNull(
    a: Int?,
    b: Int?,
): Int? =
    if (a == null) {
        b
    } else if (b == null) {
        a
    } else {
        a + b
    }

/** The extreme [pick] takes of [a] and [b], either left out where it is null, and null where both are. */
private fun <T> extremeOrNull(
    a: T?,
    b: T?,
    pick: (T, T) -> T,
): T? =
    if (a == null) {
        b
    } else if (b == null) {
        a
    } else {
        pick(a, b)
    }
