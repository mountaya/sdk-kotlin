package com.mountaya.geo

/**
 * profilePolyline is the line a profile's curve is drawn as (`geo.profile.chart-path`): the samples of
 * [series] joined in the order the series holds them, each where [profileX] and [profileY] draw it against
 * [domain].
 *
 * A [window], its two ends in either order, draws the samples within it and the one just outside each end,
 * where there is one, at the x the window places them unclamped, so the line leaves the frame at the
 * ground's own slope rather than standing upright against its edges; a renderer clips what runs past the
 * frame. A window no sample reaches draws nothing, and so does a series with no sample.
 */
public fun profilePolyline(
    series: ElevationSeries,
    domain: ElevationDomain,
    window: ProfileRange? = null,
): List<PlotPoint> {
    val span = orderedWindow(window, series.totalDistance)
    return drawnSamples(series.points, window).map {
        PlotPoint(projectX(it.distance, span), profileY(it.elevation, domain))
    }
}

/**
 * drawnSamples is the samples a curve drawn over [window] draws: every one with no window, else those the
 * window holds and the one just outside each end, and none where no sample stands at or past the window's
 * start.
 */
internal fun drawnSamples(
    points: List<ElevationPoint>,
    window: ProfileRange?,
): List<ElevationPoint> {
    if (window == null) return points
    val from = minOf(window.from, window.to)
    val to = maxOf(window.from, window.to)
    var first = points.indexOfFirst { it.distance >= from }
    if (first < 0) return emptyList()
    var last = points.size - 1
    while (last > first && points[last].distance > to) last -= 1
    first = maxOf(0, first - 1)
    last = minOf(points.size - 1, last + 1)
    return points.subList(first, last + 1)
}
