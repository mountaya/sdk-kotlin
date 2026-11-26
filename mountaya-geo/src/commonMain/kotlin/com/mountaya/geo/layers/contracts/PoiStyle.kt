package com.mountaya.geo

/**
 * PoiStyle is how a point of interest of one category is drawn (`geo.layers.poi-style`): the [glyph]
 * its pin carries, a name of the marker set (`GlyphSet.marker`), and the [color] the pin is drawn in.
 * Several categories share a glyph, a pharmacy and a hospital the medical cross, each told apart by its
 * colour.
 *
 * Two are equal where they name the same glyph and the same colour, the opacity as the same number,
 * and equal styles hash alike.
 */
public class PoiStyle(
    /** The name of the glyph the pin carries. */
    public val glyph: String,
    /** The colour the pin is drawn in. */
    public val color: HexColor,
) {
    override fun equals(other: Any?): Boolean = other is PoiStyle && other.glyph == glyph && other.color == color

    override fun hashCode(): Int = glyph.hashCode() * 31 + color.hashCode()

    override fun toString(): String = "PoiStyle(glyph=$glyph, color=$color)"
}
