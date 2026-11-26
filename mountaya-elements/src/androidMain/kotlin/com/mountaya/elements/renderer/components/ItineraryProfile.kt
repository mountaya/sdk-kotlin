package com.mountaya.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.CurveColor
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.ElevationPoint
import com.mountaya.geo.ElevationSeries
import com.mountaya.geo.HexColor
import com.mountaya.geo.ItineraryLeg
import com.mountaya.geo.ItineraryResource
import com.mountaya.geo.ItineraryResourceCache
import com.mountaya.geo.ItineraryResourceEngine
import com.mountaya.geo.ItinerarySegmentRow
import com.mountaya.geo.PROFILE_VIEW_BOX
import com.mountaya.geo.PlacedSpan
import com.mountaya.geo.ProfileAnchor
import com.mountaya.geo.ProfileClass
import com.mountaya.geo.ProfileRange
import com.mountaya.geo.SectionChipStat
import com.mountaya.geo.StatStripCell
import com.mountaya.geo.StatStripCursor
import com.mountaya.geo.StatStripStat
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.amenityStacks
import com.mountaya.geo.anchoredDomain
import com.mountaya.geo.clampRange
import com.mountaya.geo.defaultStatStripStats
import com.mountaya.geo.formatDistance
import com.mountaya.geo.formatDuration
import com.mountaya.geo.formatElevation
import com.mountaya.geo.formatSlope
import com.mountaya.geo.gradeAt
import com.mountaya.geo.placeOnCurve
import com.mountaya.geo.placeSpans
import com.mountaya.geo.profileDomain
import com.mountaya.geo.profileReadout
import com.mountaya.geo.rowStandsFor
import com.mountaya.geo.sectionChipCells
import com.mountaya.geo.slopeStretches
import com.mountaya.geo.statStripCells
import kotlin.time.Instant

/** The lowest a profile's chart is drawn, in density-independent pixels. */
private const val PROFILE_CHART_MIN_HEIGHT_DP = 100

/** How opaque a section chip's ground is, in its leg's colour. */
private const val SECTION_CHIP_OPACITY = 0.14f

/** The height of the strip of section chips at a font scale of 1, in density-independent pixels. */
internal const val SECTION_CHIPS_HEIGHT_DP: Int = 36

/** The size a section chip writes its two lines in. */
private val SECTION_CHIP_TEXT = 11.sp

