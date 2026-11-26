package com.mountaya.elements

import android.graphics.Bitmap
import android.util.DisplayMetrics
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * markerSpriteImage draws the marker sprite [sprite] describes, its glyph's [figures] fitted to the square at
 * its centre, as a colour image MapLibre takes as it is (`map.icons.marker-sprite`).
 *
 * The image is the sprite's size in density-independent pixels, drawn at the screen's density, [densityDpi],
 * which the bitmap states so MapLibre shows it that size on every screen, and it is never a signed distance
 * field MapLibre would recolour. The disc is filled with the feature's colour and stroked with its border,
 * centred on its edge; a colour that is no CSS hexadecimal colour fills no disc. The glyph is drawn over it
 * in the glyph's colour, each figure filled then stroked as its paint says, at the set's width scaled with
 * the glyph.
 */
internal fun markerSpriteImage(
    sprite: MarkerSprite,
    figures: List<GlyphFigure>,
    densityDpi: Int,
): Bitmap =
    densityImage(sprite.size, densityDpi) {
        val disc = sprite.disc
        val center = Offset(disc.center.getOrElse(0) { 0.0 }.toFloat(), disc.center.getOrElse(1) { 0.0 }.toFloat())
        val radius = disc.radius.toFloat()
        cssColor(disc.fill)?.let { drawCircle(it, radius, center) }
        cssColor(
            disc.borderColor,
        )?.let { drawCircle(it, radius, center, style = Stroke(width = disc.borderWidth.toFloat())) }
        val color = cssColor(sprite.glyph.color) ?: return@densityImage
        val fit = sprite.glyph
        translate(fit.offset.toFloat(), fit.offset.toFloat()) {
            scale(fit.scale.toFloat(), fit.scale.toFloat(), pivot = Offset.Zero) { drawGlyphFigures(figures, color) }
        }
    }

/**
 * A square image [side] density-independent pixels wide at [densityDpi], its bitmap stating that density so
 * MapLibre reads its pixel ratio off it, and [draw] drawn on it in density-independent pixels.
 */
internal fun densityImage(
    side: Double,
    densityDpi: Int,
    draw: DrawScope.() -> Unit,
): Bitmap {
    val density = densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT
    val pixels = max(1, (side * density).roundToInt())
    val bitmap = Bitmap.createBitmap(pixels, pixels, Bitmap.Config.ARGB_8888)
    bitmap.density = densityDpi
    val image = bitmap.asImageBitmap()
    val drawn = pixels.toFloat()
    CanvasDrawScope().draw(Density(density), LayoutDirection.Ltr, Canvas(image), Size(drawn, drawn)) {
        scale(drawn / side.toFloat(), drawn / side.toFloat(), pivot = Offset.Zero) { draw() }
    }
    return image.asAndroidBitmap()
}
