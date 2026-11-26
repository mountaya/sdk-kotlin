package com.mountaya.elements

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mountaya.geo.GlyphDrawing
import com.mountaya.geo.HexColor
import com.mountaya.geo.ThemeScheme
import com.mountaya.geo.generated.ElementTokens
import com.mountaya.geo.parseGlyph
import com.mountaya.geo.themeTokenValue
import kotlin.math.min

/**
 * CompassRose draws Mountaya's compass rose, the contract's `compass-rose` drawing, in a square of [size]: its disc in
 * `control-bg` and its letter in `control-fg` under the scheme in force, its needle in the colour the drawing fixes,
 * turned against [bearing] so it keeps pointing north. A bearing of 90 turns the map clockwise, so the rose turns
 * anticlockwise. Accessibility services read it as nothing: the control it stands in names itself.
 */
@Composable
internal fun CompassRose(
    bearing: Double,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
) {
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val drawing = GlyphDrawing.artwork["compass-rose"] ?: return
    val box = drawing.viewBox
    if (box.size != 4 || box[2] <= 0.0 || box[3] <= 0.0) return
    val figures =
        remember(drawing) {
            drawing.shapes.flatMap { shape ->
                parseGlyph(shape.markup).mapNotNull { parsed ->
                    shapePath(parsed)?.let { path ->
                        Triple(path, svgTransform(parsed.attributes["transform"].orEmpty()), shape)
                    }
                }
            }
        }
    Canvas(modifier.size(size).rotate(-bearing.toFloat()).clearAndSetSemantics {}) {
        val fit = min(this.size.width / box[2].toFloat(), this.size.height / box[3].toFloat())
        scale(fit, fit, pivot = Offset.Zero) {
            translate(-box[0].toFloat(), -box[1].toFloat()) {
                for ((path, placed, shape) in figures) {
                    withTransform({ transform(placed) }) {
                        shape.fill?.let { drawPath(path, compassPaintBrush(it, tokens, scheme), style = Fill) }
                        val stroke = shape.stroke
                        val width = shape.strokeWidth
                        if (stroke != null && width != null) {
                            drawPath(
                                path,
                                compassPaintBrush(stroke, tokens, scheme),
                                style = Stroke(width = width.toFloat()),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** The colour [paint] reads: the one the drawing fixes, or its token's under [scheme], at the paint's opacity. */
internal fun compassPaintColor(
    paint: GlyphDrawing.Paint,
    tokens: ElementTokens,
    scheme: ThemeScheme,
): Color =
    when (paint) {
        is GlyphDrawing.Paint.Color -> {
            hexColor(paint.color)
        }

        is GlyphDrawing.Paint.Token -> {
            val value = themeTokenValue(paint.token.get(tokens), scheme)
            hexColor(HexColor(value.hex, value.opacity * paint.opacity))
        }

        // A gradient reads its first stop as one colour, where a colour alone is asked for.
        is GlyphDrawing.Paint.LinearGradient -> {
            paint.stops.firstOrNull()?.let { hexColor(it.color) } ?: Color.Transparent
        }
    }

/**
 * The brush [paint] draws with: a gradient along its two ends through its stops, in the drawing's units, or the one
 * colour [compassPaintColor] reads.
 */
internal fun compassPaintBrush(
    paint: GlyphDrawing.Paint,
    tokens: ElementTokens,
    scheme: ThemeScheme,
): Brush =
    when (paint) {
        is GlyphDrawing.Paint.LinearGradient -> {
            Brush.linearGradient(
                colorStops = paint.stops.map { it.offset.toFloat() to hexColor(it.color) }.toTypedArray(),
                start = Offset(paint.from[0].toFloat(), paint.from[1].toFloat()),
                end = Offset(paint.to[0].toFloat(), paint.to[1].toFloat()),
            )
        }

        else -> {
            SolidColor(compassPaintColor(paint, tokens, scheme))
        }
    }