/**
 * ItineraryProfile is an itinerary's elevation profile: the stat strip over it, the curve and what stands on it, and,
 * as the app asks, the elevation scale, the distance scale with the places' names, a band strip, a chip per leg and a
 * navigator over the window drawn.
 *
 * ```
 * ItineraryProfile(itineraryId = "itin_abc", cache = itineraries, modifier = Modifier.height(160.dp), map = map)
 * ```
 *
 * It reads the itinerary [itineraryId] names on [surface] through [cache], one read for every element asking about
 * it (`geo.data.itinerary-resource`), and draws its geometry, the server's figures over the ones it reads off the
 * geometry, its legs and its places. A chain the app states, [legs], is also the curve (`sdk/SPEC.md` section 7). An
 * itinerary with no altitude draws no plot and states its empty line, its strip still stating its length. A failure
 * to read is handed to [onError], and draws the empty state. An app that holds an `ItineraryResourceEngine` of its own
 * hands it to the overload that takes one, in place of the id, the cache and the surface.
 *
 * Every figure is the profile's rules', drawn in a 100-unit square stretched over the chart (`geo.profile.*`): the
 * scale anchored on [anchor], the curve painted as [coloring] asks, the places pinned on the curve with their services
 * above them and their names under the axis, and the cut-offs marked where [formatCutoffAt] writes them. A scrub reads
 * the window through 240 columns, the cursor standing on a sample; the strip then states the cursor's distance,
 * altitude and grade, unless the card at the cursor, [ProfilePart.Tooltip], states them.
 *
 * The window is the app's where it hands [window], a null value being the whole itinerary, and the profile's own
 * otherwise, which the navigator moves and a new itinerary resets. While a scrub stands over a profile with a [map], a
 * marker stands on the map at the sample, under [markerId] in [markerColor] (`map.profile.cursor-marker`), and the leg
 * under the cursor, the later of two at the meter they share, is lit on the map's emphasis slot in [emphasisColor]
 * (`map.profile.emphasis`); both are taken down as the scrub ends or the profile leaves. An [onHover] handler takes
 * the map links over: it is handed the sample and the profile writes nothing to the map. A profile not [enabled]
 * draws and drives nothing. [highlighted] lights the leg a segment id names, its own or a member's
 * (`geo.profile.row-match`), tinted under the curve; [onWaypointClick] is handed a pin's place's id, and
 * [onSegmentClick] the id a leg's chip stands for as `geo.profile.row-match` names it, a collapsed group's
 * `group:<id>`. A chip is a button only where the app listens, presses nothing while the profile is not [enabled], and
 * a press writes nothing to the map: what it lights, or where it moves the camera, is the app's.
 *
 * Its words are the scope's (`MountayaScope`), its `profile` and `amenities` sections, each one its own [strings] name
 * standing over the scope's, its figures written in [units], the scope's locale in metric unless the app hands others,
 * and its colours the tokens' (`LocalMountayaTokens`), read under the colour scheme in force.
 *
 * The cursor is held where the parts it moves read it, the dot on the curve, the card or the strip stating it, the plot's
 * reading and the marker on the map, and never in the profile itself, so a scrub draws those parts again and never the
 * whole profile for every pointer position (`scrub-root` in the contract's `sdk/hot-paths.yaml`).
 *
 * @param itineraryId The itinerary drawn, or null for none.
 * @param cache The cache the itinerary is read through, one per client.
 * @param modifier The host's modifier, as every composable takes one; the chart takes the height the strip and the
 * parts leave.
 * @param surface The surface the itinerary is read on, null for the one the credential reads by default.
 * @param legs A chain the app states, drawn in place of the itinerary's own.
 * @param map The map the cursor's links are written to, or null for none.
 * @param units The reader's units, the scope's locale in metric where null.
 * @param stats The figures the strip states, in order.
 * @param coloring How the curve is painted.
 * @param bands Stretches drawn as a strip under the chart.
 * @param parts The parts drawn beside the curve.
 * @param anchor What the elevation scale is anchored on.
 * @param window The window the app holds, or null for the profile's own.
 * @param highlighted A segment id whose leg is lit under the curve.
 * @param emphasisColor The colour the map's emphasis is drawn in, the slot's own where null.
 * @param markerId The id the cursor's marker is pinned under on the map.
 * @param markerColor The colour of the cursor's marker, the map's accent where null.
 * @param formatCutoffAt How a cut-off is written; cut-offs are drawn only where it is given.
 * @param enabled Whether the profile drives anything.
 * @param strings The words it states for itself, by path, `profile.title`, each standing over the scope's for it and
 * everything it draws, or null for the scope's own.
 * @param onHover Handed the sample the cursor stands on, or null as it leaves, in place of the map's links.
 * @param onWaypointClick Handed the id of a pinned place pressed.
 * @param onSegmentClick Handed the id of a leg whose chip is pressed.
 * @param onError Handed a failure to read the itinerary.
 */
