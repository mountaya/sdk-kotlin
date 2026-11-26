package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * committedPace is an itinerary's committed effective pace over its [segments], in km/h: their
 * total stored `distance`, in meters, over their total stored `duration`, in seconds. It is the
 * pace that reproduces the duration the segments state, weighted by duration, never the mean of
 * each segment's own pace.
 *
 * A segment is left out unless its distance is a finite number of zero or more and its duration a
 * finite number above zero: one with no timing is not counted as instant, and a negative or
 * non-finite figure never reaches the total. Null where no segment is left, none at all included.
 */
public fun committedPace(segments: List<RestSchema.ItinerarySegment>): Double? {
    var meters = 0.0
    var seconds = 0.0
    for (segment in segments) {
        val distance = segment.distance ?: continue
        val duration = segment.duration ?: continue
        if (!(distance.isFinite() && distance >= 0.0 && duration.isFinite() && duration > 0.0)) continue
        meters += distance
        seconds += duration
    }
    if (seconds <= 0.0) return null
    return convertSpeed(meters / seconds, SpeedUnit.MeterPerSecond, SpeedUnit.KilometerPerHour)
}
