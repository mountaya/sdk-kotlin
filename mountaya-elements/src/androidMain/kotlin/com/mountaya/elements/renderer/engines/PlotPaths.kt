package com.mountaya.elements

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import com.mountaya.geo.ElevationDomain
import com.mountaya.geo.ElevationSeries
import com.mountaya.geo.ProfileRange
import com.mountaya.geo.profileAreaPath
import com.mountaya.geo.profilePolyline

/**
 * PlotPaths is a profile's curve as its chart draws it: the area under the curve and its line, read once in the 100-unit
 * square for one series, domain and window, the whole itinerary where the window is null, as a navigator draws it, and
 * as Compose paths scaled to the chart's size, built once for each size the chart is drawn at and kept. A chart drawn
 * again at its size, as a scrub's cursor moves over it, builds no path.
 */
internal class PlotPaths(
    series: ElevationSeries,
    domain: ElevationDomain,
    window: ProfileRange?,
) {
    /** The area under the curve, in the 100-unit square. */
    private val area = profileAreaPath(series, domain, window)

    /** The curve's line, in the 100-unit square. */
    private val line = profilePolyline(series, domain, window)

    /** The size the paths kept were built at, or null before the first. */
    private var builtAt: Size? = null

    private var kept: Scaled? = null

    /** How many times its paths were built, one size at a time. */
    var builds: Int = 0
        private set

    /** How many times a chart drew them, which a scrub that only moves the cursor leaves as it is. */
    var draws: Int = 0
        private set

    /** Scaled is the area and the line as Compose draws them at one size. */
    class Scaled(
        val area: Path,
        val line: Path,
    )

    /** The area and the line scaled to [size]: the ones kept where it is the size they were built at, new ones otherwise. */
    fun at(size: Size): Scaled {
        draws += 1
        val held = kept
        if (held != null && size == builtAt) return held
        builds += 1
        builtAt = size
        return Scaled(plotPath(area, size), plotLine(line, size)).also { kept = it }
    }
}
