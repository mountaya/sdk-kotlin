package com.mountaya.elements

import com.mountaya.client.GeoJsonGeometry

/**
 * SlotGeometry is what one geometry slot of a map holds: its [geometry], null where it draws none, and the
 * [color] it draws in, the slot's own until a call names another (`map.geometry.slots`).
 *
 * Two are equal where they hold the same geometry, each number as the same number, and the same colour.
 */
public class SlotGeometry(
    /** The geometry the slot draws, or null for none. */
    public val geometry: GeoJsonGeometry?,
    /** The colour the slot draws in, a CSS hexadecimal colour or any colour MapLibre reads. */
    public val color: String,
) {
    override fun equals(other: Any?): Boolean =
        other is SlotGeometry && other.geometry == geometry && other.color == color

    override fun hashCode(): Int = (geometry?.hashCode() ?: 0) * 31 + color.hashCode()

    override fun toString(): String = "SlotGeometry($geometry, $color)"
}
