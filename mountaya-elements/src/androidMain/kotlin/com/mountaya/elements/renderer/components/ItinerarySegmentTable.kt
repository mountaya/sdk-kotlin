package com.mountaya.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.ElevationSeries
import com.mountaya.geo.ItineraryLeg
import com.mountaya.geo.ItineraryResource
import com.mountaya.geo.ItineraryResourceCache
import com.mountaya.geo.ItineraryResourceEngine
import com.mountaya.geo.ItinerarySegmentRow
import com.mountaya.geo.SegmentRowCell
import com.mountaya.geo.SegmentTableColumn
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.defaultSegmentTableColumns
import com.mountaya.geo.formatAltitudeRange
import com.mountaya.geo.formatDistance
import com.mountaya.geo.formatDuration
import com.mountaya.geo.formatElevation
import com.mountaya.geo.segmentRowCell

/** The least height of a table's row, the platform's touch target, in density-independent pixels. */
internal const val TABLE_ROW_MIN_HEIGHT_DP: Int = 48

/** The height a sparkline is drawn in, in a table's cell, in density-independent pixels. */
internal const val TABLE_SPARKLINE_HEIGHT_DP: Int = 24

/** How opaque a cell's running figure is, under its own. */
private const val RUNNING_FIGURE_OPACITY = 0.6f

/**
 * ItinerarySegmentTable is an itinerary's legs as a roadbook: a row per leg in chain order, each stating, column by
 * column, its name, the whole itinerary's sparkline with its own leg lit, its length over the running distance, its
 * climb and loss over the running climb, and, as the app asks, its altitude band, its duration and its difficulty
 * (`geo.profile.segment-rows`, `geo.profile.segment-table-cells`).
 *
 * ```
 * ItinerarySegmentTable(itineraryId = "itin_abc", cache = itineraries, map = map)
 * ```
 *
 * It reads the itinerary as `ItineraryProfile` does, through [cache], a chain the app states, [legs], standing in for
 * the itinerary's own and being its curve. A row a reader points at, a hovering pointer over it or a press on it, is
 * lit on [map]'s emphasis slot as its stretch of the series (`map.profile.emphasis`), with a marker at its end under
 * [markerId] (`map.profile.cursor-marker`), both taken down as the pointer leaves or the table goes; [onHover] takes
 * that over, handed the row or null. The leg [map]'s own cursor is over lights its row, a collapsed group's row for
 * each of its members, and [highlighted] lights the row a segment id names, its own or a member's
 * (`geo.profile.row-match`), in place of the one the map's cursor would light (`map.profile.table-rows`); a press
 * hands the row's id to [onSegmentClick]. A table not [enabled] draws and drives nothing. Each row is the platform's
 * touch target high.
 *
 * It reads through the app's own `ItineraryResourceEngine` where the app hands it to the overload that takes one, in
 * place of the id, the cache and the surface, never configuring or closing it.
 *
 * Its words are the scope's `table` section, each one its own [strings] name standing over the scope's, its figures
 * written in [units], and its colours the tokens'.
 *
 * @param itineraryId The itinerary listed, or null for none.
 * @param cache The cache the itinerary is read through, one per client.
 * @param modifier The host's modifier, as every composable takes one.
 * @param surface The surface the itinerary is read on, null for the one the credential reads by default.
 * @param legs A chain the app states, listed in place of the itinerary's own.
 * @param map The map a row pointed at is lit on, or null for none.
 * @param units The reader's units, the scope's locale in metric where null.
 * @param columns The columns drawn, in order.
 * @param highlighted A segment id whose row is lit, in place of the row of the leg the map's cursor is over.
 * @param emphasisColor The colour the map's emphasis is drawn in, the slot's own where null.
 * @param markerId The id the row's marker is pinned under on the map.
 * @param markerColor The colour of the row's marker, the map's accent where null.
 * @param enabled Whether the table drives anything.
 * @param strings The words it states for itself, by path, `table.title`, each standing over the scope's for it and
 * everything it draws, or null for the scope's own.
 * @param onHover Handed the row pointed at, or null as it is let go, in place of the map's links.
 * @param onSegmentClick Handed the id of a row pressed.
 * @param onError Handed a failure to read the itinerary.
 */
