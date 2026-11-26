package com.mountaya.elements

/**
 * MarkerSprite is what a marker sprite draws, the image a waypoint's or a point of interest's symbol
 * layer names for a glyph in a colour (`map.icons.marker-sprite`): a square colour image the style never
 * recolours, a disc at its centre under a border, and the glyph fitted to the square half the image wide
 * at its centre.
 *
 * Every length is in the image's pixels, density-independent ones, which a renderer draws at its screen's
 * density, so a sprite is the same size on every screen. Two are equal where every member is the same,
 * each number as the same number and each colour by its characters.
 */
internal class MarkerSprite(
    /** The side of the square image. */
    val size: Double,
    /** The disc the glyph stands on. */
    val disc: Disc,
    /** Where the glyph is drawn, painted as its set paints it. */
    val glyph: GlyphFit,
) {
    /** Disc is the disc a sprite's glyph stands on. */
    class Disc(
        /** The disc's centre, `[x, y]` from the image's top left corner. */
        val center: List<Double>,
        /** The disc's radius, to the middle of its border. */
        val radius: Double,
        /** The feature's colour, as given, which the disc is filled with. */
        val fill: String,
        /** The colour of the border drawn around the disc, centred on its edge. */
        val borderColor: String,
        /** The border's width. */
        val borderWidth: Double,
    ) {
        override fun equals(other: Any?): Boolean =
            other is Disc &&
                other.center == center &&
                other.radius.compareTo(radius) == 0 &&
                other.fill == fill &&
                other.borderColor == borderColor &&
                other.borderWidth.compareTo(borderWidth) == 0

        override fun hashCode(): Int {
            var hash = center.hashCode()
            hash = 31 * hash + radius.hashCode()
            hash = 31 * hash + fill.hashCode()
            hash = 31 * hash + borderColor.hashCode()
            return 31 * hash + borderWidth.hashCode()
        }

        override fun toString(): String =
            "Disc(center=$center, radius=$radius, fill=$fill, borderColor=$borderColor, borderWidth=$borderWidth)"
    }

    /**
     * GlyphFit is where a sprite's glyph is drawn, and in what colour: the set's view box starts [offset]
     * pixels from the image's left and top edges, and one of its units is [scale] pixels, so a set drawn on
     * any grid fills the same square.
     */
    class GlyphFit(
        /** How far the glyph's square starts from the image's left and top edges. */
        val offset: Double,
        /** How many pixels one unit of the set's view box is drawn as. */
        val scale: Double,
        /** The colour the glyph's current colour is. */
        val color: String,
    ) {
        override fun equals(other: Any?): Boolean =
            other is GlyphFit &&
                other.offset.compareTo(offset) == 0 &&
                other.scale.compareTo(scale) == 0 &&
                other.color == color

        override fun hashCode(): Int = (offset.hashCode() * 31 + scale.hashCode()) * 31 + color.hashCode()

        override fun toString(): String = "GlyphFit(offset=$offset, scale=$scale, color=$color)"
    }

    override fun equals(other: Any?): Boolean =
        other is MarkerSprite && other.size.compareTo(size) == 0 && other.disc == disc && other.glyph == glyph

    override fun hashCode(): Int = (size.hashCode() * 31 + disc.hashCode()) * 31 + glyph.hashCode()

    override fun toString(): String = "MarkerSprite(size=$size, disc=$disc, glyph=$glyph)"
}
