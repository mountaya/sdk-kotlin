package com.mountaya.elements

import com.mountaya.client.GeoJsonGeometry

/**
 * writeLegEmphasis lights the [stretch] an element points at on [map]'s emphasis slot, in [color], the map's own
 * where it is null, or takes down what the element lit where it points at none, [stretch] null, and answers
 * whether the slot holds the element's stretch afterwards (`map.profile.emphasis`). The stretch is the series cut
 * between the leg's two ends (`lineBetween`), the line the chart drew.
 *
 * An element clears only a slot it wrote, [isLit]: two elements over one map would otherwise take turns clearing
 * each other's stretch as a pointer left one for the other, and an element that never pointed at a leg leaves the
 * slot as it found it.
 */
public fun writeLegEmphasis(
    map: LegEmphasisTarget,
    isLit: Boolean,
    stretch: GeoJsonGeometry?,
    color: String? = null,
): Boolean {
    if (stretch == null) {
        if (isLit) map.clearGeometry(GeometrySlot.Emphasis)
        return false
    }
    map.setGeometry(stretch, GeometrySlot.Emphasis, color)
    return true
}