@Composable
public fun ItinerarySegmentTable(
    itineraryId: String?,
    cache: ItineraryResourceCache,
    modifier: Modifier = Modifier,
    surface: RestSchema.ReadSurface? = null,
    legs: List<ItineraryLeg>? = null,
    map: MapEngine? = null,
    units: UnitPreferences? = null,
    columns: List<SegmentTableColumn> = defaultSegmentTableColumns,
    highlighted: String? = null,
    emphasisColor: String? = null,
    markerId: String = SEGMENT_TABLE_CURSOR_MARKER_ID,
    markerColor: String? = null,
    enabled: Boolean = true,
    strings: Map<String, String>? = null,
    onHover: ((ItinerarySegmentRow?) -> Unit)? = null,
    onSegmentClick: ((String) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null,
) {
    val resource = remember { ItineraryResourceEngine(cache) }
    DisposableEffect(resource) { onDispose { resource.close() } }
    SideEffect { resource.configure(cache, itineraryId, surface) }
    ProvideStrings(strings) {
        ItinerarySegmentTableOver(
            resource,
            modifier,
            legs,
            map,
            units,
            columns,
            highlighted,
            emphasisColor,
            markerId,
            markerColor,
            enabled,
            onHover,
            onSegmentClick,
            onError,
        )
    }
}

/**
 * ItinerarySegmentTable is the roadbook of the itinerary the app's [engine] reads, drawn as the table over a cache draws
 * one: it reads the engine's `state` and never configures or closes it, the app choosing the itinerary through
 * `configure`. The compiler holds that a table reads through an engine or a cache, never neither.
 *
 * @param engine The app's engine the itinerary is read through, which the table never closes.
 * @param modifier The host's modifier, as every composable takes one.
 * @param legs A chain the app states, listed in place of the itinerary's own.
 * @param map The map a row pointed at is lit on, or null for none.
 * @param units The reader's units, the scope's locale in metric where null.
 * @param columns The columns drawn, in order.
 * @param highlighted A segment id whose row is lit, in place of the row of the leg the map's cursor is over.
 * @param emphasisColor The colour the map's emphasis is drawn in, the slot's own where null.
 * @param markerId The id the row's marker is pinned under on the map.
 * @param markerColor The colour of the row's marker, the map's accent where null.
 * @param enabled Whether the table drives anything.
 * @param strings The words it states for itself, by path, `table.title`, each standing over the scope's for it and
 * everything it draws, or null for the scope's own.
 * @param onHover Handed the row pointed at, or null as it is let go, in place of the map's links.
 * @param onSegmentClick Handed the id of a row pressed.
 * @param onError Handed a failure to read the itinerary.
 */
@Composable
public fun ItinerarySegmentTable(
    engine: ItineraryResourceEngine,
    modifier: Modifier = Modifier,
    legs: List<ItineraryLeg>? = null,
    map: MapEngine? = null,
    units: UnitPreferences? = null,
    columns: List<SegmentTableColumn> = defaultSegmentTableColumns,
    highlighted: String? = null,
    emphasisColor: String? = null,
    markerId: String = SEGMENT_TABLE_CURSOR_MARKER_ID,
    markerColor: String? = null,
    enabled: Boolean = true,
    strings: Map<String, String>? = null,
    onHover: ((ItinerarySegmentRow?) -> Unit)? = null,
    onSegmentClick: ((String) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null,
) {
    ProvideStrings(strings) {
        ItinerarySegmentTableOver(
            engine,
            modifier,
            legs,
            map,
            units,
            columns,
            highlighted,
            emphasisColor,
            markerId,
            markerColor,
            enabled,
            onHover,
            onSegmentClick,
            onError,
        )
    }
}

/** The roadbook of the itinerary [resource] reads, which its host makes or the app hands. */
@Composable
private fun ItinerarySegmentTableOver(
    resource: ItineraryResourceEngine,
    modifier: Modifier,
    legs: List<ItineraryLeg>?,
    map: MapEngine?,
    units: UnitPreferences?,
    columns: List<SegmentTableColumn>,
    highlighted: String?,
    emphasisColor: String?,
    markerId: String,
    markerColor: String?,
    enabled: Boolean,
    onHover: ((ItinerarySegmentRow?) -> Unit)?,
    onSegmentClick: ((String) -> Unit)?,
    onError: ((Throwable) -> Unit)?,
) {
    val scope = LocalMountayaScope.current
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val state by resource.state.collectAsState()
    val link = remember(markerId) { MapLink<String>(markerId) }
    // The leg the map's own cursor is over, as its hover tells it, which lights that leg's row.
    var hovered by remember(map) { mutableStateOf<String?>(null) }
    LaunchedEffect(map) {
        map?.events()?.collect { event -> if (event is MapEvent.FeatureHover) hovered = hoveredLeg(event) }
    }
    val drawing = remember(state, legs) { ItineraryDrawing((state as? ItineraryResource.Resolved)?.itinerary, legs) }
    val figures = units ?: UnitPreferences(scope.locale)
    val currentOnError by rememberUpdatedState(onError)
    val point by rememberUpdatedState { row: ItinerarySegmentRow? ->
        link.point(row?.key, row, drawing.segmentTarget(row), enabled, onHover, map, markerColor, emphasisColor)
    }
    // The state as this composition reads it: an effect reading the state later would see a failure the next
    // composition reports too.
    val read = state
    LaunchedEffect(read) { (read as? ItineraryResource.Failed)?.let { currentOnError?.invoke(it.cause) } }
    LaunchedEffect(enabled) { if (!enabled) point(null) }
    DisposableEffect(Unit) { onDispose { point(null) } }
    val dimmed = tokenColor(tokens.dimmed, scheme)
    Column(modifier.semantics { contentDescription = scope.word("table.title") }) {
        if (drawing.rows.isEmpty()) {
            val word = if (state is ItineraryResource.Resolving) "table.loading" else "table.empty"
            BasicText(scope.word(word), style = TextStyle(color = dimmed, fontSize = 13.sp))
        } else {
            TableHeader(columns.map { scope.word("table.${it.id}") })
            for (row in drawing.rows) {
                key(row.key) {
                    TableRow(
                        isLit = isSegmentRowLit(row, link.pointed, highlighted, hovered),
                        enabled = enabled,
                        onClick = {
                            point(if (link.pointed == row.key) null else row)
                            onSegmentClick?.invoke(row.id)
                        },
                        onHover = { isOver ->
                            if (isOver) {
                                point(row)
                            } else if (link.pointed == row.key) {
                                point(null)
                            }
                        },
                    ) {
                        for (column in columns) {
                            Box(
                                Modifier.weight(1f),
                            ) { SegmentCell(segmentRowCell(row, column), drawing.series, figures) }
                        }
                    }
                }
            }
        }
    }
}

/** One cell of a leg's row, as its column states it: a sparkline, or its figure over the running one. */
@Composable
private fun SegmentCell(
    cell: SegmentRowCell,
    series: ElevationSeries,
    units: UnitPreferences,
) {
    if (cell is SegmentRowCell.Profile) {
        ItinerarySparkline(series, Modifier.fillMaxWidth().height(TABLE_SPARKLINE_HEIGHT_DP.dp), cell.lit, cell.color)
        return
    }
    val tokens = LocalMountayaTokens.current
    val ink = tokenColor(tokens.panelFg, themeSchemeInForce())
    val lines = segmentCellText(cell, units)
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        val weight = if (cell is SegmentRowCell.Segment) FontWeight.SemiBold else FontWeight.Normal
        BasicText(
            lines.firstOrNull().orEmpty(),
            style = TextStyle(color = ink, fontSize = 14.sp, fontWeight = weight),
            maxLines = 2,
        )
        for (total in lines.drop(1)) {
            BasicText(
                total,
                style = TextStyle(color = ink.copy(alpha = ink.alpha * RUNNING_FIGURE_OPACITY), fontSize = 11.sp),
            )
        }
    }
}

/**
 * What a leg's cell writes, its own figure first and the running one under it where it has one, in the reader's
 * [units]: a band as the locale's range, a climb and a loss signed, and a difficulty as the itinerary writes it. A
 * sparkline writes nothing.
 */
internal fun segmentCellText(
    cell: SegmentRowCell,
    units: UnitPreferences,
): List<String> =
    when (cell) {
        is SegmentRowCell.Segment -> {
            listOf(cell.name)
        }

        is SegmentRowCell.Profile -> {
            emptyList()
        }

        is SegmentRowCell.Distance -> {
            listOf(formatDistance(cell.leg, units)) + listOfNotNull(cell.total?.let { formatDistance(it, units) })
        }

        is SegmentRowCell.Elevation -> {
            val own =
                listOfNotNull(
                    cell.ascent?.let { "+" + formatElevation(it, units) },
                    cell.descent?.let { "-" + formatElevation(it, units) },
                ).joinToString(" ")
            listOf(own) + listOfNotNull(cell.totalAscent?.let { "+" + formatElevation(it, units) })
        }

        is SegmentRowCell.Altitude -> {
            listOf(cell.band?.let { formatAltitudeRange(it.min, it.max, units) } ?: formatElevation(cell.single, units))
        }

        is SegmentRowCell.Duration -> {
            listOf(formatDuration(cell.seconds, units))
        }

        is SegmentRowCell.Difficulty -> {
            listOf(cell.text?.value.orEmpty())
        }
    }

/** A table's header row: each column's word, read by accessibility services as a heading. */
@Composable
internal fun TableHeader(words: List<String>) {
    val tokens = LocalMountayaTokens.current
    val dimmed = tokenColor(tokens.dimmed, themeSchemeInForce())
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .semantics { heading() },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (word in words) {
            BasicText(
                word,
                Modifier.weight(1f),
                style = TextStyle(color = dimmed, fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

/**
 * One row of a table: its cells side by side, the platform's touch target high, lit in `table-row-hover` while
 * [isLit], bordered under in `table-border`, pressed as a whole, ringed in `focus-ring` while it holds the keyboard's
 * focus, told whether a hovering pointer stands over it, and read by accessibility services as one selectable button.
 */
@Composable
internal fun TableRow(
    isLit: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    onHover: (Boolean) -> Unit,
    cells: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val hover by rememberUpdatedState(onHover)
    val border = tokenColor(tokens.tableBorder, scheme)
    val interactions = remember { MutableInteractionSource() }
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = TABLE_ROW_MIN_HEIGHT_DP.dp)
                .background(if (isLit) tokenColor(tokens.tableRowHover, scheme) else Color.Transparent)
                .keyboardFocusRing(interactions)
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    awaitPointerEventScope {
                        while (true) {
                            when (awaitPointerEvent().type) {
                                PointerEventType.Enter -> hover(true)
                                PointerEventType.Exit -> hover(false)
                            }
                        }
                    }
                }.clickable(
                    interactionSource = interactions,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                ).semantics { selected = isLit }
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = cells,
        )
        Box(Modifier.fillMaxWidth().height(1.dp).background(border))
    }
}
