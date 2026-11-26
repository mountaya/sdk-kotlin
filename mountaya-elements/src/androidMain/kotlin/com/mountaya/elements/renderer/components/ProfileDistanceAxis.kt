package com.mountaya.elements

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountaya.geo.AxisLabelBox
import com.mountaya.geo.CurveDivider
import com.mountaya.geo.PROFILE_VIEW_BOX
import com.mountaya.geo.ProfileRange
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.cullCrowdedLabels
import com.mountaya.geo.distanceTicks
import com.mountaya.geo.formatDistanceTick
import com.mountaya.geo.profileX

/** The height of a profile's distance axis at a font scale of 1, in density-independent pixels. */
internal const val PROFILE_DISTANCE_AXIS_HEIGHT_DP: Int = 32

/** How far down the axis its stops' labels stand, their middles, in density-independent pixels at a font scale of 1. */
private const val TICK_LABEL_DP = 7f

/** How far down the axis the places' names stand, their middles, in density-independent pixels at a font scale of 1. */
private const val PLACE_NAME_DP = 24f

/** The style a profile's distance axis writes its labels and names in. */
internal val profileAxisStyle: TextStyle = TextStyle(fontSize = 11.sp)

/**
 * ProfileDistanceAxis is the row under a profile's chart: the distance its [window] spans, labelled at the stops of the
 * reader's unit (`distanceTicks`), and under them the names of the places that divide the itinerary, [dividers], each
 * centred under its place, the first and the last always drawn and a name between them left out where it would crowd
 * its neighbours (`geo.profile.axis-label-culling`).
 *
 * The row is 32 density-independent pixels high, taller as the reader's font scale draws its two lines larger, each
 * line standing where it stands at a scale of 1 grown by as much, so neither clips.
 */
@Composable
internal fun ProfileDistanceAxis(
    totalDistance: Double,
    window: ProfileRange,
    dividers: List<CurveDivider>,
    units: UnitPreferences,
    modifier: Modifier = Modifier,
) {
    LocalMountayaRenderProbe.current?.report("ProfileDistanceAxis")
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val dimmed = tokenColor(tokens.dimmed, scheme)
    val ink = tokenColor(tokens.panelFg, scheme)
    val density = LocalDensity.current.density
    val grown = textGrowth(LocalDensity.current, profileAxisStyle.fontSize)
    val measurer = rememberTextMeasurer()
    BoxWithConstraints(modifier.fillMaxWidth().height((PROFILE_DISTANCE_AXIS_HEIGHT_DP * grown).dp)) {
        val width = constraints.maxWidth.toFloat()
        for (tick in distanceTicks(window.from, window.to, units.length)) {
            val x = (profileX(tick.meters, totalDistance, window) / PROFILE_VIEW_BOX * width).toFloat()
            BasicText(
                formatDistanceTick(tick, units),
                Modifier.centredAt(x, TICK_LABEL_DP * grown * density),
                style = profileAxisStyle.copy(color = dimmed),
                softWrap = false,
            )
        }
        for (divider in shownProfileNames(dividers, width, density, measurer)) {
            BasicText(
                divider.name,
                Modifier.centredAt(profileNameCentre(divider, width, measurer), PLACE_NAME_DP * grown * density),
                style = profileAxisStyle.copy(color = ink),
                softWrap = false,
            )
        }
    }
}

/**
 * shownProfileNames is the [dividers] whose names are drawn under a row [width] pixels wide: each laid out centred
 * under its place and kept in the row, measured by [measurer], and a crowded one between the first and the last left
 * out, the gutter between two names in density-independent pixels at [density].
 */
internal fun shownProfileNames(
    dividers: List<CurveDivider>,
    width: Float,
    density: Float,
    measurer: TextMeasurer,
): List<CurveDivider> {
    val boxes =
        dividers.map { divider ->
            val half = textWidth(divider.name, measurer, profileAxisStyle) / 2f
            val centre = profileNameCentre(divider, width, measurer)
            AxisLabelBox(divider.key, ((centre - half) / density).toDouble(), ((centre + half) / density).toDouble())
        }
    val hidden = cullCrowdedLabels(boxes).toSet()
    return dividers.filter { it.key !in hidden }
}

/** The centre of a divider's name, in pixels, under its place, kept so the name stays in a row [width] pixels wide. */
private fun profileNameCentre(
    divider: CurveDivider,
    width: Float,
    measurer: TextMeasurer,
): Float {
    val half = textWidth(divider.name, measurer, profileAxisStyle) / 2f
    val x = (divider.x / PROFILE_VIEW_BOX * width).toFloat()
    return x.coerceAtLeast(half).coerceAtMost(maxOf(half, width - half))
}
