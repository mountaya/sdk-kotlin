package com.mountaya.elements

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.net.Uri
import android.view.View
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.setFrom
import androidx.compose.ui.graphics.toArgb
import com.mountaya.geo.GlyphDrawing
import com.mountaya.geo.MapWatermark
import com.mountaya.geo.parseGlyph

/** The ratio of a blur's radius to the standard deviation of its Gaussian, as Skia relates the two. */
private const val BLUR_SIGMA_PER_RADIUS: Float = 0.57735f

/** The part of a blur's standard deviation Skia adds whatever its radius. */
private const val BLUR_SIGMA_FLOOR: Float = 0.5f

/**
 * WatermarkView is the Mountaya watermark a map draws over its canvas (`geo.organization.watermark`), drawn natively
 * from the drawing [mark] names, its path data, its gradients, its opacity and its drop shadow, clipped to its box: the
 * shadow is its silhouette blurred by a Gaussian, painted beneath the shapes and never over them, and the whole is laid
 * over the map at the drawing's opacity. It is drawn once into an image of its size, so a frame of the map draws it as
 * the image it is, with no work of its own. The image is let go when the view leaves its window and drawn again when it
 * comes back at the size it kept, so a view the renderer takes off and lays again, as it does on every change of its
 * pins, never loses its mark.
 *
 * A press opens [mark]'s link through [onPress], outside the application, and accessibility services read it as a link
 * labelled [label]. It is placed by the renderer, above the markers and beneath the label a marker shows, raised as high
 * as a pin and casting no shadow of the platform's, its outline none.
 */
@SuppressLint("ViewConstructor")
internal class WatermarkView(
    context: Context,
    /** The watermark, its drawing and its frame. */
    val mark: MapWatermark,
    label: String,
    onPress: (String) -> Unit,
) : View(context) {
    /** Each shape's outline in the drawing's units, and the paint it is filled with, in drawing order. */
    private val figures: List<Pair<Path, GlyphDrawing.Paint?>> =
        mark.drawing.shapes.flatMap { shape ->
            parseGlyph(shape.markup).mapNotNull { parsed ->
                val path = shapePath(parsed)?.asAndroidPath() ?: return@mapNotNull null
                val placed =
                    android.graphics.Matrix().apply {
                        setFrom(
                            svgTransform(parsed.attributes["transform"].orEmpty()),
                        )
                    }
                Path(path).apply { transform(placed) } to shape.fill
            }
        }

    /** The watermark drawn at the view's size, or null before it is laid out and while it is out of its window. */
    private var image: Bitmap? = null

    private val laid =
        Paint(Paint.FILTER_BITMAP_FLAG).apply {
            alpha =
                (mark.drawing.opacity * 255).toInt().coerceIn(0, 255)
        }

    init {
        contentDescription = label
        isClickable = true
        isFocusable = true
        setOnClickListener { onPress(mark.link) }
        // The drawing carries its own shadow, so the view casts none of the platform's at the height it is raised to.
        outlineProvider = null
    }

    override fun onSizeChanged(
        width: Int,
        height: Int,
        oldWidth: Int,
        oldHeight: Int,
    ) {
        image?.recycle()
        image = if (width > 0 && height > 0) drawn(width, height) else null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // A view laid again keeps the size it had, so its size is told no change, and its image is drawn here.
        if (image == null && width > 0 && height > 0) image = drawn(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        image?.let { canvas.drawBitmap(it, 0f, 0f, laid) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        image?.recycle()
        image = null
    }

    /** The drawing at [width] by [height] pixels: its shadow outside its shapes, then its shapes, clipped to its box. */
    private fun drawn(
        width: Int,
        height: Int,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val box = mark.drawing.viewBox
        canvas.scale(width / box[2].toFloat(), height / box[3].toFloat())
        canvas.translate(-box[0].toFloat(), -box[1].toFloat())
        canvas.clipRect(box[0].toFloat(), box[1].toFloat(), (box[0] + box[2]).toFloat(), (box[1] + box[3]).toFloat())
        val silhouette = Path().apply { for ((path, _) in figures) addPath(path) }
        mark.drawing.shadow?.let { shadow ->
            val sigma = shadow.blur.toFloat()
            val blurred =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = hexColor(shadow.color).toArgb()
                    if (sigma > BLUR_SIGMA_FLOOR) {
                        maskFilter =
                            BlurMaskFilter(
                                (sigma - BLUR_SIGMA_FLOOR) / BLUR_SIGMA_PER_RADIUS,
                                BlurMaskFilter.Blur.NORMAL,
                            )
                    }
                }
            canvas.save()
            canvas.translate(shadow.dx.toFloat(), shadow.dy.toFloat())
            canvas.drawPath(silhouette, blurred)
            canvas.restore()
            // The shadow stands beneath the shapes and never over them, as the drawing's filter cuts it out.
            canvas.drawPath(
                silhouette,
                Paint(Paint.ANTI_ALIAS_FLAG).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) },
            )
        }
        for ((path, fill) in figures) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
            when (fill) {
                null -> {
                    continue
                }

                is GlyphDrawing.Paint.Color -> {
                    paint.color = hexColor(fill.color).toArgb()
                }

                is GlyphDrawing.Paint.Token -> {
                    paint.color =
                        hexColor(fill.token.get(com.mountaya.geo.generated.ElementTokens.standard).light).toArgb()
                    paint.alpha = (paint.alpha * fill.opacity).toInt().coerceIn(0, 255)
                }

                is GlyphDrawing.Paint.LinearGradient -> {
                    paint.shader =
                        LinearGradient(
                            fill.from[0].toFloat(),
                            fill.from[1].toFloat(),
                            fill.to[0].toFloat(),
                            fill.to[1].toFloat(),
                            fill.stops.map { hexColor(it.color).toArgb() }.toIntArray(),
                            fill.stops.map { it.offset.toFloat() }.toFloatArray(),
                            Shader.TileMode.CLAMP,
                        )
                }
            }
            canvas.drawPath(path, paint)
        }
        return bitmap
    }
}

/**
 * watermarkViewOf answers the watermark [mark] a map draws in [context], labelled [label], whose press opens its link
 * outside the application through the platform's own opener, [openLinkOutside], and never through a handler the
 * composition provides, so no app redirects or swallows the mark's link.
 */
internal fun watermarkViewOf(
    context: Context,
    mark: MapWatermark,
    label: String,
): WatermarkView = WatermarkView(context, mark, label) { link -> openLinkOutside(context, Uri.parse(link)) }
