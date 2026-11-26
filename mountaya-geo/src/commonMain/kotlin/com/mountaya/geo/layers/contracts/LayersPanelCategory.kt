package com.mountaya.geo

/**
 * LayersPanelCategory is one classic category a layers picker's overlays section switches between
 * (`geo.layers.picker-rows`): its slug, which a pick in the switch hands back, and its name in the reader's language.
 *
 * Two are equal where both members are.
 */
public class LayersPanelCategory(
    /** The category's slug, as the catalogue spells it. */
    public val slug: String,
    /** The category's name in the reader's language, else in English, else its slug. */
    public val name: String,
) {
    override fun equals(other: Any?): Boolean = other is LayersPanelCategory && other.slug == slug && other.name == name

    override fun hashCode(): Int = slug.hashCode() * 31 + name.hashCode()

    override fun toString(): String = "LayersPanelCategory(slug=$slug, name=$name)"
}
