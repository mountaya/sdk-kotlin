package com.mountaya.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountaya.geo.AmenityStack
import com.mountaya.geo.BadgeSurface
import com.mountaya.geo.CurveColor
import com.mountaya.geo.CurvePin
import com.mountaya.geo.CurvePlacement
import com.mountaya.geo.CurveStop
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.ElevationDomain
import com.mountaya.geo.ElevationPoint
import com.mountaya.geo.ElevationSeries
import com.mountaya.geo.HexColor
import com.mountaya.geo.PROFILE_VIEW_BOX
import com.mountaya.geo.ProfileRange
import com.mountaya.geo.ProfileReadout
import com.mountaya.geo.ReadoutCell
import com.mountaya.geo.ReadoutStat
import com.mountaya.geo.ThemeScheme
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.amenityGlyph
import com.mountaya.geo.amenityName
import com.mountaya.geo.badgeFallback
import com.mountaya.geo.curveStops
import com.mountaya.geo.domainTicks
import com.mountaya.geo.formatDistance
import com.mountaya.geo.formatElevation
import com.mountaya.geo.formatSlope
import com.mountaya.geo.placeGlyph
import com.mountaya.geo.profileX
import com.mountaya.geo.profileY
import com.mountaya.geo.scrubGrid
import com.mountaya.geo.themeTokenValue
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.time.Instant

/** The side of a pin standing on a profile's curve, in density-independent pixels. */
internal const val PROFILE_PIN_SIDE_DP: Float = 20f

/** How far the readout card stands beside the cursor, in density-independent pixels. */
private const val CARD_OFFSET_DP = 70f

/** How near the chart's edges the readout card's middle may come, in density-independent pixels. */
private const val CARD_MARGIN_DP = 60f

/** How far down the chart the readout card's middle stands, in density-independent pixels. */
private const val CARD_TOP_DP = 36f

/** Where across the chart the elevation scale's labels stand, their middles, in density-independent pixels. */
private const val AXIS_LABEL_CENTRE_DP = 28f

/** How far down the chart a cut-off's mark starts, under its time, in density-independent pixels. */
private const val CUTOFF_MARK_TOP_DP = 12f

/** How far down the chart a cut-off's time stands, its middle, in density-independent pixels. */
private const val CUTOFF_TIME_DP = 6f

/** How far over a pin's top its services stand, their middle, in density-independent pixels. */
private const val STACK_GAP_DP = 12f

/** How opaque the area under the curve is. */
internal const val PROFILE_AREA_OPACITY: Float = 0.18f

/**
 * ProfileChart is an itinerary's curve and what stands on it, the plot of an `ItineraryProfile`: the area under the
 * curve and its line in the accent, or in the gradient its coloured stretches paint (`geo.profile.curve-stops`), the
 * leg a reader points at, [lit], tinted under it, the elevation scale's lines, the drop line under each place that
 * divides the itinerary, a dashed mark at each cut-off, a pin on the curve at each place with its services stacked
 * above it, the cursor, and the card that states what the cursor stands on.
 *
 * Everything stands where the profile's rules place it in the 100-unit square stretched over the chart
 * (`geo.profile.plot-scale`, `geo.profile.curve-placement`). A scrub reads the pointer through the window's 240
 * columns, each standing for the sample nearest its centre (`geo.profile.scrub-grid`), so the cursor stands on a
 * sample and never between two: a touch reads the column under it while it is down, a hovering pointer the column
 * under it while it hovers. Accessibility services adjust it a sample at a time. A disabled chart draws and drives
 * nothing.
 *
 * The curve is drawn on a layer of its own from [paths], the area and the line kept for its series and built once for
 * each size, and the cursor on a layer over it. The chart never reads the sample [cursor] answers: the cursor's layer
 * reads it as it is drawn, and the plot's reading and the card where [readout] is handed as they are composed, so a scrub
 * that moves the cursor draws the cursor and what reads it alone, never the curve or a part it does not move
 * (`profile-cursor` in the contract's `sdk/hot-paths.yaml`).
 */
