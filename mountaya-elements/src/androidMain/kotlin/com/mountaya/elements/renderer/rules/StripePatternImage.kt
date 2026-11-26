package com.mountaya.elements

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * stripePatternImage draws the stripe tile [pattern] describes, the image a stretch's pattern repeats along
 * it (`map.icons.stripe-pattern`): the tile filled with its ground, where it has one, and each stripe
 * stroked along its segment in its colour and width, with its ends, the halves that run past the tile's
 * edges cut at them.
 *
 * The image is the tile's size in density-independent pixels, drawn at the screen's density, [densityDpi],
 * which the bitmap states, and never a signed distance field MapLibre would recolour.
 */
internal fun stripePatternImage(
    pattern: StripePattern,
    densityDpi: Int,
): Bitmap =
    densityImage(pattern.size, densityDpi) {
        pattern.ground?.let(::cssColor)?.let { drawRect(it) }
        val color = cssColor(pattern.color) ?: return@densityImage
        val stripes = Path()
        for (segment in pattern.segments) {
            val from = segment.getOrNull(0)?.takeIf { it.size == 2 } ?: continue
            val to = segment.getOrNull(1)?.takeIf { it.size == 2 && segment.size == 2 } ?: continue
            stripes.moveTo(from[0].toFloat(), from[1].toFloat())
            stripes.lineTo(to[0].toFloat(), to[1].toFloat())
        }
        // A stripe running past an edge is cut there, by the tile's own bounds.
        drawPath(stripes, color, style = Stroke(width = pattern.width.toFloat(), cap = composeLineCap(pattern.lineCap)))
    }
