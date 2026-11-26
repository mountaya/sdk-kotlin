package com.mountaya.geo

/**
 * LayersPanelRow is one entry a layers picker lists, a terrain or an overlay (`geo.layers.picker-rows`): its
 * slug, its name in the reader's language, whether the selection holds it, and the mark it shows.
 *
 * Two are equal where every member is.
 */
public class LayersPanelRow(
    /** The entry's slug, which a press hands on. */
    public val slug: String,
    /** The entry's name in the reader's language, else its English name, else its slug. */
    public val label: String,
    /** Whether the selection holds the entry. */
    public val isChecked: Boolean,
    /**
     * What the row shows beside its label: its lock where the entry is out of the viewer's reach, a tick where
     * the selection holds it, and nothing otherwise.
     */
    public val mark: LayersPanelMark?,
) {
    override fun equals(other: Any?): Boolean =
        other is LayersPanelRow &&
            other.slug == slug &&
            other.label == label &&
            other.isChecked == isChecked &&
            other.mark == mark

    override fun hashCode(): Int =
        ((slug.hashCode() * 31 + label.hashCode()) * 31 + isChecked.hashCode()) * 31 + mark.hashCode()

    override fun toString(): String = "LayersPanelRow(slug=$slug, label=$label, isChecked=$isChecked, mark=$mark)"
}
