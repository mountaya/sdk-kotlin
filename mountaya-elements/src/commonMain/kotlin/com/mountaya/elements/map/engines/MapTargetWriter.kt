package com.mountaya.elements

/**
 * MapTargetWriter writes what a data element points at to the map beside it: one cursor marker under the element's
 * own id, [markerId] (`map.profile.cursor-marker`), and the stretch it lights on the map's emphasis slot
 * (`map.profile.emphasis`).
 *
 * It holds what the element pinned and whether it lit the slot, so it never takes down a marker or a stretch another
 * element wrote. It is driven from the one thread its map is.
 */
internal class MapTargetWriter(
    val markerId: String,
) {
    /** What the element's marker is drawn with, null where it pinned none. */
    var pinned: CursorMarkerPin? = null
        private set

    /** Whether the element lit the map's emphasis slot. */
    var isLit: Boolean = false
        private set

    /**
     * Writes [target] to [map]: the marker at its position in [markerColor], taken down where it states none, and its
     * stretch lit in [emphasisColor], what the element lit taken down where it states none. A position the map refuses
     * leaves the marker as it stood.
     */
    fun write(
        target: MapTarget,
        map: MapEngine,
        markerColor: String?,
        emphasisColor: String?,
    ) {
        val lngLat = target.lngLat
        if (lngLat != null) {
            try {
                pinned = showCursorMarker(map, pinned, CursorMarkerPoint(markerId, lngLat, markerColor))
            } catch (_: MapException) {
                // The marker the map holds, if any, stands as it was.
            }
        } else {
            hideCursorMarker(map, pinned, markerId)
            pinned = null
        }
        isLit = writeLegEmphasis(map, isLit, target.stretch, emphasisColor)
    }
}
