package com.mountaya.elements

/**
 * CursorMarkerTarget is the map an element's cursor marker is pinned to (`map.profile.cursor-marker`): the three
 * marker calls a map answers. `MapEngine` is one; a map of an app's own, or a test's double recording what an
 * element writes, is another.
 *
 * It is driven from the one thread its map is.
 */
public interface CursorMarkerTarget {
    /** Pins [marker] in place of the one pinned under its id, as `MapEngine` pins one. */
    public fun addMarker(marker: MapMarker)

    /**
     * Changes the marker pinned under [id], each member given replacing the one held, and throws a [MapException]
     * of kind `MarkerNotFound` where none is pinned under it.
     */
    public fun editMarker(
        id: String,
        position: List<Double>? = null,
        color: String? = null,
        glyph: String? = null,
        label: String? = null,
    )

    /** Takes off the marker pinned under [id], where one is. */
    public fun removeMarker(id: String)
}
