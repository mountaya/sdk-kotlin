package com.mountaya.geo

/**
 * PanelLayout is how a panel the controls open is laid out over a map (`geo.panels.layout`), the layers,
 * points-of-interest and preferences panels alike: its width, or none where it fills the width its corner leaves, the
 * most it grows to, its body scrolling under a title row that stays, the height of a layer card's picture, and the
 * columns its cards stand in, each in density-independent pixels.
 *
 * Two are equal where every member is, the numbers compared as every value of the geo layer compares them.
 */
public class PanelLayout(
    /** The panel's width, or null where it fills the width its corner leaves. */
    public val width: Double?,
    /** Whether the panel fills the width its corner leaves, on a map no wider than `panelNarrowMapWidth`. */
    public val fill: Boolean,
    /** The most the panel grows to, never below 0. */
    public val maxHeight: Double,
    /** The height of a layer card's picture. */
    public val pictureHeight: Double,
    /** The columns the layers panel's cards stand in. */
    public val layerCardColumns: Int,
    /** The columns the preferences panel's cards stand in. */
    public val preferenceCardColumns: Int,
) {
    override fun equals(other: Any?): Boolean =
        other is PanelLayout && sameNumberOrNull(other.width, width) && other.fill == fill &&
            sameNumber(other.maxHeight, maxHeight) && sameNumber(other.pictureHeight, pictureHeight) &&
            other.layerCardColumns == layerCardColumns && other.preferenceCardColumns == preferenceCardColumns

    override fun hashCode(): Int =
        listOf(width, fill, maxHeight, pictureHeight, layerCardColumns, preferenceCardColumns).hashCode()

    override fun toString(): String =
        "PanelLayout(width=$width, fill=$fill, maxHeight=$maxHeight, pictureHeight=$pictureHeight, " +
            "layerCardColumns=$layerCardColumns, preferenceCardColumns=$preferenceCardColumns)"
}