@Composable
internal fun ProfileChart(
    series: ElevationSeries,
    domain: ElevationDomain,
    window: ProfileRange,
    painted: List<CurveColor>,
    placement: CurvePlacement,
    stacks: List<AmenityStack>,
    lit: ProfileRange?,
    cursor: () -> ElevationPoint?,
    readout: ((ElevationPoint) -> ProfileReadout)?,
    showsElevationAxis: Boolean,
    units: UnitPreferences,
    formatCutoffAt: ((Instant) -> String)?,
    enabled: Boolean,
    onScrub: (Int?) -> Unit,
    onWaypointClick: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
    paths: PlotPaths = remember(series, domain, window) { PlotPaths(series, domain, window) },
) {
    val probe = LocalMountayaRenderProbe.current
    probe?.report("ProfileChart")
    probe?.plotPaths = paths
    val scope = LocalMountayaScope.current
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val density = LocalDensity.current.density
    val grid = remember(series, window) { scrubGrid(series.points, window) }
    val scrub by rememberUpdatedState(onScrub)
    val dimmed = tokenColor(tokens.dimmed, scheme)
    BoxWithConstraints(
        modifier.pointerInput(grid, enabled) {
            if (!enabled) return@pointerInput
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull() ?: continue
                    val width = size.width.toFloat()
                    when (event.type) {
                        PointerEventType.Press -> {
                            scrub(profileSampleAt(change.position.x, width, grid))
                        }

                        PointerEventType.Move, PointerEventType.Enter -> {
                            if (change.pressed || change.type != PointerType.Touch) {
                                scrub(profileSampleAt(change.position.x, width, grid))
                            }
                        }

                        PointerEventType.Release, PointerEventType.Exit -> {
                            scrub(null)
                        }
                    }
                }
            }
        },
    ) {
        probe?.report("ProfileChart content")
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        val across = { x: Double -> (x / PROFILE_VIEW_BOX * width).toFloat() }
        val down = { y: Double -> (y / PROFILE_VIEW_BOX * height).toFloat() }
        // The plot as accessibility services read it, then two layers: the curve, drawn from paths kept for its series
        // and its size, and the cursor over it. The reading and the cursor's layer read the cursor themselves, so a
        // scrub that moves it composes the reading again and draws the cursor's layer again, and nothing else.
        ProfilePlotReading(series, window, cursor, units, enabled, { scrub(it) }, Modifier.fillMaxSize())
        ProfileCurve(
            paths,
            series,
            domain,
            window,
            painted,
            placement,
            lit,
            showsElevationAxis,
            scheme,
            Modifier.matchParentSize(),
        )
        ProfileCursor(series, domain, window, cursor, scheme, Modifier.matchParentSize())
        if (showsElevationAxis) {
            for (tick in domainTicks(domain)) {
                BasicText(
                    formatElevation(tick, units),
                    Modifier
                        .centredAt(AXIS_LABEL_CENTRE_DP * density, down(profileY(tick, domain)))
                        .clearAndSetSemantics {},
                    style = TextStyle(color = dimmed, fontSize = 11.sp),
                )
            }
        }
        if (formatCutoffAt != null) {
            for (cutoff in placement.cutoffs) {
                val moment = formatCutoffAt(cutoff.cutoffAt)
                BasicText(
                    moment,
                    Modifier
                        .centredAt(across(cutoff.x), CUTOFF_TIME_DP * density)
                        .clearAndSetSemantics { contentDescription = scope.word("profile.cutoff") + ", " + moment },
                    style = TextStyle(color = dimmed, fontSize = 11.sp),
                    softWrap = false,
                )
            }
        }
        for (stack in stacks) {
            AmenityStackBadges(
                stack,
                scheme,
                Modifier.centredAt(
                    across(stack.pin.x),
                    down(stack.pin.y) - (PROFILE_PIN_SIDE_DP + STACK_GAP_DP) * density,
                ),
            )
        }
        for (pin in placement.pins) {
            ProfilePin(
                pin,
                scheme,
                onWaypointClick,
                Modifier.centredAt(across(pin.x), down(pin.y) - PROFILE_PIN_SIDE_DP / 2f * density),
            )
        }
        if (readout != null) ProfileCursorCard(series, window, cursor, readout, units, scheme, width, density)
    }
}

/**
 * The plot of a profile's chart as accessibility services read it: the chart, an adjustable value standing at the
 * sample [cursor] answers, else at the window's first, which an adjustment scrubs a sample at a time where the chart is
 * [enabled], handing [onScrub] the sample. It reads the cursor itself, so a scrub composes it again and nothing around it.
 */
