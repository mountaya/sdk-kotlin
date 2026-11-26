package com.mountaya.geo

/**
 * placeSpans is the stretches a profile draws under its chart, its band strip and its section chips, each
 * where it stands, in the order given (`geo.profile.window-spans`), so the two cannot disagree about a
 * stretch at the edge of a zoomed window.
 *
 * A stretch of [spans], read in order, is drawn only where it overlaps [window], the whole itinerary of
 * [totalDistance] where none is given, its two ends in either order, and cut at the window's ends, so a band
 * stays under the stretch of curve it describes at any zoom. A stretch the window does not reach, one that
 * only touches an end of it, and one of no length, given or cut, are left out rather than clamped onto the
 * window's edge: a sliver of no width is still announced to a screen reader.
 */
public fun <Span : ProfileSpan<Span>> placeSpans(
    spans: List<Span>,
    totalDistance: Double,
    window: ProfileRange? = null,
): List<PlacedSpan<Span>> {
    val shown = orderedWindow(window, totalDistance)
    return spans.mapIndexedNotNull { index, span ->
        val from = minOf(span.from, span.to)
        val to = maxOf(span.from, span.to)
        if (to <= shown.from || from >= shown.to) return@mapIndexedNotNull null
        val start = profileX(from, totalDistance, window)
        val end = profileX(to, totalDistance, window)
        if (end > start) PlacedSpan(span, index, from, to, start, end) else null
    }
}
