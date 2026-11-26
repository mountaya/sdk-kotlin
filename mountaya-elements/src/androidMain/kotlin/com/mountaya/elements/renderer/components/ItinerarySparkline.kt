package com.mountaya.elements

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.mountaya.geo.ElevationSeries
import com.mountaya.geo.HexColor
import com.mountaya.geo.PROFILE_VIEW_BOX
import com.mountaya.geo.ProfileRange
import com.mountaya.geo.profileAreaPath
import com.mountaya.geo.profileDomain
import com.mountaya.geo.profilePolyline
import com.mountaya.geo.profileX

/** How opaque the lit stretch's area is in a sparkline. */
internal const val SPARKLINE_LIT_OPACITY: Float = 0.35f

/**
 * ItinerarySparkline is an itinerary's whole curve drawn small, the stretch [lit] drawn over it in [color], the accent
 * where it names none: the shape of a leg, or of the stretch reaching a place, within the itinerary a table row
 * belongs to.
 *
 * ```
 * ItinerarySparkline(series, Modifier.size(96.dp, 24.dp), lit = ProfileRange(1_200.0, 3_400.0))
 * ```
 *
 * The whole curve is drawn in the `table-spark-base` token and the lit stretch in the colour, each read on the
 * profile's own scale (`geo.profile.plot-scale`, `geo.profile.chart-path`), so a sparkline and the chart beside it
 * draw one line. It is hidden from accessibility services, the row it stands in stating its figures.
 *
 * @param series The itinerary's series.
 * @param modifier The host's modifier, as every composable takes one; the sparkline fills the size it is given.
 * @param lit The stretch drawn over the curve, or null for none.
 * @param color The colour the lit stretch is drawn in, a CSS hexadecimal colour, the accent where null.
 */
@Composable
public fun ItinerarySparkline(
    series: ElevationSeries,
    modifier: Modifier = Modifier,
    lit: ProfileRange? = null,
    color: String? = null,
) {
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val base = tokenColor(tokens.tableSparkBase, scheme)
    val shade = color?.let { hexColor(HexColor(it)) } ?: tokenColor(tokens.accent, scheme)
    val domain = remember(series) { profileDomain(series) }
    val area = remember(series, domain) { profileAreaPath(series, domain) }
    val line = remember(series, domain) { profilePolyline(series, domain) }
    val total = series.totalDistance
    Canvas(modifier.clearAndSetSemantics {}) {
        val areaPath = plotPath(area, size)
        drawPath(areaPath, base)
        if (lit == null) return@Canvas
        val start = (profileX(lit.from, total) / PROFILE_VIEW_BOX * size.width).toFloat()
        val end = (profileX(lit.to, total) / PROFILE_VIEW_BOX * size.width).toFloat()
        if (!(end > start)) return@Canvas
        clipRect(left = start, right = end) {
            drawPath(areaPath, shade.copy(alpha = shade.alpha * SPARKLINE_LIT_OPACITY))
            drawPath(plotLine(line, size), shade, style = Stroke(width = 1.5.dp.toPx()))
        }
    }
}
