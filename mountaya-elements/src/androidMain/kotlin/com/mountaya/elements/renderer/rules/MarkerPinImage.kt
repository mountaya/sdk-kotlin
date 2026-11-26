package com.mountaya.elements

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.mountaya.geo.GlyphSet
import com.mountaya.geo.HexColor

/**
 * markerPinImage draws the pin [pin] describes, how a marker given no view of its own is shown
 * (`map.marker.pin`): a disc its diameter across in its fill, [accent] at its opacity where it states none, its
 * ring drawn inside that diameter, and at its centre its glyph, drawn from [glyphs] and fitted to the glyph's
 * square, or its dot.
 *
 * The image is the pin's diameter in density-independent pixels, drawn at the screen's density, [densityDpi],
 * which the bitmap states, so a pin is the same size on every screen. A fill that is no CSS hexadecimal colour
 * fills no disc, as a marker sprite's does, and the ring and the centre are drawn all the same. The glyph is
 * painted as its set paints it, each figure filled then stroked at the set's width scaled with the glyph.
 */
internal fun markerPinImage(
    pin: MarkerPin,
    accent: HexColor,
    glyphs: GlyphSet,
    densityDpi: Int,
): Bitmap =
    densityImage(pin.diameter, densityDpi) {
        val side = pin.diameter.toFloat()
        val center = Offset(side / 2f, side / 2f)
        val fill = pin.fill
        (
            if (fill !=
                null
            ) {
                cssColor(fill)
            } else {
                cssColor(accent.hex, accent.opacity)
            }
        )?.let { drawCircle(it, side / 2f, center) }
        // The ring lies within the diameter, as a border drawn inside its box.
        val ring = pin.ringWidth.toFloat()
        if (ring > 0f) {
            cssColor(pin.ringColor)?.let { drawCircle(it, (side - ring) / 2f, center, style = Stroke(width = ring)) }
        }
        when (val centre = pin.centre) {
            is MarkerPin.Centre.Glyph -> {
                val color = cssColor(centre.color) ?: return@densityImage
                val figures = glyphFigures(centre.glyph, glyphs) ?: return@densityImage
                val square = centre.size.toFloat()
                val fit = square / glyphs.viewBox.toFloat()
                translate((side - square) / 2f, (side - square) / 2f) {
                    scale(fit, fit, pivot = Offset.Zero) { drawGlyphFigures(figures, color) }
                }
            }

            is MarkerPin.Centre.Dot -> {
                cssColor(centre.color)?.let { drawCircle(it, centre.size.toFloat() / 2f, center) }
            }
        }
    }