@Composable
private fun ProfilePlotReading(
    series: ElevationSeries,
    window: ProfileRange,
    cursor: () -> ElevationPoint?,
    units: UnitPreferences,
    enabled: Boolean,
    onScrub: (Int?) -> Unit,
    modifier: Modifier,
) {
    LocalMountayaRenderProbe.current?.report("ProfilePlotReading")
    val scope = LocalMountayaScope.current
    val point = cursor()
    Spacer(
        modifier.semantics {
            contentDescription = scope.word("profile.chart")
            stateDescription = cursorText(point, units)
            plotProgress(series, window, point)?.let { (current, first, last) ->
                progressBarRangeInfo =
                    ProgressBarRangeInfo(
                        current.toFloat(),
                        first.toFloat()..last.toFloat(),
                        steps = (last - first - 1).coerceAtLeast(0),
                    )
                if (enabled) {
                    setProgress { target ->
                        onScrub(target.roundToInt().coerceIn(first, last))
                        true
                    }
                }
            }
        },
    )
}

/**
 * The card at a profile's cursor, beside the sample [cursor] answers, on the side of a chart [width] pixels wide that
 * has room, stating what [readout] reads of the sample; nothing where no cursor stands. It reads the cursor itself, so a
 * scrub composes the card again and nothing around it.
 */
@Composable
private fun ProfileCursorCard(
    series: ElevationSeries,
    window: ProfileRange,
    cursor: () -> ElevationPoint?,
    readout: (ElevationPoint) -> ProfileReadout,
    units: UnitPreferences,
    scheme: ThemeScheme,
    width: Float,
    density: Float,
) {
    val point = cursor() ?: return
    val x = (profileX(point.distance, series.totalDistance, window) / PROFILE_VIEW_BOX * width).toFloat()
    val side = if (x > width / 2f) -CARD_OFFSET_DP else CARD_OFFSET_DP
    val margin = CARD_MARGIN_DP * density
    ProfileReadoutCard(
        readout(point),
        units,
        scheme,
        Modifier.centredAt(
            (x + side * density).coerceAtLeast(margin).coerceAtMost(maxOf(margin, width - margin)),
            CARD_TOP_DP * density,
        ),
    )
}

/**
 * The layer the area, the line and the marks of a profile are drawn on, from [paths], which it builds once for each
 * size it is drawn at. It takes no cursor, and its layer is kept while what it draws is equal, so a scrub that moves
 * the cursor alone draws it again nowhere.
 */
@Composable
private fun ProfileCurve(
    paths: PlotPaths,
    series: ElevationSeries,
    domain: ElevationDomain,
    window: ProfileRange,
    painted: List<CurveColor>,
    placement: CurvePlacement,
    lit: ProfileRange?,
    showsElevationAxis: Boolean,
    scheme: ThemeScheme,
    modifier: Modifier,
) {
    LocalMountayaRenderProbe.current?.report("ProfileCurve")
    val tokens = LocalMountayaTokens.current
    val accent = tokenColor(tokens.accent, scheme)
    val stops = remember(painted, series, window) { curveStops(painted, series.totalDistance, window) }
    val grid = tokenColor(tokens.profileGridColor, scheme)
    val litFill = tokenColor(tokens.profileLitFill, scheme)
    val dropLine = tokenColor(tokens.profileWaypointLine, scheme)
    val dimmed = tokenColor(tokens.dimmed, scheme)
    // The layer is kept while what it draws is equal, whatever instances carry it, so a composition that hands it the
    // same values again, a scrub's, records nothing again.
    val drawn =
        remember(
            paths,
            series,
            domain,
            window,
            stops,
            placement,
            lit,
            showsElevationAxis,
            accent,
            grid,
            litFill,
            dropLine,
            dimmed,
        ) {
            Modifier.graphicsLayer().drawBehind {
                drawCurve(
                    paths,
                    series,
                    domain,
                    window,
                    stops,
                    placement,
                    lit,
                    showsElevationAxis,
                    accent,
                    grid,
                    litFill,
                    dropLine,
                    dimmed,
                )
            }
        }
    Spacer(modifier.then(drawn))
}