@Composable
public fun ItineraryProfile(
    itineraryId: String?,
    cache: ItineraryResourceCache,
    modifier: Modifier = Modifier,
    surface: RestSchema.ReadSurface? = null,
    legs: List<ItineraryLeg>? = null,
    map: MapEngine? = null,
    units: UnitPreferences? = null,
    stats: List<StatStripStat> = defaultStatStripStats,
    coloring: ProfileColoring = ProfileColoring.Plain,
    bands: List<ProfileClass> = emptyList(),
    parts: Set<ProfilePart> = ProfilePart.standard,
    anchor: ProfileAnchor = ProfileAnchor.Route,
    window: MutableState<ProfileRange?>? = null,
    highlighted: String? = null,
    emphasisColor: String? = null,
    markerId: String = PROFILE_CURSOR_MARKER_ID,
    markerColor: String? = null,
    formatCutoffAt: ((Instant) -> String)? = null,
    enabled: Boolean = true,
    strings: Map<String, String>? = null,
    onHover: ((ElevationPoint?) -> Unit)? = null,
    onWaypointClick: ((String) -> Unit)? = null,
    onSegmentClick: ((String) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null,
) {
    // Made idle and asked at each composition, so a profile Compose makes and drops asks nothing.
    val resource = remember { ItineraryResourceEngine(cache) }
    DisposableEffect(resource) { onDispose { resource.close() } }
    SideEffect { resource.configure(cache, itineraryId, surface) }
    ProvideStrings(strings) {
        ItineraryProfileOver(
            resource,
            listOf(cache, itineraryId, surface),
            modifier,
            legs,
            map,
            units,
            stats,
            coloring,
            bands,
            parts,
            anchor,
            window,
            highlighted,
            emphasisColor,
            markerId,
            markerColor,
            formatCutoffAt,
            enabled,
            onHover,
            onWaypointClick,
            onSegmentClick,
            onError,
        )
    }
}

/**
 * ItineraryProfile is the elevation profile of the itinerary the app's [engine] reads, drawn as the profile over a cache
 * draws one: the element reads the engine's `state` and never configures or closes it, the app choosing the itinerary
 * through `configure`, and starts over, its window and its cursor, as it is handed another engine. The compiler holds
 * that an element reads through an engine or a cache, never neither.
 *
 * ```
 * ItineraryProfile(engine = profile, modifier = Modifier.height(160.dp), map = map)
 * ```
 *
 * @param engine The app's engine the itinerary is read through, which the profile never closes.
 * @param modifier The host's modifier, as every composable takes one; the chart takes the height the strip and the
 * parts leave.
 * @param legs A chain the app states, drawn in place of the itinerary's own.
 * @param map The map the cursor's links are written to, or null for none.
 * @param units The reader's units, the scope's locale in metric where null.
 * @param stats The figures the strip states, in order.
 * @param coloring How the curve is painted.
 * @param bands Stretches drawn as a strip under the chart.
 * @param parts The parts drawn beside the curve.
 * @param anchor What the elevation scale is anchored on.
 * @param window The window the app holds, or null for the profile's own.
 * @param highlighted A segment id whose leg is lit under the curve.
 * @param emphasisColor The colour the map's emphasis is drawn in, the slot's own where null.
 * @param markerId The id the cursor's marker is pinned under on the map.
 * @param markerColor The colour of the cursor's marker, the map's accent where null.
 * @param formatCutoffAt How a cut-off is written; cut-offs are drawn only where it is given.
 * @param enabled Whether the profile drives anything.
 * @param strings The words it states for itself, by path, `profile.title`, each standing over the scope's for it and
 * everything it draws, or null for the scope's own.
 * @param onHover Handed the sample the cursor stands on, or null as it leaves, in place of the map's links.
 * @param onWaypointClick Handed the id of a pinned place pressed.
 * @param onSegmentClick Handed the id of a leg whose chip is pressed.
 * @param onError Handed a failure to read the itinerary.
 */
@Composable
public fun ItineraryProfile(
    engine: ItineraryResourceEngine,
    modifier: Modifier = Modifier,
    legs: List<ItineraryLeg>? = null,
    map: MapEngine? = null,
    units: UnitPreferences? = null,
    stats: List<StatStripStat> = defaultStatStripStats,
    coloring: ProfileColoring = ProfileColoring.Plain,
    bands: List<ProfileClass> = emptyList(),
    parts: Set<ProfilePart> = ProfilePart.standard,
    anchor: ProfileAnchor = ProfileAnchor.Route,
    window: MutableState<ProfileRange?>? = null,
    highlighted: String? = null,
    emphasisColor: String? = null,
    markerId: String = PROFILE_CURSOR_MARKER_ID,
    markerColor: String? = null,
    formatCutoffAt: ((Instant) -> String)? = null,
    enabled: Boolean = true,
    strings: Map<String, String>? = null,
    onHover: ((ElevationPoint?) -> Unit)? = null,
    onWaypointClick: ((String) -> Unit)? = null,
    onSegmentClick: ((String) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null,
) {
    ProvideStrings(strings) {
        ItineraryProfileOver(
            engine,
            engine,
            modifier,
            legs,
            map,
            units,
            stats,
            coloring,
            bands,
            parts,
            anchor,
            window,
            highlighted,
            emphasisColor,
            markerId,
            markerColor,
            formatCutoffAt,
            enabled,
            onHover,
            onWaypointClick,
            onSegmentClick,
            onError,
        )
    }
}

/**
 * The profile of the itinerary [resource] reads, which its host makes or the app hands, starting over, its window and its
 * cursor, as [question] changes: the cache, the id and the surface a profile over a cache asks, or the app's engine.
 */
@Composable
private fun ItineraryProfileOver(
    resource: ItineraryResourceEngine,
    question: Any,
    modifier: Modifier,
    legs: List<ItineraryLeg>?,
    map: MapEngine?,
    units: UnitPreferences?,
    stats: List<StatStripStat>,
    coloring: ProfileColoring,
    bands: List<ProfileClass>,
    parts: Set<ProfilePart>,
    anchor: ProfileAnchor,
    window: MutableState<ProfileRange?>?,
    highlighted: String?,
    emphasisColor: String?,
    markerId: String,
    markerColor: String?,
    formatCutoffAt: ((Instant) -> String)?,
    enabled: Boolean,
    onHover: ((ElevationPoint?) -> Unit)?,
    onWaypointClick: ((String) -> Unit)?,
    onSegmentClick: ((String) -> Unit)?,
    onError: ((Throwable) -> Unit)?,
) {
    val probe = LocalMountayaRenderProbe.current
    probe?.report("ItineraryProfile")
    val scope = LocalMountayaScope.current
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val state by resource.state.collectAsState()
    val link = remember(markerId) { MapLink<Int>(markerId) }
    val ownWindow = remember { mutableStateOf<ProfileRange?>(null) }
    val drawing = remember(state, legs) { ItineraryDrawing((state as? ItineraryResource.Resolved)?.itinerary, legs) }
    // The sample the cursor stands on, answered to each part that reads it as that part is composed or drawn, never here.
    val cursorAt = remember(link, drawing) { { link.pointed?.let { drawing.series.points.getOrNull(it) } } }
    val figures = units ?: UnitPreferences(scope.locale)
    val currentOnError by rememberUpdatedState(onError)
    val scrub by rememberUpdatedState { index: Int? ->
        val point = index?.let { drawing.series.points.getOrNull(it) }
        link.point(
            point?.index,
            point,
            drawing.cursorTarget(point?.index),
            enabled,
            onHover,
            map,
            markerColor,
            emphasisColor,
        )
    }
    // The state as this composition reads it: an effect reading the state later would see a failure the next
    // composition reports too.
    val read = state
    LaunchedEffect(read) { (read as? ItineraryResource.Failed)?.let { currentOnError?.invoke(it.cause) } }
    LaunchedEffect(question) {
        // A new itinerary starts over: its window, and its cursor.
        ownWindow.value = null
        scrub(null)
    }
    LaunchedEffect(enabled) { if (!enabled) scrub(null) }
    DisposableEffect(Unit) { onDispose { scrub(null) } }

    // What the chart draws is read once for what it depends on, so a scrub that moves the cursor alone hands it the
    // same values and its curve is neither composed nor drawn again.
    val asked = window?.value ?: ownWindow.value ?: drawing.whole
    val shown = remember(asked, drawing) { clampRange(asked, drawing.series.totalDistance) }
    BoxWithConstraints(modifier.semantics { contentDescription = scope.word("profile.title") }) {
        probe?.report("ItineraryProfile content")
        // The chart takes the height the strip and the parts leave where the profile is given one, and its least
        // height where it is not, in a scrolling column.
        val bounded = constraints.hasBoundedHeight
        Column(
            if (bounded) Modifier.fillMaxWidth().fillMaxHeight() else Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (stats.isNotEmpty()) {
                // Where the card states the cursor, the strip states the itinerary alone and never reads it.
                ProfileCursorStrip(drawing, stats, figures, if (ProfilePart.Tooltip in parts) null else cursorAt)
            }
            if (drawing.series.hasElevation) {
                val series = drawing.series
                val whole = remember(drawing) { profileDomain(series) }
                val domain =
                    remember(drawing, anchor, shown) {
                        if (anchor == ProfileAnchor.Route ||
                            (anchor == ProfileAnchor.Window && shown == drawing.whole)
                        ) {
                            whole
                        } else {
                            anchoredDomain(series, anchor, shown)
                        }
                    }
                val placement =
                    remember(
                        drawing,
                        domain,
                        shown,
                    ) { placeOnCurve(series, drawing.geometry, domain, shown, drawing.places) }
                val classes = remember(coloring, series, scope) { profileClasses(coloring, series, scope) }
                val painted = remember(coloring, classes) { paintedStretches(coloring, classes) }
                val litRow = highlighted?.let { id -> drawing.rows.firstOrNull { rowStandsFor(it, id) } }
                val lit = remember(litRow) { litRow?.let { ProfileRange(it.from, it.to) } }
                val stacked = ProfilePart.Amenities in parts
                val stacks =
                    remember(placement, stacked) { if (stacked) amenityStacks(placement.pins) else emptyList() }
                val readout =
                    remember(series, classes) {
                        { point: ElevationPoint ->
                            profileReadout(series, point, stretches = classes)
                        }
                    }
                ProfileChart(
                    series = series,
                    domain = domain,
                    window = shown,
                    painted = painted,
                    placement = placement,
                    stacks = stacks,
                    lit = lit,
                    cursor = cursorAt,
                    readout = if (ProfilePart.Tooltip in parts) readout else null,
                    showsElevationAxis = ProfilePart.ElevationAxis in parts,
                    units = figures,
                    formatCutoffAt = formatCutoffAt,
                    enabled = enabled,
                    onScrub = { scrub(it) },
                    onWaypointClick = onWaypointClick,
                    modifier =
                        if (bounded) {
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .heightIn(min = PROFILE_CHART_MIN_HEIGHT_DP.dp)
                        } else {
                            Modifier.fillMaxWidth().height(PROFILE_CHART_MIN_HEIGHT_DP.dp)
                        },
                )
                if (ProfilePart.DistanceAxis in parts) {
                    ProfileDistanceAxis(series.totalDistance, shown, placement.dividers, figures)
                }
                if (bands.isNotEmpty()) {
                    ProfileStretchStrip(placeSpans(bands, series.totalDistance, shown), scope.word("profile.bands"))
                }
                if (ProfilePart.Sections in parts) {
                    ProfileSections(
                        placeSpans(drawing.rows, series.totalDistance, shown),
                        figures,
                        enabled = enabled,
                        onSegmentClick = onSegmentClick,
                    )
                }
                if (ProfilePart.Navigator in parts) {
                    ProfileNavigator(series, whole, shown, enabled) { moved ->
                        if (window != null) window.value = moved else ownWindow.value = moved
                    }
                }
            } else {
                BasicText(
                    scope.word("profile.empty"),
                    style = TextStyle(color = tokenColor(tokens.dimmed, scheme), fontSize = 13.sp),
                )
            }
        }
    }
}

/** The stretches the curve is classified into by [coloring], which the readout names, each slope bucket by the scope's word. */
private fun profileClasses(
    coloring: ProfileColoring,
    series: ElevationSeries,
    scope: ElementsScope,
): List<ProfileClass> =
    when (coloring) {
        ProfileColoring.Plain, is ProfileColoring.Stretches -> {
            emptyList()
        }

        is ProfileColoring.Classes -> {
            coloring.classes
        }

        is ProfileColoring.Slope -> {
            slopeStretches(series.points, coloring.buckets).map {
                ProfileClass(it.from, it.to, it.color, scope.word("profile.slope.${it.label}"))
            }
        }
    }

/** The stretches the curve is painted in by [coloring]. */
private fun paintedStretches(
    coloring: ProfileColoring,
    classes: List<ProfileClass>,
): List<CurveColor> =
    if (coloring is ProfileColoring.Stretches) {
        coloring.stretches
    } else {
        classes.map {
            CurveColor(
                it.from,
                it.to,
                it.color,
            )
        }
    }

/**
 * The stat strip over a profile of [drawing], stating [stats] in [units]: the cursor's distance, altitude and grade where
 * [cursorAt] is handed and answers a sample, and the itinerary's figures otherwise. It reads the cursor itself, so a scrub
 * draws the strip again and never the profile around it.
 */
@Composable
private fun ProfileCursorStrip(
    drawing: ItineraryDrawing,
    stats: List<StatStripStat>,
    units: UnitPreferences,
    cursorAt: (() -> ElevationPoint?)?,
) {
    val stands =
        cursorAt?.invoke()?.let {
            StatStripCursor(
                it.distance,
                it.elevation,
                gradeAt(drawing.series.points, it.distance),
            )
        }
    ProfileStatStrip(statStripCells(drawing.characteristics, stands, stats), units)
}

/**
 * ProfileStatStrip is the row of figures over a profile (`geo.profile.stat-strip`): each its word over its value,
 * written in the reader's [units], a difficulty as the itinerary writes it.
 */
@Composable
internal fun ProfileStatStrip(
    cells: List<StatStripCell>,
    units: UnitPreferences,
    modifier: Modifier = Modifier,
) {
    LocalMountayaRenderProbe.current?.report("ProfileStatStrip")
    val scope = LocalMountayaScope.current
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val dimmed = tokenColor(tokens.dimmed, scheme)
    val ink = tokenColor(tokens.panelFg, scheme)
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        for (cell in cells) {
            val word = scope.word("profile.${cell.stat.id}")
            val value = statStripValue(cell, units)
            Column(
                Modifier.clearAndSetSemantics { contentDescription = "$word $value" },
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                BasicText(word, style = TextStyle(color = dimmed, fontSize = 11.sp))
                BasicText(value, style = TextStyle(color = ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
            }
        }
    }
}

/** A stat strip cell's figure as the reader's [units] write it. */
internal fun statStripValue(
    cell: StatStripCell,
    units: UnitPreferences,
): String =
    when (cell) {
        is StatStripCell.Distance -> formatDistance(cell.number, units)
        is StatStripCell.Duration -> formatDuration(cell.number, units)
        is StatStripCell.Ascent -> formatElevation(cell.number, units)
        is StatStripCell.Descent -> formatElevation(cell.number, units)
        is StatStripCell.Elevation -> formatElevation(cell.number, units)
        is StatStripCell.Grade -> formatSlope(cell.number, units)
        is StatStripCell.Difficulty -> cell.difficulty.value
    }

/**
 * ProfileStretchStrip is the strip of coloured stretches under a profile's chart, each placed where it overlaps the
 * window (`geo.profile.window-spans`), painted in its colour and read by its word, the strip read by [label].
 */
@Composable
internal fun ProfileStretchStrip(
    spans: List<PlacedSpan<ProfileClass>>,
    label: String,
    modifier: Modifier = Modifier,
) {
    LocalMountayaRenderProbe.current?.report("ProfileStretchStrip")
    val scope = LocalMountayaScope.current
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(8.dp)
            .semantics { contentDescription = label },
    ) {
        for (placed in spans) {
            val word = placed.span.label.ifEmpty { scope.word("profile.unlabelledBand") }
            Box(
                Modifier
                    .offset(x = maxWidth * (placed.start / PROFILE_VIEW_BOX).toFloat())
                    .width(maxWidth * ((placed.end - placed.start) / PROFILE_VIEW_BOX).toFloat())
                    .fillMaxHeight()
                    .background(hexColor(HexColor(placed.span.color)))
                    .clearAndSetSemantics { contentDescription = word },
            )
        }
    }
}

/**
 * ProfileSections is a chip per leg under a profile's chart, each as wide as its leg's stretch of the window
 * (`geo.profile.window-spans`) and stating its length and climb (`geo.profile.section-chips`). The strip is 36
 * density-independent pixels high, taller as the reader's font scale draws its chips' two lines larger, so a chip
 * never clips its words. Where [onSegmentClick] listens, each chip is a button handing over its leg's id
 * (`geo.profile.row-match`), pressing nothing while the strip is not [enabled].
 */
@Composable
internal fun ProfileSections(
    rows: List<PlacedSpan<ItinerarySegmentRow>>,
    units: UnitPreferences,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSegmentClick: ((String) -> Unit)? = null,
) {
    LocalMountayaRenderProbe.current?.report("ProfileSections")
    val scope = LocalMountayaScope.current
    val density = LocalDensity.current
    val grown = textGrowth(density, SECTION_CHIP_TEXT)
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height((SECTION_CHIPS_HEIGHT_DP * grown).dp)
            .semantics { contentDescription = scope.word("profile.sections") },
    ) {
        for (placed in rows) {
            val start = maxWidth * (placed.start / PROFILE_VIEW_BOX).toFloat()
            val span = maxWidth * ((placed.end - placed.start) / PROFILE_VIEW_BOX).toFloat()
            SectionChip(
                placed.span,
                units,
                Modifier
                    .offset(x = start + 1.dp)
                    .width(with(density) { (span - 2.dp).coerceAtLeast(0.dp) })
                    .fillMaxHeight(),
                enabled,
                onSegmentClick,
            )
        }
    }
}

/**
 * One leg's chip: its colour, its name and its figures; a button handing [onClick] the leg's id where it is given, ringed
 * in `focus-ring` while it holds the keyboard's focus, pressing nothing while not [enabled].
 */
@Composable
private fun SectionChip(
    row: ItinerarySegmentRow,
    units: UnitPreferences,
    modifier: Modifier,
    enabled: Boolean,
    onClick: ((String) -> Unit)?,
) {
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val ink = tokenColor(tokens.panelFg, scheme)
    val ground = row.color?.let { hexColor(HexColor(it)) } ?: tokenColor(tokens.accent, scheme)
    val figures = sectionChipText(row, units)
    val shape = RoundedCornerShape(4.dp)
    val interactions = remember { MutableInteractionSource() }
    val pressed =
        if (onClick == null) {
            Modifier
        } else {
            Modifier
                .keyboardFocusRing(interactions, shape)
                .clickable(
                    interactionSource = interactions,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                ) { onClick(row.id) }
        }
    Column(
        modifier
            .then(pressed)
            .background(ground.copy(alpha = ground.alpha * SECTION_CHIP_OPACITY), shape)
            .padding(horizontal = 4.dp)
            .clearAndSetSemantics {
                contentDescription = row.name + ", " + figures
                if (onClick != null) role = Role.Button
            },
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        BasicText(
            row.name,
            style = TextStyle(color = ink, fontSize = SECTION_CHIP_TEXT, fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
        BasicText(
            figures,
            style = TextStyle(color = ink, fontSize = SECTION_CHIP_TEXT),
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
    }
}

/** What a leg's chip states, its figures in the reader's [units] parted by a middle dot. */
internal fun sectionChipText(
    row: ItinerarySegmentRow,
    units: UnitPreferences,
): String =
    sectionChipCells(row.metrics).joinToString(" · ") { cell ->
        when (cell.stat) {
            SectionChipStat.Distance -> formatDistance(cell.value, units)
            SectionChipStat.Ascent -> "+" + formatElevation(cell.value, units)
            SectionChipStat.Descent -> "-" + formatElevation(cell.value, units)
            SectionChipStat.Grade -> formatSlope(cell.value, units)
        }
    }
