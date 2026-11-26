package com.mountaya.geo

/**
 * The cap a set draws its lines with, under either paint: round, so a dot drawn as a stroke a hundredth
 * of a unit long is a dot, where butt caps draw nothing.
 */
internal val glyphLineCap: ShapePaint.LineCap = ShapePaint.LineCap.Round

/** The join a set draws its lines with, under either paint: round, as its caps are. */
internal val glyphLineJoin: ShapePaint.LineJoin = ShapePaint.LineJoin.Round

/**
 * The miter limit a shape that states none is drawn with: SVG's own, 4. A platform's drawing API may
 * default to another, CoreGraphics' to 10, so a renderer states it rather than inheriting its platform's.
 */
internal const val GLYPH_MITER_LIMIT: Double = 4.0

/** The fill rule a shape that states none is drawn with: SVG's own, non-zero. */
internal val glyphFillRule: ShapePaint.FillRule = ShapePaint.FillRule.NonZero

/**
 * glyphPaint answers how [shape], one shape of a glyph of [set], is drawn.
 *
 * A fill set fills each shape in the current colour and strokes none; a stroke set strokes each shape in
 * the current colour and fills none. Either draws a stroke at the set's width, with round caps and joins,
 * at SVG's miter limit of 4, and fills by the non-zero rule. What the shape states of its own replaces the
 * set's for that shape alone: its `fill` and its `stroke`, `currentColor` or `none`, its `stroke-linecap`,
 * its `stroke-linejoin`, its `stroke-miterlimit` and its `fill-rule`, each read without regard to letter
 * case. A value `parseGlyph` would drop is no statement, whoever built the shape, and a shape never
 * states a width: the width is the set's. So a fill set strokes a shape that asks for a stroke of its own
 * at the set's width with round ends, and a stroke set strokes a dot that fills itself around its edge as
 * well, unless the dot states a stroke of `none` (`geo.icons.glyph-paint`).
 *
 * A shape that states nothing answers the set's own paint, which a renderer may state once on whatever
 * holds every shape of the glyph.
 */
public fun glyphPaint(
    shape: GlyphShape,
    set: GlyphSet,
): ShapePaint {
    val fills = set.paint == GlyphSet.Paint.Fill

    // What the shape states of its own, as the parse keeps it, its letters folded.
    fun stated(name: String): String? = shape.attributes[name]?.let { keptGlyphValue(name, it) }?.let(::asciiLowercase)

    val fillRule = shape.attributes["fill-rule"]?.let { asciiLowercase(trimWhiteSpace(it)) }
    return ShapePaint(
        isFilled = stated("fill")?.let { it != "none" } ?: fills,
        isStroked = stated("stroke")?.let { it != "none" } ?: !fills,
        strokeWidth = set.strokeWidth,
        lineCap =
            stated("stroke-linecap")?.let { cap -> ShapePaint.LineCap.entries.firstOrNull { it.id == cap } }
                ?: glyphLineCap,
        lineJoin =
            stated("stroke-linejoin")?.let { join -> ShapePaint.LineJoin.entries.firstOrNull { it.id == join } }
                ?: glyphLineJoin,
        miterLimit = stated("stroke-miterlimit")?.let(::plainNumberValue) ?: GLYPH_MITER_LIMIT,
        fillRule = if (fillRule == ShapePaint.FillRule.EvenOdd.id) ShapePaint.FillRule.EvenOdd else glyphFillRule,
    )
}