/** Draws a profile's curve, from [paths], and the marks beside it, the lit leg tinted under it, on the layer it is drawn on. */
private fun DrawScope.drawCurve(
    paths: PlotPaths,
    series: ElevationSeries,
    domain: ElevationDomain,
    window: ProfileRange,
    stops: List<CurveStop>,
    placement: CurvePlacement,
    lit: ProfileRange?,
    showsElevationAxis: Boolean,
    accent: Color,
    grid: Color,
    litFill: Color,
    dropLine: Color,
    dimmed: Color,
) {
    val across = { x: Double -> (x / PROFILE_VIEW_BOX * size.width).toFloat() }
    val down = { y: Double -> (y / PROFILE_VIEW_BOX * size.height).toFloat() }
    val scaled = paths.at(size)
    val areaPath = scaled.area
    val linePath = scaled.line
    clipRect {
        if (showsElevationAxis) {
            for (tick in domainTicks(domain)) {
                val y = down(profileY(tick, domain))
                drawLine(grid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }
        }
        if (lit != null) {
            val start = across(profileX(lit.from, series.totalDistance, window))
            val end = across(profileX(lit.to, series.totalDistance, window))
            clipRect(left = start, right = maxOf(start, end)) { drawPath(areaPath, litFill) }
        }
        val stroke = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Round)
        if (stops.isEmpty()) {
            drawPath(areaPath, accent.copy(alpha = accent.alpha * PROFILE_AREA_OPACITY))
            drawPath(linePath, accent, style = stroke)
        } else {
            val shading =
                Brush.horizontalGradient(
                    *stops
                        .map { stop ->
                            stop.offset.toFloat() to
                                (stop.color?.let { hexColor(HexColor(it)) } ?: accent)
                        }.toTypedArray(),
                    startX = 0f,
                    endX = size.width,
                )
            drawPath(areaPath, shading, alpha = PROFILE_AREA_OPACITY)
            drawPath(linePath, shading, style = stroke)
        }
        for (divider in placement.dividers) {
            val y = divider.y ?: continue
            drawLine(
                dropLine,
                Offset(across(divider.x), down(y)),
                Offset(across(divider.x), size.height),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 2.dp.toPx())),
            )
        }
        for (cutoff in placement.cutoffs) {
            drawLine(
                dimmed,
                Offset(across(cutoff.x), CUTOFF_MARK_TOP_DP.dp.toPx()),
                Offset(across(cutoff.x), size.height),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())),
            )
        }
    }
}

/**
 * The layer a profile's cursor is drawn on, over its curve: the dashed rule down the chart at the sample [cursor] answers
 * and the dot on the curve there, nothing where no cursor stands. The cursor is read as the layer is drawn, so a scrub
 * draws this layer again and composes nothing.
 */
@Composable
private fun ProfileCursor(
    series: ElevationSeries,
    domain: ElevationDomain,
    window: ProfileRange,
    cursor: () -> ElevationPoint?,
    scheme: ThemeScheme,
    modifier: Modifier,
) {
    val probe = LocalMountayaRenderProbe.current
    val tokens = LocalMountayaTokens.current
    val accent = tokenColor(tokens.accent, scheme)
    val cursorColor = tokenColor(tokens.profileCursor, scheme)
    val dash = themeTokenValue(tokens.profileCursorDash, scheme)
    Spacer(
        modifier.graphicsLayer().drawBehind {
            val point = cursor() ?: return@drawBehind
            probe?.report("ProfileCursor drawn")
            val x = (profileX(point.distance, series.totalDistance, window) / PROFILE_VIEW_BOX * size.width).toFloat()
            val y = (profileY(point.elevation, domain) / PROFILE_VIEW_BOX * size.height).toFloat()
            clipRect {
                drawLine(
                    cursorColor,
                    Offset(x, 0f),
                    Offset(x, size.height),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect =
                        dash.takeIf { it.isNotEmpty() }?.let { lengths ->
                            PathEffect.dashPathEffect(lengths.map { it.toFloat() * density }.toFloatArray())
                        },
                )
                drawCircle(accent, radius = 4.dp.toPx(), center = Offset(x, y))
                drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(x, y), style = Stroke(1.5.dp.toPx()))
            }
        },
    )
}

/**
 * A pin standing on a profile's curve: a disc in the place's colour, or the profile's own, its glyph in white, pressed
 * where the app hands [onWaypointClick], and read by its place's name.
 */
