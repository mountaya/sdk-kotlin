package com.mountaya.elements

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * MapLink is what a data element points at, and what that writes to the map beside it: one cursor marker under the
 * element's own id, [markerId], moved as the reader moves (`map.profile.cursor-marker`), and the leg pointed at lit on
 * the map's emphasis slot (`map.profile.emphasis`). A profile points at a sample by its index, a table at a row by its
 * key.
 *
 * It holds what the element pinned and whether it lit the slot, so it never takes down a marker or a stretch another
 * element wrote, and pointing where it already points writes nothing. What it points at is snapshot state, so a
 * composition reading it draws again as it moves. It is driven from the one thread its map is.
 */
internal class MapLink<Key>(
    val markerId: String,
) {
    /** What writes the element's target to its map, holding what it pinned and lit. */
    private val writer = MapTargetWriter(markerId)

    /** What the element points at, null for nothing. */
    var pointed: Key? by mutableStateOf(null)
        private set

    /** What the element's marker is drawn with, null where it pinned none. */
    val pinned: CursorMarkerPin? get() = writer.pinned

    /** Whether the element lit the map's emphasis slot. */
    val isLit: Boolean get() = writer.isLit

    /**
     * Points the element at what [key] names, null for nothing, and tells it: [onHover] is handed [value], where the app
     * hands a handler, and [map] is otherwise written [target]. A disabled element points at nothing new and only lets
     * go, and pointing where it already points tells nothing, so a pointer resting on a sample writes the map once.
     */
    fun <Value> point(
        key: Key?,
        value: Value?,
        target: MapTarget,
        isEnabled: Boolean,
        onHover: ((Value?) -> Unit)?,
        map: MapEngine?,
        markerColor: String?,
        emphasisColor: String?,
    ) {
        if (!isEnabled && key != null) return
        if (pointed == key) return
        pointed = key
        if (onHover != null) {
            onHover(value)
            return
        }
        val drawn = map ?: return
        write(target, drawn, markerColor, emphasisColor)
    }

    /**
     * Writes [target] to [map]: the marker at its position in [markerColor], taken down where it states none, and its
     * stretch lit in [emphasisColor], what the element lit taken down where it states none, as [MapTargetWriter] writes
     * it.
     */
    fun write(
        target: MapTarget,
        map: MapEngine,
        markerColor: String?,
        emphasisColor: String?,
    ) {
        writer.write(target, map, markerColor, emphasisColor)
    }
}
