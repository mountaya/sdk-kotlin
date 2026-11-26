package com.mountaya.elements

import com.mountaya.client.GeoJsonGeometry

/**
 * LegEmphasisTarget is the map whose emphasis slot an element lights the leg a reader points at on
 * (`map.profile.emphasis`): the slot calls a map answers. `MapEngine` is one; a map of an app's own, or a test's
 * double recording what an element writes, is another.
 *
 * It is driven from the one thread its map is.
 */
public interface LegEmphasisTarget {
    /**
     * Draws [geometry] in [slot], in [color], the slot's own where it is null, as `MapEngine` draws one
     * (`map.geometry.slots`).
     */
    public fun setGeometry(
        geometry: GeoJsonGeometry?,
        slot: GeometrySlot,
        color: String? = null,
    )

    /** Takes [slot] down, giving it back its own colour. */
    public fun clearGeometry(slot: GeometrySlot)
}