@Composable
private fun ProfilePin(
    pin: CurvePin,
    scheme: ThemeScheme,
    onWaypointClick: ((String) -> Unit)?,
    modifier: Modifier,
) {
    LocalMountayaRenderProbe.current?.report("ProfilePin ${pin.place.id}")
    val scope = LocalMountayaScope.current
    val tokens = LocalMountayaTokens.current
    val fill = pin.place.color?.let { hexColor(HexColor(it)) } ?: tokenColor(tokens.profileWaypointColor, scheme)
    val glyph = placeGlyph(pin.place.glyph?.value, scope.markerGlyphs)
    val name = pin.place.name ?: scope.word("profile.unlabelledWaypoint")
    val interactions = remember { MutableInteractionSource() }
    val pressed =
        if (onWaypointClick == null) {
            Modifier.clearAndSetSemantics { contentDescription = name }
        } else {
            Modifier
                .keyboardFocusRing(interactions, CircleShape)
                .clickable(
                    interactionSource = interactions,
                    indication = null,
                    role = Role.Button,
                ) { onWaypointClick(pin.place.id) }
                .clearAndSetSemantics {
                    contentDescription = name
                    role = Role.Button
                }
        }
    Box(
        modifier
            .size(PROFILE_PIN_SIDE_DP.dp)
            .background(fill, CircleShape)
            .border(1.5.dp, Color.White, CircleShape)
            .then(pressed),
        contentAlignment = Alignment.Center,
    ) {
        if (glyph != null) Glyph(glyph, set = scope.markerGlyphs, size = 12.dp, color = Color.White)
    }
}

/**
 * AmenityStackBadges is the services a pinned place offers stacked above its pin (`geo.profile.amenity-stacks`): a
 * badge per service shown, its glyph of the marker set, or its name's first letter where the set cannot draw it
 * (`geo.icons.glyph-fallback`), and the count of the rest, read as one label naming every service.
 */
