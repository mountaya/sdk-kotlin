package com.mountaya.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.BadgeSurface
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.ElevationSeries
import com.mountaya.geo.ItineraryResource
import com.mountaya.geo.ItineraryResourceCache
import com.mountaya.geo.ItineraryResourceEngine
import com.mountaya.geo.ItineraryWaypointRow
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.WaypointRowCell
import com.mountaya.geo.WaypointTableColumn
import com.mountaya.geo.amenityFilter
import com.mountaya.geo.amenityGlyph
import com.mountaya.geo.amenityName
import com.mountaya.geo.badgeFallback
import com.mountaya.geo.defaultWaypointTableColumns
import com.mountaya.geo.formatDistance
import com.mountaya.geo.formatElevation
import com.mountaya.geo.toggleAmenity
import com.mountaya.geo.waypointRowCell
import kotlin.time.Instant

/**
 * ItineraryWaypointTable is an itinerary's places as a table: a row per pass of a place in itinerary order, a loop's
 * shared start and finish two rows, each stating, column by column, its glyph and name, the sparkline with the stretch
 * reaching it lit, its running distance, the climb and loss since the place before, and, as the app asks, its
 * altitude, its services, its cut-off and its fields (`geo.profile.waypoint-rows`, `geo.profile.waypoint-table-cells`).
 *
 * ```
 * ItineraryWaypointTable(itineraryId = "itin_abc", cache = itineraries, map = map)
 * ```
 *
 * Over its rows stand a chip per service its places offer, sorted by the reader's name for it, and a row stays only
 * where its place offers every service picked (`geo.profile.amenity-filter`); the choice is the app's where it hands
 * [picked], and the table's own otherwise. A cut-off and a `date` field are written only by [formatCutoffAt], the time
 * zone being the app's. A row a reader points at, a hovering pointer over it or a press on it, pins a marker on [map]
 * at its place under [markerId] (`map.profile.cursor-marker`), taken down as the pointer leaves or the table goes;
 * [onHover] takes that over, and a press hands the place's id to [onWaypointClick]. A table not [enabled] draws and
 * drives nothing. Each row and each chip is the platform's touch target high.
 *
 * It reads through the app's own `ItineraryResourceEngine` where the app hands it to the overload that takes one, in
 * place of the id, the cache and the surface, never configuring or closing it.
 *
 * Its words are the scope's `table`, `profile` and `amenities` sections, each one its own [strings] name standing over
 * the scope's, its glyphs the scope's marker set, its figures written in [units], and its colours the tokens'.
 *
 * @param itineraryId The itinerary whose places are listed, or null for none.
 * @param cache The cache the itinerary is read through, one per client.
 * @param modifier The host's modifier, as every composable takes one.
 * @param surface The surface the itinerary is read on, null for the one the credential reads by default.
 * @param map The map a row pointed at is pinned on, or null for none.
 * @param units The reader's units, the scope's locale in metric where null.
 * @param columns The columns drawn, in order.
 * @param picked The services picked, held by the app, or null for the table's own choice.
 * @param markerId The id the row's marker is pinned under on the map.
 * @param markerColor The colour of the row's marker, the map's accent where null.
 * @param formatCutoffAt How a cut-off and a `date` field are written; neither is written where it is null.
 * @param enabled Whether the table drives anything.
 * @param strings The words it states for itself, by path, `table.title`, each standing over the scope's for it and
 * everything it draws, or null for the scope's own.
 * @param onHover Handed the row pointed at, or null as it is let go, in place of the map's link.
 * @param onWaypointClick Handed the id of the place of a row pressed.
 * @param onError Handed a failure to read the itinerary.
 */
