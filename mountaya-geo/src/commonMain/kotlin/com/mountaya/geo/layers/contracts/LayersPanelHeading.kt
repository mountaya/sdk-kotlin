package com.mountaya.geo

/**
 * LayersPanelHeading is what heads a section of a layers picker (`geo.layers.picker-rows`): a word of the elements'
 * `layers` section, by its dotted path as `ElementsScope.word` reads one, `layers.terrain` over the terrains,
 * `layers.enrichment` over the enrichments, `layers.overlays` over the classic overlays and `layers.time` over the time.
 * A classic category's own name heads no section: it names its segment of the overlays' category switch
 * (`LayersPanelCategory`).
 *
 * Two are equal where they read the same word.
 */
public class LayersPanelHeading(
    /** The path of the word that heads the section, its section and its key joined by a dot, `layers.terrain`. */
    public val word: String,
) {
    override fun equals(other: Any?): Boolean = other is LayersPanelHeading && other.word == word

    override fun hashCode(): Int = word.hashCode()

    override fun toString(): String = "LayersPanelHeading($word)"
}
