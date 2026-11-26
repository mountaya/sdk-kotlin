package com.mountaya.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountaya.geo.LayerRank
import com.mountaya.geo.PoiPanelGroup
import com.mountaya.geo.PoiPanelRow
import com.mountaya.geo.PoiPanelRows
import com.mountaya.geo.poiPanelNoteZoom
import com.mountaya.geo.poiPanelRows

/**
 * PoiPanel toggles what a map's points of interest show, one concept at a time (`geo.layers.poi-rows`).
 *
 * The unit is the catalogue's key, a map concept a fill and the label over it may both draw, so a toggle never hides
 * one and leaves the other floating. Its rows are what the map draws at the points-of-interest rank, so a catalogue
 * that gains a concept gains a row, and a map drawing no point of interest shows no panel at all. [keys] curates them,
 * a row per key it lists that is drawn, in its own order; [groups], which supersede [keys], a row per concept of the
 * app's own, driving the drawn keys it names. A key's row is labelled from [labels], else by the key.
 *
 * A row is on while any key it drives is shown, read off the map as it changes, whoever changed it; [activeGroups]
 * holds the groups the app keeps on itself, which the rows then read instead. A press shows or hides the row's keys on
 * the map, unless the app hands a handler in its place, [onToggle] for a key's row and [onToggleGroup] for a group's,
 * told the row and the state it asks for. [showsAllToggle] adds a row over them all, on while any row is, its press
 * handed to [onToggleAll] where the app hands one. Below [minZoom], the zoom the overlay draws from, it says the overlay
 * draws nothing yet, never at it.
 *
 * It is drawn again as what it offers changes, and its note as the camera crosses [minZoom], never once per camera frame
 * (`zoom-threshold` in the contract's `sdk/hot-paths.yaml`); each row is a part of its own, handed equal values and
 * pressed through handlers that stay as the panel is drawn again, so a redraw that changes no row draws no row
 * (`panel-rows`).
 *
 * @param map The map whose points of interest the panel toggles, or null for none, which offers no row.
 * @param modifier The host's modifier, as every composable takes one.
 * @param keys The catalogue's keys a row is offered for, in their order, or null for every one drawn.
 * @param groups The app's own concepts, each a row, superseding [keys], or null for none.
 * @param activeGroups The groups the app holds on itself, or null for the rows to read the map.
 * @param labels Each key's label, the key itself where it states none.
 * @param minZoom The zoom the overlay draws from, below which the panel says it draws nothing, or null for none.
 * @param showsAllToggle Whether a row over them all stands above them.
 * @param strings The words it states for itself, by path, `controls.zoomIn`, each standing over the scope's for it
 * and everything it draws, or null for the scope's own.
 * @param onToggle Handed a key's row and the state it asks for, in place of the map, where the app hands one.
 * @param onToggleGroup Handed a group's row and the state it asks for, in place of the map, where the app hands one.
 * @param onToggleAll Handed the state the row over them all asks for, in place of the map, where the app hands one.
 */
@Composable
public fun PoiPanel(
    map: MapEngine?,
    modifier: Modifier = Modifier,
    keys: List<String>? = null,
    groups: List<PoiPanelGroup>? = null,
    activeGroups: List<String>? = null,
    labels: Map<String, String> = emptyMap(),
    minZoom: Double? = null,
    showsAllToggle: Boolean = false,
    strings: Map<String, String>? = null,
    onToggle: ((String, Boolean) -> Unit)? = null,
    onToggleGroup: ((String, Boolean) -> Unit)? = null,
    onToggleAll: ((Boolean) -> Unit)? = null,
) {
    ProvideStrings(strings) {
        LocalMountayaRenderProbe.current?.report("PoiPanel")
        val scope = LocalMountayaScope.current
        // The map's state, read where what the panel offers and its note are derived and never by the panel itself: the
        // offer is read again as the map changes, whoever changed it, and the panel drawn again only where it moved.
        val drawn = map?.state?.collectAsState()
        val offer by remember(map, keys, groups, activeGroups, labels, drawn) {
            derivedStateOf(structuralEqualityPolicy()) {
                val reading = if (drawn?.value == null) null else map
                poiOffer(reading, keys, groups, activeGroups, labels)
            }
        }
        if (offer.rows.isEmpty()) return@ProvideStrings
        // What a press does, read from the panel as it was drawn last, behind handlers that stay as it is drawn again.
        val toggle by rememberUpdatedState { id: String, visible: Boolean ->
            val row = offer.rows.firstOrNull { it.id == id }
            if (row != null) poiToggle(row, visible, map, groups != null, onToggle, onToggleGroup)
        }
        val toggleAll by rememberUpdatedState { visible: Boolean ->
            poiToggleAll(visible, offer.rows, map, onToggleAll)
        }
        val onRowPress = remember { { id: String, visible: Boolean -> toggle(id, visible) } }
        val onAllPress = remember { { visible: Boolean -> toggleAll(visible) } }
        PanelSurface(title = scope.word("controls.pois"), modifier) {
            if (showsAllToggle) PoiAllRow(scope.word("pois.displayOnMap"), offer.isAnyVisible, onAllPress)
            PoiNote(drawn, minZoom)
            Column {
                PanelHeading(scope.word("pois.categories"))
                for (row in offer.rows) {
                    key(row.id) { PoiRow(row.id, row.label, row.glyph, row.isVisible, onRowPress) }
                }
            }
        }
    }
}

