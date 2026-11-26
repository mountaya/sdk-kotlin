package com.mountaya.geo

/**
 * ShapePaint is how one shape of a glyph is drawn, as `glyphPaint` answers it: whether its inside is
 * filled and its outline stroked, each in the current colour, the colour the surface draws its glyph
 * in, and how a stroke is drawn.
 *
 * Two are equal where every member is, the numbers compared as every value of the geo layer compares
 * them, `NaN` the same as `NaN` and 0 another than -0, and equal values hash alike.
 */
public class ShapePaint(
    /** Whether the shape's inside is filled in the current colour. */
    public val isFilled: Boolean,
    /** Whether the shape's outline is stroked in the current colour. */
    public val isStroked: Boolean,
    /** The stroke's width, in units of the set's box. */
    public val strokeWidth: Double,
    /** How each end of a stroked line is drawn. */
    public val lineCap: LineCap,
    /** How a stroked line turns a corner. */
    public val lineJoin: LineJoin,
    /** How far a miter may reach, in stroke widths, before the corner is drawn bevelled instead. */
    public val miterLimit: Double,
    /** How a fill reads which parts of the shape are inside it. */
    public val fillRule: FillRule,
) {
    /** LineCap is how each end of a stroked line is drawn, by the keyword SVG spells it with. */
    public enum class LineCap(
        /** The cap as SVG spells it. */
        public val id: String,
    ) {
        /** An end cut square where the line ends. */
        Butt("butt"),

        /** An end rounded in a half disc as wide as the stroke, so a line of no length is a dot. */
        Round("round"),

        /** An end cut square half the stroke's width past where the line ends. */
        Square("square"),
    }

    /** LineJoin is how a stroked line turns a corner, by the keyword SVG spells it with. */
    public enum class LineJoin(
        /** The join as SVG spells it. */
        public val id: String,
    ) {
        /** A corner turned in arcs; a renderer that draws none draws a miter. */
        Arcs("arcs"),

        /** A corner cut off. */
        Bevel("bevel"),

        /** A sharp corner, up to the miter limit. */
        Miter("miter"),

        /** A sharp corner clipped at the miter limit; a renderer that draws no clipped miter draws a miter. */
        MiterClip("miter-clip"),

        /** A corner turned in an arc. */
        Round("round"),
    }

    /** FillRule is how a fill reads which parts of a shape are inside it, by the keyword SVG spells it with. */
    public enum class FillRule(
        /** The rule as SVG spells it. */
        public val id: String,
    ) {
        /** The reading of a point as inside where the outline winds around it. */
        NonZero("nonzero"),

        /** The reading of a point as inside where a line from it crosses the outline an odd number of times. */
        EvenOdd("evenodd"),
    }

    override fun equals(other: Any?): Boolean =
        other is ShapePaint &&
            other.isFilled == isFilled &&
            other.isStroked == isStroked &&
            other.strokeWidth.compareTo(strokeWidth) == 0 &&
            other.lineCap == lineCap &&
            other.lineJoin == lineJoin &&
            other.miterLimit.compareTo(miterLimit) == 0 &&
            other.fillRule == fillRule

    override fun hashCode(): Int {
        var hash = isFilled.hashCode()
        hash = 31 * hash + isStroked.hashCode()
        hash = 31 * hash + strokeWidth.hashCode()
        hash = 31 * hash + lineCap.hashCode()
        hash = 31 * hash + lineJoin.hashCode()
        hash = 31 * hash + miterLimit.hashCode()
        return 31 * hash + fillRule.hashCode()
    }

    override fun toString(): String =
        "ShapePaint(isFilled=$isFilled, isStroked=$isStroked, strokeWidth=$strokeWidth, lineCap=${lineCap.id}, " +
            "lineJoin=${lineJoin.id}, miterLimit=$miterLimit, fillRule=${fillRule.id})"
}
