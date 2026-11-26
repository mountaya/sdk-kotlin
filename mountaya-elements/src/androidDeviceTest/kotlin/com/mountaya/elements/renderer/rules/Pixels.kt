package com.mountaya.elements

import android.graphics.Bitmap
import android.util.DisplayMetrics
import kotlin.math.abs

/** Pixels reads an image a test drew, pixel by pixel, as its colour channels from 0 to 255. */
internal object Pixels {
    /** Pixel is one pixel's red, green, blue and alpha, each from 0 to 255, its colour divided out of its alpha. */
    class Pixel(
        val red: Int,
        val green: Int,
        val blue: Int,
        val alpha: Int,
    ) {
        /** Whether every channel is within [tolerance] of [other]'s. */
        fun isNear(
            other: Pixel,
            tolerance: Int = 8,
        ): Boolean =
            abs(red - other.red) <= tolerance &&
                abs(green - other.green) <= tolerance &&
                abs(blue - other.blue) <= tolerance &&
                abs(alpha - other.alpha) <= tolerance

        override fun toString(): String = "rgba($red, $green, $blue, $alpha)"
    }

    val white = Pixel(255, 255, 255, 255)

    /**
     * The pixel of [bitmap] under the point ([x], [y]), in density-independent pixels from its top left corner,
     * [scale] pixels each, the density the bitmap states unless another is given, or null where the point is
     * outside it. Android answers a pixel with its colour divided out of its alpha.
     */
    fun pixel(
        bitmap: Bitmap,
        x: Double,
        y: Double,
        scale: Double = bitmap.density.toDouble() / DisplayMetrics.DENSITY_DEFAULT,
    ): Pixel? {
        val column = (x * scale).toInt()
        val row = (y * scale).toInt()
        if (column !in 0 until bitmap.width || row !in 0 until bitmap.height) return null
        val argb = bitmap.getPixel(column, row)
        return Pixel(argb shr 16 and 0xff, argb shr 8 and 0xff, argb and 0xff, argb ushr 24)
    }
}
