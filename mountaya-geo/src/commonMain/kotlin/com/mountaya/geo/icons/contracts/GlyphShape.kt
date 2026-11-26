package com.mountaya.geo

/**
 * GlyphShape is one shape a renderer builds from an icon's markup: its element name, lowercase, and
 * the attributes it keeps, keyed lowercase in the order the markup first writes them, a group's
 * transform folded into its own.
 *
 * Two are equal where they hold the same tag and the same attributes, whatever their order.
 */
public class GlyphShape(
    /** The shape's element name, lowercase: `path`, `circle`, `rect` and the like. */
    public val tag: String,
    /** The attributes the shape keeps, each a member of the glyph attributes, keyed lowercase. */
    public val attributes: Map<String, String>,
) {
    override fun equals(other: Any?): Boolean =
        other is GlyphShape && other.tag == tag && other.attributes == attributes

    override fun hashCode(): Int = tag.hashCode() * 31 + attributes.hashCode()

    override fun toString(): String = "GlyphShape($tag, $attributes)"
}
