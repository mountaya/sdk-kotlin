package com.mountaya.geo

/**
 * curveStops is the gradient a profile's curve is painted with from coloured [stretches] of the itinerary
 * (`geo.profile.curve-stops`), its stops in offset order, each a fraction of the plot's width from 0 to 1.
 *
 * The stretches are read in distance order, one of no length left out, and where two overlap the earlier
 * keeps the shared ground. Each is placed where [profileX] draws its two ends, held to the frame, and
 * painted between two stops of its colour at its two edges, so every change of colour is a hard edge of two
 * stops at one offset rather than a blend. Ground no stretch covers is two stops of no colour, which a
 * renderer paints in its own accent, and a colour stated twice at one offset, two stretches of one colour
 * meeting, is stated once.
 *
 * It is empty, no gradient at all, where no stretch keeps a length in the frame: the signal to paint the
 * curve in one colour.
 */
public fun curveStops(
    stretches: List<CurveColor>,
    totalDistance: Double,
    window: ProfileRange? = null,
): List<CurveStop> {
    val stops = ArrayList<Pair<Double, String?>>()
    var cursor = 0.0
    for (span in curveSpans(stretches)) {
        val start = maxOf(profileX(span.from, totalDistance, window), cursor)
        val end = profileX(span.to, totalDistance, window)
        if (!(end > start)) continue
        if (start > cursor) {
            stops += cursor to null
            stops += start to null
        }
        stops += start to span.color
        stops += end to span.color
        cursor = end
    }
    if (stops.isEmpty()) return emptyList()
    if (cursor < PROFILE_VIEW_BOX) {
        stops += cursor to null
        stops += PROFILE_VIEW_BOX to null
    }
    val kept = ArrayList<CurveStop>(stops.size)
    for ((index, stop) in stops.withIndex()) {
        val before = stops.getOrNull(index - 1)
        if (before != null && before.second == stop.second && before.first == stop.first) continue
        kept += CurveStop(stop.first / PROFILE_VIEW_BOX, stop.second)
    }
    return kept
}
