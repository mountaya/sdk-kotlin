package com.mountaya.elements

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.mountaya.geo.ElevationTile

/**
 * decodeElevationImage reads [bytes], an elevation tile at [address], a WebP or a PNG image, into its pixels through the
 * platform's own codec, `BitmapFactory`, with no library of its own (`geo.terrain.elevation-sample`): each pixel's red,
 * green and blue as they are written, never multiplied by its alpha, which a height is not. The source's [size] is the
 * edge the catalogue declares, and an image of another edge is read at its own, square.
 *
 * Null where the bytes hold no image the device reads, or one that is not square.
 */
internal fun decodeElevationImage(
    bytes: ByteArray,
    address: ElevationTile.Address,
    size: Int,
): ElevationTile? {
    val options =
        BitmapFactory.Options().apply {
            inPremultiplied = false
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
    val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: return null
    try {
        val edge = image.width
        if (edge <= 0 || image.height != edge) return null
        val pixels = IntArray(edge * edge)
        image.getPixels(pixels, 0, edge, 0, 0, edge, edge)
        return ElevationTile(address, if (edge == size) size else edge, pixels)
    } finally {
        image.recycle()
    }
}