@Composable
public fun ItineraryWaypointTable(
    itineraryId: String?,
    cache: ItineraryResourceCache,
    modifier: Modifier = Modifier,
    surface: RestSchema.ReadSurface? = null,
    map: MapEngine? = null,
    units: UnitPreferences? = null,
    columns: List<WaypointTableColumn> = defaultWaypointTableColumns,
    picked: MutableState<List<RestSchema.WaypointAmenity>>? = null,
    markerId: String = WAYPOINT_TABLE_CURSOR_MARKER_ID,
    markerColor: String? = null,
    formatCutoffAt: ((Instant) -> String)? = null,
    enabled: Boolean = true,
    strings: Map<String, String>? = null,
    onHover: ((ItineraryWaypointRow?) -> Unit)? = null,
    onWaypointClick: ((String) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null,
) {
    val resource = remember { ItineraryResourceEngine(cache) }
    DisposableEffect(resource) { onDispose { resource.close() } }
    SideEffect { resource.configure(cache, itineraryId, surface) }
    ProvideStrings(strings) {
        ItineraryWaypointTableOver(
            resource,
            modifier,
            map,
            units,
            columns,
            picked,
            markerId,
            markerColor,
            formatCutoffAt,
            enabled,
            onHover,
            onWaypointClick,
            onError,
        )
    }
}

/**
 * ItineraryWaypointTable is the places of the itinerary the app's [engine] reads, drawn as the table over a cache draws
 * them: it reads the engine's `state` and never configures or closes it, the app choosing the itinerary through
 * `configure`. The compiler holds that a table reads through an engine or a cache, never neither.
 *
 * @param engine The app's engine the itinerary is read through, which the table never closes.
 * @param modifier The host's modifier, as every composable takes one.
 * @param map The map a row pointed at is pinned on, or null for none.
 * @param units The reader's units, the scope's locale in metric where null.
 * @param columns The columns drawn, in order.
 * @param picked The services picked, held by the app, or null for the table's own choice.
 * @param markerId The id the row's marker is pinned under on the map.
 * @param markerColor The colour of the row's marker, the map's accent where null.
 * @param formatCutoffAt How a cut-off and a `date` field are written; neither is written where it is null.
 * @param enabled Whether the table drives anything.
 * @param strings The words it states for itself, by path, `table.title`, each standing over the scope's for it and
 * everything it draws, or null for the scope's own.
 * @param onHover Handed the row pointed at, or null as it is let go, in place of the map's link.
 * @param onWaypointClick Handed the id of the place of a row pressed.
 * @param onError Handed a failure to read the itinerary.
 */
@Composable
public fun ItineraryWaypointTable(
    engine: ItineraryResourceEngine,
    modifier: Modifier = Modifier,
    map: MapEngine? = null,
    units: UnitPreferences? = null,
    columns: List<WaypointTableColumn> = defaultWaypointTableColumns,
    picked: MutableState<List<RestSchema.WaypointAmenity>>? = null,
    markerId: String = WAYPOINT_TABLE_CURSOR_MARKER_ID,
    markerColor: String? = null,
    formatCutoffAt: ((Instant) -> String)? = null,
    enabled: Boolean = true,
    strings: Map<String, String>? = null,
    onHover: ((ItineraryWaypointRow?) -> Unit)? = null,
    onWaypointClick: ((String) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null,
) {
    ProvideStrings(strings) {
        ItineraryWaypointTableOver(
            engine,
            modifier,
            map,
            units,
            columns,
            picked,
            markerId,
            markerColor,
            formatCutoffAt,
            enabled,
            onHover,
            onWaypointClick,
            onError,
        )
    }
}

/** The places of the itinerary [resource] reads, which its host makes or the app hands. */
@Composable
private fun ItineraryWaypointTableOver(
    resource: ItineraryResourceEngine,
    modifier: Modifier,
    map: MapEngine?,
    units: UnitPreferences?,
    columns: List<WaypointTableColumn>,
    picked: MutableState<List<RestSchema.WaypointAmenity>>?,
    markerId: String,
    markerColor: String?,
    formatCutoffAt: ((Instant) -> String)?,
    enabled: Boolean,
    onHover: ((ItineraryWaypointRow?) -> Unit)?,
    onWaypointClick: ((String) -> Unit)?,
    onError: ((Throwable) -> Unit)?,
) {
    val scope = LocalMountayaScope.current
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val state by resource.state.collectAsState()
    val link = remember(markerId) { MapLink<String>(markerId) }
    val ownPicked = remember { mutableStateOf<List<RestSchema.WaypointAmenity>>(emptyList()) }
    val drawing = remember(state) { ItineraryDrawing((state as? ItineraryResource.Resolved)?.itinerary, null) }
    val rows = remember(drawing) { drawing.waypointRows }
    val figures = units ?: UnitPreferences(scope.locale)
    val choice = picked?.value ?: ownPicked.value
    val filter = amenityFilter(rows, choice, scope)
    val currentOnError by rememberUpdatedState(onError)
    val point by rememberUpdatedState { row: ItineraryWaypointRow? ->
        link.point(row?.key, row, drawing.waypointTarget(row), enabled, onHover, map, markerColor, null)
    }
    // The state as this composition reads it: an effect reading the state later would see a failure the next
    // composition reports too.
    val read = state
    LaunchedEffect(read) { (read as? ItineraryResource.Failed)?.let { currentOnError?.invoke(it.cause) } }
    LaunchedEffect(enabled) { if (!enabled) point(null) }
    DisposableEffect(Unit) { onDispose { point(null) } }
    val dimmed = tokenColor(tokens.dimmed, scheme)
    Column(modifier.semantics { contentDescription = scope.word("table.waypointTitle") }) {
        if (rows.isEmpty()) {
            val word = if (state is ItineraryResource.Resolving) "table.loading" else "table.waypointEmpty"
            BasicText(scope.word(word), style = TextStyle(color = dimmed, fontSize = 13.sp))
        } else {
            if (filter.available.isNotEmpty()) {
                AmenityChips(filter.available, choice, enabled) { amenity ->
                    val next = toggleAmenity(choice, amenity)
                    if (picked != null) picked.value = next else ownPicked.value = next
                }
            }
            TableHeader(columns.map { scope.word("table.${it.id}") })
            if (filter.shown.isEmpty()) {
                BasicText(
                    scope.word("table.waypointFilterEmpty"),
                    Modifier.padding(vertical = 8.dp),
                    style = TextStyle(color = dimmed, fontSize = 13.sp),
                )
            }
            for (row in filter.shown) {
                key(row.key) {
                    TableRow(
                        isLit = link.pointed == row.key,
                        enabled = enabled,
                        onClick = {
                            point(if (link.pointed == row.key) null else row)
                            onWaypointClick?.invoke(row.place.id)
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
                            Box(Modifier.weight(1f)) {
                                WaypointCell(
                                    waypointRowCell(row, column, scope.markerGlyphs, figures, formatCutoffAt),
                                    drawing.series,
                                    figures,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * The filter's chips over a waypoint table, one per service in [available], each pressed to toggle its service, read
 * as selected while it is among [choice], drawn in the accent while picked and on the panel's ground otherwise.
 */
@Composable
private fun AmenityChips(
    available: List<RestSchema.WaypointAmenity>,
    choice: List<RestSchema.WaypointAmenity>,
    enabled: Boolean,
    onToggle: (RestSchema.WaypointAmenity) -> Unit,
) {
    val scope = LocalMountayaScope.current
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 6.dp)
            .semantics { contentDescription = scope.word("table.amenityFilter") },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        for (amenity in available) {
            val isPicked = amenity in choice
            val ground = if (isPicked) tokenColor(tokens.accent, scheme) else tokenColor(tokens.panelBg, scheme)
            val ink = if (isPicked) Color.White else tokenColor(tokens.panelFg, scheme)
            val interactions = remember { MutableInteractionSource() }
            Box(
                Modifier
                    .heightIn(min = TABLE_ROW_MIN_HEIGHT_DP.dp)
                    .keyboardFocusRing(interactions, shape)
                    .selectable(
                        selected = isPicked,
                        interactionSource = interactions,
                        indication = null,
                        enabled = enabled,
                        role = Role.Checkbox,
                    ) { onToggle(amenity) },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    amenityName(amenity, scope),
                    Modifier
                        .background(ground, shape)
                        .border(1.dp, tokenColor(tokens.panelBorder, scheme), shape)
                        .heightIn(min = 32.dp)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    style = TextStyle(color = ink, fontSize = 12.sp),
                )
            }
        }
    }
}

/** One cell of a place's row, as its column states it. */
@Composable
private fun WaypointCell(
    cell: WaypointRowCell,
    series: ElevationSeries,
    units: UnitPreferences,
) {
    val scope = LocalMountayaScope.current
    val tokens = LocalMountayaTokens.current
    val ink = tokenColor(tokens.panelFg, themeSchemeInForce())
    when (cell) {
        is WaypointRowCell.Waypoint -> {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Glyph(cell.glyph, set = scope.markerGlyphs, size = 16.dp, color = ink)
                BasicText(
                    waypointCellText(cell, units, scope),
                    style = TextStyle(color = ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                    maxLines = 2,
                )
            }
        }

        is WaypointRowCell.Profile -> {
            ItinerarySparkline(series, Modifier.fillMaxWidth().height(TABLE_SPARKLINE_HEIGHT_DP.dp), cell.lit)
        }

        is WaypointRowCell.Distance, is WaypointRowCell.Elevation, is WaypointRowCell.Altitude -> {
            BasicText(waypointCellText(cell, units, scope), style = TextStyle(color = ink, fontSize = 14.sp))
        }

        is WaypointRowCell.Amenities -> {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (amenity in cell.amenities) {
                    val glyph = amenityGlyph(amenity)
                    val name = amenityName(amenity, scope)
                    if (glyph in scope.markerGlyphs.markup) {
                        Glyph(glyph, set = scope.markerGlyphs, size = 14.dp, color = ink, label = name)
                    } else {
                        BasicText(
                            badgeFallback(name, BadgeSurface.Cell).orEmpty(),
                            style = TextStyle(color = ink, fontSize = 11.sp),
                        )
                    }
                }
            }
        }

        is WaypointRowCell.Cutoff, is WaypointRowCell.Properties -> {
            BasicText(
                waypointCellText(cell, units, scope),
                style = TextStyle(color = ink, fontSize = 12.sp),
                maxLines = 3,
            )
        }
    }
}

/**
 * What a place's cell writes, in the reader's [units] and the [scope]'s words: its name, the pin's word for a place
 * with none; its running distance; its climb and loss signed; its altitude; its services by name; its cut-off as the
 * app's formatter wrote it; and each field as its label and its value, a line each. A sparkline writes nothing.
 */
internal fun waypointCellText(
    cell: WaypointRowCell,
    units: UnitPreferences,
    scope: ElementsScope,
): String =
    when (cell) {
        is WaypointRowCell.Waypoint -> {
            cell.name ?: scope.word("profile.unlabelledWaypoint")
        }

        is WaypointRowCell.Profile -> {
            ""
        }

        is WaypointRowCell.Distance -> {
            formatDistance(cell.total, units)
        }

        is WaypointRowCell.Elevation -> {
            listOfNotNull(
                cell.ascent?.let { "+" + formatElevation(it, units) },
                cell.descent?.let { "-" + formatElevation(it, units) },
            ).joinToString(" ")
        }

        is WaypointRowCell.Altitude -> {
            formatElevation(cell.altitude, units)
        }

        is WaypointRowCell.Amenities -> {
            cell.amenities.joinToString(", ") { amenityName(it, scope) }
        }

        is WaypointRowCell.Cutoff -> {
            cell.moment.orEmpty()
        }

        is WaypointRowCell.Properties -> {
            cell.fields.joinToString("\n") { "${it.label}: ${it.value}" }
        }
    }
