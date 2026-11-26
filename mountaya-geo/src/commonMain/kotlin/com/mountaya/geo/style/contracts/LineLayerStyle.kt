package com.mountaya.geo

import kotlinx.serialization.json.JsonElement

/**
 * LineLayerStyle is how a map's `line` layer draws, as MapLibre's style document writes it: its
 * [layout] and [paint] properties under their style names, `line-cap` and `line-color`. The layer's id,
 * its source, the source layer it draws and its filter are its caller's, so one style draws in any layer
 * that names them.
 *
 * Two are equal where they hold the same properties, a JSON value as the serialization library compares
 * it, and equal styles hash alike.
 */
public class LineLayerStyle(
    /** The layer's layout properties, by their style names. */
    public val layout: Map<String, JsonElement>,
    /** The layer's paint properties, by their style names. */
    public val paint: Map<String, JsonElement>,
) {
    override fun equals(other: Any?): Boolean =
        other is LineLayerStyle && other.layout == layout && other.paint == paint

    override fun hashCode(): Int = layout.hashCode() * 31 + paint.hashCode()

    override fun toString(): String = "LineLayerStyle(layout=$layout, paint=$paint)"
}
