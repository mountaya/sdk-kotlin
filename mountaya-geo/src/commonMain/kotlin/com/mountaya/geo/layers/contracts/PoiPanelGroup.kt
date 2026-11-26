package com.mountaya.geo

/**
 * PoiPanelGroup is one curated concept a points-of-interest picker offers as one row (`geo.layers.poi-rows`),
 * drawn by however many of the layers catalogue's keys it takes: water is a spring, a drinking tap and a
 * fountain.
 *
 * A group is the app's data, not a published taxonomy: the label is the app's own words and the keys are the
 * catalogue's. Two are equal where every member is.
 */
public class PoiPanelGroup(
    /** How the group is named to the app's own handler and among the groups it holds on. */
    public val id: String,
    /** The group's own label. */
    public val label: String,
    /** The catalogue's keys the concept is drawn by. A row stands where any is drawn. */
    public val keys: List<String>,
    /** The role of a glyph of the control set drawn beside the label, or null for none. */
    public val glyph: String? = null,
) {
    override fun equals(other: Any?): Boolean =
        other is PoiPanelGroup && other.id == id && other.label == label && other.keys == keys && other.glyph == glyph

    override fun hashCode(): Int =
        ((id.hashCode() * 31 + label.hashCode()) * 31 + keys.hashCode()) * 31 + glyph.hashCode()

    override fun toString(): String = "PoiPanelGroup(id=$id, label=$label, keys=$keys, glyph=$glyph)"
}
