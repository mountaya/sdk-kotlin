package com.mountaya.geo

/**
 * LayersPanelChoice is the choice a section of a layers picker makes (`geo.layers.picker-rows`), carrying the
 * contract's spelling: one base map among the catalogue's terrains, each enrichment on or off by itself, one classic
 * overlay among every classic category's at once, or the moment the time-aware overlays show.
 */
public enum class LayersPanelChoice(
    /** The choice as `geo/layers-picker-rows.json` spells it. */
    public val id: String,
) {
    /** One base map among the catalogue's terrains, drawn as picture cards. */
    Terrain("terrain"),

    /** One classic overlay among those of every classic category, drawn as picture cards a category at a time. */
    Overlay("overlay"),

    /** Each enrichment of a category whose overlays stack, on or off by itself, drawn as a switch. */
    Enrichment("enrichment"),

    /**
     * The moment the time-aware overlays show, pinned on a slider (`geo.layers.time-slider`): a section with no row,
     * listed only where the host offers the time.
     */
    Time("time"),
}
