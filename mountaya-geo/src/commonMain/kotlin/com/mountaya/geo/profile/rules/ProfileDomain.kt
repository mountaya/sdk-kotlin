package com.mountaya.geo

/**
 * profileDomain is the elevation range [series] is drawn against (`geo.profile.domain`), anchored on the
 * itinerary's own extent rather than on sea level, since the shape of a climb is what a profile is read for:
 * a ridge between 2,400 m and 2,600 m is a ridge, not a flat line four fifths of the way up an empty chart.
 *
 * With no [window] it frames the series' stated extent, `minElevation` to `maxElevation`. A window, its two
 * ends in either order, frames the stretch it holds: the lowest and the highest the curve stands between the
 * window's ends, read at each end on the line between the two samples either side of it and at every sample
 * between them. A window that holds the whole curve is no zoom and frames the stated extent, since the
 * samples are thinned and the extent is not; and so does a window the samples do not reach, so the frame
 * keeps its scale with no curve in it. The extent is then widened and rounded by [extentDomain].
 */
public fun profileDomain(
    series: ElevationSeries,
    window: ProfileRange? = null,
): ElevationDomain {
    val extent = windowedExtent(series, window)
    return extentDomain(extent.first, extent.second)
}

/** The elevation range of [series] over [window], the stated extent where the window frames the whole series or none of it. */
private fun windowedExtent(
    series: ElevationSeries,
    window: ProfileRange?,
): Pair<Double?, Double?> {
    val whole = series.minElevation to series.maxElevation
    val first = series.points.firstOrNull()
    val last = series.points.lastOrNull()
    if (window == null || first == null || last == null) return whole
    val from = minOf(window.from, window.to)
    val to = maxOf(window.from, window.to)
    if (from <= first.distance && to >= last.distance) return whole
    val band = altitudeBand(series.points, from, to) ?: return whole
    return band.first to band.second
}
