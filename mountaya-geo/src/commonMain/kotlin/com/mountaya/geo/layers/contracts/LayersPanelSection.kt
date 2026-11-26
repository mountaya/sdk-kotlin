package com.mountaya.geo

/**
 * LayersPanelSection is one section of a layers picker (`geo.layers.picker-rows`): what heads it, the choice its rows
 * make, the catalogue category it lists, the classic categories its switch offers, and its rows, in the catalogue's
 * order.
 *
 * Two are equal where every member is.
 */
public class LayersPanelSection(
    /** What heads the section. */
    public val heading: LayersPanelHeading,
    /** The choice the section's rows make. */
    public val choice: LayersPanelChoice,
    /**
     * The slug of the catalogue category the section lists: the enrichment category for the enrichments, the category
     * the overlays open on for the overlays, and null for the terrains and the time.
     */
    public val category: String?,
    /** The section's entries, in the catalogue's order: none for the time, and the open category's for the overlays. */
    public val rows: List<LayersPanelRow>,
    /**
     * The classic categories the overlays section switches between, each holding an overlay, in the catalogue's order,
     * a switch drawn where it lists two or more; none for every other section.
     */
    public val categories: List<LayersPanelCategory> = emptyList(),
) {
    override fun equals(other: Any?): Boolean =
        other is LayersPanelSection &&
            other.heading == heading &&
            other.choice == choice &&
            other.category == category &&
            other.rows == rows &&
            other.categories == categories

    override fun hashCode(): Int =
        (((heading.hashCode() * 31 + choice.hashCode()) * 31 + category.hashCode()) * 31 + rows.hashCode()) * 31 +
            categories.hashCode()

    override fun toString(): String =
        "LayersPanelSection(heading=$heading, choice=$choice, category=$category, rows=$rows, categories=$categories)"
}
