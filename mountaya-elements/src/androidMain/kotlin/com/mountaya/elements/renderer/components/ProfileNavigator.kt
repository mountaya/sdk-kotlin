package com.mountaya.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.mountaya.geo.ElevationDomain
import com.mountaya.geo.ElevationSeries
import com.mountaya.geo.PROFILE_SCRUB_COLUMNS
import com.mountaya.geo.PROFILE_VIEW_BOX
import com.mountaya.geo.ProfileRange
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.clampRange
import com.mountaya.geo.formatDistance
import com.mountaya.geo.profileX
import com.mountaya.geo.scrubDistance
import kotlin.math.abs
import kotlin.math.floor

/** The height of a profile's navigator, in density-independent pixels. */
internal const val PROFILE_NAVIGATOR_HEIGHT_DP: Int = 36

/** How near a handle a drag must start to grab it, in density-independent pixels. */
internal const val NAVIGATOR_HANDLE_REACH_DP: Float = 22f

/** The share of the itinerary an accessibility service moves a handle by, a step. */
private const val NAVIGATOR_STEPS = 20

/**
 * NavigatorGrab is what a drag over a profile's navigator moves, [part], and the [window] it started from.
 */
internal class NavigatorGrab(
    val part: Part,
    val window: ProfileRange,
) {
    /** Part is the window's start, its end, or the window whole. */
    enum class Part { Start, End, Whole }
}

/**
 * ProfileNavigator is the strip under a profile's chart showing the whole itinerary, the [window] drawn marked on it
 * and the rest veiled: a reader drags either handle to move that end of the window, or the window itself to move it
 * whole, and each move is held to the itinerary and to the narrowest window (`geo.profile.window-clamp`) and handed to
 * [onWindowChange]. An edge moves to the distance a column of the strip stands for, never snapped to a sample
 * (`geo.profile.scrub-grid`). Each handle is adjustable by accessibility services, a step a twentieth of the
 * itinerary. A navigator not [enabled] draws and drives nothing.
 *
 * Its curve is drawn on a layer of its own, from paths built once for each size it is drawn at, so a redraw of the chart
 * above it, a scrub's above all, draws it again nowhere (`profile-cursor` in the contract's `sdk/hot-paths.yaml`).
 */
@Composable
internal fun ProfileNavigator(
    series: ElevationSeries,
    domain: ElevationDomain,
    window: ProfileRange,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onWindowChange: (ProfileRange) -> Unit,
) {
    val probe = LocalMountayaRenderProbe.current
    probe?.report("ProfileNavigator")
    val scope = LocalMountayaScope.current
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val density = LocalDensity.current.density
    val accent = tokenColor(tokens.accent, scheme)
    val veil = tokenColor(tokens.profileNavigatorVeil, scheme)
    val handleColor = tokenColor(tokens.profileNavigatorHandle, scheme)
    // The whole curve, read once for its series and its domain and built once for each size it is drawn at.
    val overview = remember(series, domain) { PlotPaths(series, domain, null) }
    val shown by rememberUpdatedState(window)
    val change by rememberUpdatedState(onWindowChange)
    val total = series.totalDistance
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(PROFILE_NAVIGATOR_HEIGHT_DP.dp)
            .semantics { contentDescription = scope.word("profile.navigator") }
            .pointerInput(enabled, total) {
                if (!enabled) return@pointerInput
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitPointerEvent().changes.firstOrNull { it.pressed } ?: continue
                        val width = size.width.toFloat()
                        val start = (profileX(shown.from, total) / PROFILE_VIEW_BOX * width).toFloat()
                        val end = (profileX(shown.to, total) / PROFILE_VIEW_BOX * width).toFloat()
                        val grab =
                            NavigatorGrab(
                                navigatorPart(down.position.x, start, end, NAVIGATOR_HANDLE_REACH_DP * density),
                                shown,
                            )
                        change(movedWindow(grab, down.position.x, down.position.x, width, total))
                        while (true) {
                            val moving = awaitPointerEvent().changes.firstOrNull() ?: break
                            if (!moving.pressed) break
                            change(movedWindow(grab, down.position.x, moving.position.x, width, total))
                        }
                    }
                }
            },
    ) {
        val start = maxWidth * (profileX(window.from, total) / PROFILE_VIEW_BOX).toFloat()
        val end = maxWidth * (profileX(window.to, total) / PROFILE_VIEW_BOX).toFloat()
        // The whole curve, small, on a layer of its own, so a redraw around it, a scrub's above all, draws it again nowhere.
        Spacer(
            Modifier.fillMaxSize().clearAndSetSemantics {}.graphicsLayer().drawBehind {
                probe?.report("ProfileNavigator curve drawn")
                val scaled = overview.at(size)
                drawPath(scaled.area, accent.copy(alpha = accent.alpha * PROFILE_AREA_OPACITY))
                drawPath(scaled.line, accent, style = Stroke(width = 1.dp.toPx()))
            },
        )
        Box(Modifier.width(start).fillMaxHeight().background(veil))
        Box(
            Modifier
                .offset(x = end)
                .width((maxWidth - end).coerceAtLeast(0.dp))
                .fillMaxHeight()
                .background(veil),
        )
        NavigatorHandle(
            scope.word("profile.rangeStart"),
            window.from,
            total,
            enabled,
            handleColor,
            Modifier.offset(x = start - 22.dp),
        ) { change(clampRange(ProfileRange(it, window.to), total)) }
        NavigatorHandle(
            scope.word("profile.rangeEnd"),
            window.to,
            total,
            enabled,
            handleColor,
            Modifier.offset(x = end - 22.dp),
        ) { change(clampRange(ProfileRange(window.from, it), total)) }
    }
}