@Composable
internal fun AmenityStackBadges(
    stack: AmenityStack,
    scheme: ThemeScheme,
    modifier: Modifier = Modifier,
) {
    LocalMountayaRenderProbe.current?.report("AmenityStackBadges")
    val scope = LocalMountayaScope.current
    val tokens = LocalMountayaTokens.current
    val ink = tokenColor(tokens.panelFg, scheme)
    val label = stack.named.joinToString(", ") { amenityName(it, scope) }
    Row(
        modifier.clearAndSetSemantics { contentDescription = label },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        for (amenity in stack.shown) {
            StackBadge(scheme) {
                val glyph = amenityGlyph(amenity)
                if (glyph in scope.markerGlyphs.markup) {
                    Glyph(glyph, set = scope.markerGlyphs, size = 10.dp, color = ink)
                } else {
                    BasicText(
                        badgeFallback(amenityName(amenity, scope), BadgeSurface.Stack).orEmpty(),
                        style = TextStyle(color = ink, fontSize = 8.sp, fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        }
        if (stack.counted > 0) {
            StackBadge(scheme) {
                BasicText(
                    "+${stack.counted}",
                    style = TextStyle(color = ink, fontSize = 8.sp, fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

/** One badge of a stack, on the panel's ground. */
@Composable
private fun StackBadge(
    scheme: ThemeScheme,
    content: @Composable () -> Unit,
) {
    val tokens = LocalMountayaTokens.current
    val shape = RoundedCornerShape(3.dp)
    Box(
        Modifier
            .size(14.dp)
            .background(tokenColor(tokens.panelBg, scheme), shape)
            .border(1.dp, tokenColor(tokens.panelBorder, scheme), shape),
        contentAlignment = Alignment.Center,
    ) { content() }
}

/**
 * ProfileReadoutCard is the card at a profile's cursor, stating what it stands on (`geo.profile.cursor-readout`): its
 * figures, its grade, and the stretch it stands in, named beside its colour, on the tooltip's ground.
 */
@Composable
internal fun ProfileReadoutCard(
    readout: ProfileReadout,
    units: UnitPreferences,
    scheme: ThemeScheme,
    modifier: Modifier = Modifier,
) {
    LocalMountayaRenderProbe.current?.report("ProfileReadoutCard")
    val scope = LocalMountayaScope.current
    val tokens = LocalMountayaTokens.current
    val ink = tokenColor(tokens.profileTooltipFg, scheme)
    val shadow =
        themeTokenValue(tokens.profileTooltipShadow, scheme).firstOrNull()?.let(::hexColor) ?: Color.Transparent
    val shape = RoundedCornerShape(6.dp)
    val label = readoutText(readout, units, scope)
    Column(
        modifier
            .shadow(4.dp, shape, ambientColor = shadow, spotColor = shadow)
            .background(tokenColor(tokens.profileTooltipBg, scheme), shape)
            .padding(8.dp)
            .clearAndSetSemantics { contentDescription = label },
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        val stretch = readout.stretch
        if (stretch != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(Modifier.size(8.dp).background(hexColor(HexColor(stretch.color)), CircleShape))
                BasicText(
                    stretch.label,
                    style = TextStyle(color = ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                )
            }
        }
        for (cell in readout.cells) ReadoutLine(scope.word(readoutWord(cell.stat)), readoutValue(cell, units), ink)
        readout.grade?.let { ReadoutLine(scope.word("profile.grade"), formatSlope(it, units), ink) }
    }
}

/** One labelled figure of a readout card. */
@Composable
private fun ReadoutLine(
    word: String,
    value: String,
    ink: Color,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        BasicText(word, style = TextStyle(color = ink.copy(alpha = ink.alpha * 0.7f), fontSize = 12.sp))
        BasicText(value, style = TextStyle(color = ink, fontSize = 12.sp))
    }
}

/** The path of the word a readout's figure is labelled with. */
internal fun readoutWord(stat: ReadoutStat): String = "profile.${stat.id}"

/** A readout's figure as the reader's units write it. */
internal fun readoutValue(
    cell: ReadoutCell,
    units: UnitPreferences,
): String =
    when (cell.stat) {
        ReadoutStat.Distance -> formatDistance(cell.value, units)
        ReadoutStat.Elevation, ReadoutStat.Ascent, ReadoutStat.Descent -> formatElevation(cell.value, units)
    }

/** What a readout card states, as accessibility services read it: the stretch, each figure and the grade. */
internal fun readoutText(
    readout: ProfileReadout,
    units: UnitPreferences,
    scope: ElementsScope,
): String =
    buildList {
        readout.stretch?.let { add(it.label) }
        for (cell in readout.cells) add(scope.word(readoutWord(cell.stat)) + " " + readoutValue(cell, units))
        readout.grade?.let { add(scope.word("profile.grade") + " " + formatSlope(it, units)) }
    }.joinToString(", ")

/** profileSampleAt is the sample the column under [x] of a chart [width] pixels wide resolves to, through [grid]. */
internal fun profileSampleAt(
    x: Float,
    width: Float,
    grid: List<Int?>,
): Int? {
    if (!(width > 0f) || grid.isEmpty()) return null
    val column = floor(x / width * grid.size).toInt()
    return grid[column.coerceIn(0, grid.size - 1)]
}

/** What the cursor stands on, as accessibility services read it: its distance and its altitude. */
private fun cursorText(
    cursor: ElevationPoint?,
    units: UnitPreferences,
): String =
    if (cursor ==
        null
    ) {
        ""
    } else {
        listOf(formatDistance(cursor.distance, units), formatElevation(cursor.elevation, units)).joinToString(", ")
    }

/**
 * The plot's place as an adjustable value: the cursor's sample, else the window's first, and the window's first and
 * last samples, null where the window holds none.
 */
private fun plotProgress(
    series: ElevationSeries,
    window: ProfileRange,
    cursor: ElevationPoint?,
): Triple<Int, Int, Int>? {
    val inside = series.points.filter { it.distance >= window.from && it.distance <= window.to }
    val first = inside.firstOrNull()?.index ?: return null
    val last = inside.last().index
    return Triple((cursor?.index ?: first).coerceIn(first, last), first, last)
}

/** Stands what it modifies with its middle at ([x], [y]), in pixels from its parent's top left. */
internal fun Modifier.centredAt(
    x: Float,
    y: Float,
): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
        layout(placeable.width, placeable.height) {
            placeable.place((x - placeable.width / 2f).roundToInt(), (y - placeable.height / 2f).roundToInt())
        }
    }
