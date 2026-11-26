package com.mountaya.geo

/**
 * profileAreaPath is the area filled under a profile's curve (`geo.profile.chart-path`): a move to the first
 * drawn sample's x at the bottom of the square, a line along each sample of [profilePolyline], a line down to
 * the bottom of the square at the last drawn sample's x, and a close. It is closed against the bottom of the
 * square rather than the baseline, so the fill reads as ground under the itinerary, and is empty where the
 * curve draws nothing.
 */
public fun profileAreaPath(
    series: ElevationSeries,
    domain: ElevationDomain,
    window: ProfileRange? = null,
): List<PlotCommand> {
    val line = profilePolyline(series, domain, window)
    val first = line.firstOrNull() ?: return emptyList()
    val last = line.last()
    return listOf(PlotCommand.Move(PlotPoint(first.x, PROFILE_VIEW_BOX))) +
        line.map { PlotCommand.Line(it) } +
        listOf(PlotCommand.Line(PlotPoint(last.x, PROFILE_VIEW_BOX)), PlotCommand.Close)
}