/** A handle at one end of the window, standing at [edge] meters, adjustable by accessibility services a step at a time. */
@Composable
private fun NavigatorHandle(
    word: String,
    edge: Double,
    total: Double,
    enabled: Boolean,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier,
    onEdgeChange: (Double) -> Unit,
) {
    val scope = LocalMountayaScope.current
    Box(
        modifier
            .size(44.dp, PROFILE_NAVIGATOR_HEIGHT_DP.dp)
            .semantics {
                contentDescription = word
                stateDescription = formatDistance(edge, UnitPreferences(scope.locale))
                if (total > 0.0) {
                    progressBarRangeInfo =
                        ProgressBarRangeInfo(edge.toFloat(), 0f..total.toFloat(), steps = NAVIGATOR_STEPS - 1)
                    if (enabled) {
                        setProgress { target ->
                            val step = total / NAVIGATOR_STEPS
                            onEdgeChange(
                                if (target > edge) {
                                    edge + step
                                } else if (target < edge) {
                                    edge - step
                                } else {
                                    edge
                                },
                            )
                            true
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(6.dp, 28.dp).background(color, RoundedCornerShape(2.dp)))
    }
}

/** navigatorPart is what a drag starting at [x] grabs: a handle within [reach] pixels of it, else the window. */
internal fun navigatorPart(
    x: Float,
    start: Float,
    end: Float,
    reach: Float,
): NavigatorGrab.Part =
    when {
        abs(x - start) <= reach -> NavigatorGrab.Part.Start
        abs(x - end) <= reach -> NavigatorGrab.Part.End
        else -> NavigatorGrab.Part.Whole
    }

/**
 * movedWindow is the window a drag that grabbed [grab] at [startX] leaves at [x], across a strip [width] pixels wide
 * over an itinerary of [total] meters: a handle's end moved to the distance the column under [x] stands for
 * (`geo.profile.scrub-grid`), a point past the strip read as its last column, or the whole window moved by the
 * distance between the two columns, kept inside the itinerary; either is then held to the itinerary and the
 * narrowest window (`geo.profile.window-clamp`).
 */
internal fun movedWindow(
    grab: NavigatorGrab,
    startX: Float,
    x: Float,
    width: Float,
    total: Double,
): ProfileRange {
    if (!(width > 0f)) return grab.window
    val whole = ProfileRange(0.0, total)
    val distance = { at: Float ->
        val column = floor(at / width * PROFILE_SCRUB_COLUMNS).toInt()
        scrubDistance(column.coerceIn(0, PROFILE_SCRUB_COLUMNS - 1), whole)
    }
    val moved =
        when (grab.part) {
            NavigatorGrab.Part.Start -> {
                ProfileRange(distance(x), grab.window.to)
            }

            NavigatorGrab.Part.End -> {
                ProfileRange(grab.window.from, distance(x))
            }

            NavigatorGrab.Part.Whole -> {
                val span = grab.window.to - grab.window.from
                val from =
                    (grab.window.from + distance(x) - distance(startX))
                        .coerceAtLeast(0.0)
                        .coerceAtMost(maxOf(0.0, total - span))
                ProfileRange(from, from + span)
            }
        }
    return clampRange(moved, total)
}