/** The row over every other, on while any is, its press asking [onPress] for them all to be on or off. */
@Composable
private fun PoiAllRow(
    label: String,
    isAnyVisible: Boolean,
    onPress: (Boolean) -> Unit,
) {
    val scope = LocalMountayaScope.current
    PanelRow(label = label, isChecked = isAnyVisible, onClick = { onPress(!isAnyVisible) }) { ink ->
        if (isAnyVisible) Glyph("check", set = scope.controlGlyphs, size = 14.dp, color = ink)
    }
}

/** A row of the panel, [id]'s, labelled [label], its group's [glyph] beside it, on while [isVisible], pressed through [onPress]. */
@Composable
private fun PoiRow(
    id: String,
    label: String,
    glyph: String?,
    isVisible: Boolean,
    onPress: (String, Boolean) -> Unit,
) {
    val scope = LocalMountayaScope.current
    PanelRow(label = label, isChecked = isVisible, onClick = { onPress(id, !isVisible) }) { ink ->
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            glyph?.let { Glyph(it, set = scope.controlGlyphs, size = 14.dp, color = ink) }
            if (isVisible) Glyph("check", set = scope.controlGlyphs, size = 14.dp, color = ink)
        }
    }
}

/**
 * The note that the overlay draws nothing yet, while the camera of the map [drawn] holds is below [minZoom]: it compares
 * the zoom itself, on every state the map reports, and is drawn again only where the comparison flips.
 */
@Composable
private fun PoiNote(
    drawn: State<MapState>?,
    minZoom: Double?,
) {
    val probe = LocalMountayaRenderProbe.current
    val noted by remember(drawn, minZoom) {
        derivedStateOf(structuralEqualityPolicy()) {
            probe?.report("PoiNote compared")
            isPoiNoted(drawn?.value?.camera?.zoom, minZoom)
        }
    }
    probe?.report("PoiNote")
    if (!noted) return
    val scope = LocalMountayaScope.current
    val dimmed = tokenColor(LocalMountayaTokens.current.dimmed, themeSchemeInForce())
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Glyph("info", set = scope.controlGlyphs, size = 14.dp, color = dimmed)
        BasicText(scope.word("pois.zoomWarning"), style = TextStyle(color = dimmed, fontSize = 12.sp))
    }
}

/** What the panel offers over [map]: the keys it draws at the points-of-interest rank and whether each is shown. */
internal fun poiOffer(
    map: MapEngine?,
    keys: List<String>?,
    groups: List<PoiPanelGroup>?,
    activeGroups: List<String>?,
    labels: Map<String, String>,
): PoiPanelRows =
    poiPanelRows(
        drawn = map?.drawnLayerKeys(LayerRank.Poi.rank).orEmpty(),
        isKeyVisible = { key -> map?.isKeyVisible(key) ?: true },
        keys = keys,
        groups = groups,
        activeGroups = activeGroups,
        labels = labels,
    )

/**
 * A press on [row] asking for it to be [visible]: the app's handler where it hands one, [onToggleGroup] for a group's
 * row where [isGroup] and [onToggle] for a key's, each of the row's keys shown or hidden on [map] otherwise.
 */
internal fun poiToggle(
    row: PoiPanelRow,
    visible: Boolean,
    map: MapEngine?,
    isGroup: Boolean,
    onToggle: ((String, Boolean) -> Unit)?,
    onToggleGroup: ((String, Boolean) -> Unit)?,
) {
    val handler = if (isGroup) onToggleGroup else onToggle
    if (handler != null) return handler(row.id, visible)
    for (key in row.keys) map?.setKeyVisibility(key, visible)
}

/** A press on the row over them all asking for every row to be [visible]: [onToggleAll] where the app hands one, every row's keys on [map] otherwise. */
internal fun poiToggleAll(
    visible: Boolean,
    rows: List<PoiPanelRow>,
    map: MapEngine?,
    onToggleAll: ((Boolean) -> Unit)?,
) {
    if (onToggleAll != null) return onToggleAll(visible)
    for (key in rows.flatMap { it.keys }) map?.setKeyVisibility(key, visible)
}

/**
 * Whether the panel notes the overlay draws nothing: while the camera's [zoom] is below [minZoom], never at it, and never
 * with no camera, no floor or one that is no number.
 */
internal fun isPoiNoted(
    zoom: Double?,
    minZoom: Double?,
): Boolean = zoom != null && !(zoom >= poiPanelNoteZoom(minZoom))
