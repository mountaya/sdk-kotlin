package com.mountaya.elements

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.mountaya.geo.GlyphSet
import com.mountaya.geo.ShapePaint
import com.mountaya.geo.glyphPaint
import com.mountaya.geo.parseGlyph

/**
 * glyphFigures answers the glyph [glyph] of [set] as a renderer draws it, a figure for each of its shapes
 * that draws anything, in drawing order, or null where the set does not hold the glyph.
 *
 * The markup is read through `parseGlyph`, never drawn as it was handed over, so a set built from a source
 * its host does not control draws no element and no attribute the glyph's allowlist does not keep. Each
 * shape is painted as its set paints it, its own fill, stroke, cap, join and miter limit replacing the
 * set's for it alone (`glyphPaint`).
 */
internal fun glyphFigures(
    glyph: String,
    set: GlyphSet,
): List<GlyphFigure>? {
    val markup = set.markup[glyph] ?: return null
    return parseGlyph(markup).mapNotNull { shape ->
        shapePath(shape)?.let { path ->
            val paint = glyphPaint(shape, set)
            path.fillType =
                if (paint.fillRule == ShapePaint.FillRule.EvenOdd) PathFillType.EvenOdd else PathFillType.NonZero
            GlyphFigure(path, svgTransform(shape.attributes["transform"].orEmpty()), paint)
        }
    }
}

/**
 * Draws [figures] in [color], each filled then stroked as its paint says, at the miter limit it states,
 * since Compose's default is the platform's and not SVG's.
 *
 * The scope is in the units of the set's box, its transform placing the box where the glyph is drawn; each
 * figure is drawn through its own transform on top of it.
 */
internal fun DrawScope.drawGlyphFigures(
    figures: List<GlyphFigure>,
    color: Color,
) {
    for (figure in figures) {
        withTransform({ transform(figure.transform) }) {
            val paint = figure.paint
            if (paint.isFilled) drawPath(figure.path, color, style = Fill)
            if (paint.isStroked) {
                drawPath(
                    figure.path,
                    color,
                    style =
                        Stroke(
                            width = paint.strokeWidth.toFloat(),
                            miter = paint.miterLimit.toFloat(),
                            cap = composeLineCap(paint.lineCap),
                            join = composeLineJoin(paint.lineJoin),
                        ),
                )
            }
        }
    }
}

/** The cap Compose draws for an SVG cap. */
internal fun composeLineCap(cap: ShapePaint.LineCap): StrokeCap =
    when (cap) {
        ShapePaint.LineCap.Butt -> StrokeCap.Butt
        ShapePaint.LineCap.Round -> StrokeCap.Round
        ShapePaint.LineCap.Square -> StrokeCap.Square
    }

/**
 * The join Compose draws for an SVG join: `arcs` and `miter-clip`, which it draws none of, as a miter, as SVG
 * asks a renderer that draws neither to.
 */
internal fun composeLineJoin(join: ShapePaint.LineJoin): StrokeJoin =
    when (join) {
        ShapePaint.LineJoin.Arcs, ShapePaint.LineJoin.Miter, ShapePaint.LineJoin.MiterClip -> StrokeJoin.Miter
        ShapePaint.LineJoin.Bevel -> StrokeJoin.Bevel
        ShapePaint.LineJoin.Round -> StrokeJoin.Round
    }
