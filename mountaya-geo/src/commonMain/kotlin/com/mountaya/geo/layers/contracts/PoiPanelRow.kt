package com.mountaya.geo

/**
 * PoiPanelRow is one row a points-of-interest picker offers (`geo.layers.poi-rows`): a key of the layers
 * catalogue drawn at the points-of-interest rank, or a curated group of them, with the drawn keys its toggle
 * drives and whether it is on.
 *
 * Two are equal where every member is.
 */
public class PoiPanelRow(
    /** The catalogue's key, or the group's id. */
    public val id: String,
    /** A group's own label, a key's from the labels handed in, else the key itself. */
    public val label: String,
    /** The role of the glyph a group draws beside its label, or null for none. */
    public val glyph: String? = null,
    /** The drawn keys the row's toggle drives, and none that is not drawn. */
    public val keys: List<String>,
    /**
     * Whether the row is on: while any key it drives is shown, or, for a group the app holds the state of,
     * while the app holds it on.
     */
    public val isVisible: Boolean,
) {
    override fun equals(other: Any?): Boolean =
        other is PoiPanelRow &&
            other.id == id &&
            other.label == label &&
            other.glyph == glyph &&
            other.keys == keys &&
            other.isVisible == isVisible

    override fun hashCode(): Int {
        var hash = id.hashCode()
        hash = 31 * hash + label.hashCode()
        hash = 31 * hash + glyph.hashCode()
        hash = 31 * hash + keys.hashCode()
        return 31 * hash + isVisible.hashCode()
    }

    override fun toString(): String =
        "PoiPanelRow(id=$id, label=$label, glyph=$glyph, keys=$keys, isVisible=$isVisible)"
}
