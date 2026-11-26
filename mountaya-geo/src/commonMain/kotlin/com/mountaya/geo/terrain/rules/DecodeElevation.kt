package com.mountaya.geo

/** A byte's last value plus one: the weight of a channel over the one after it. */
private const val CHANNEL: Int = 256

/** The lowest height Terrain-RGB writes, in meters. */
private const val MAPBOX_FLOOR: Double = -10_000.0

/** The meters Terrain-RGB counts per step. */
private const val MAPBOX_STEP: Double = 0.1

/** The offset Terrarium writes every height from, in meters. */
private const val TERRARIUM_OFFSET: Double = 32_768.0

/** The bits a packed pixel's red channel stands above. */
private const val RED_SHIFT: Int = 16

/** The bits a packed pixel's green channel stands above. */
private const val GREEN_SHIFT: Int = 8

/** The bits of one channel. */
private const val CHANNEL_MASK: Int = 0xFF

/**
 * decodeElevation reads the height, in meters, a pixel of an elevation tile stands for (`geo.terrain.elevation-decode`):
 * [pixel] is its channels packed as an `Int`, red in bits 16 to 23, green in 8 to 15 and blue in 0 to 7, as an
 * Android `Bitmap` hands a pixel, the alpha channel in the bits above them read by neither encoding.
 *
 * Under `mapbox`, which [encoding] reads as where it names none, the height is
 * -10000 + (R × 65536 + G × 256 + B) × 0.1; under `terrarium` it is R × 256 + G + B / 256 - 32768. An encoding is
 * compared whole, so any other reads no height, and null is the answer.
 */
public fun decodeElevation(
    pixel: Int,
    encoding: ElevationEncoding?,
): Double? {
    val red = (pixel shr RED_SHIFT) and CHANNEL_MASK
    val green = (pixel shr GREEN_SHIFT) and CHANNEL_MASK
    val blue = pixel and CHANNEL_MASK
    return when (encoding ?: ElevationEncoding.Mapbox) {
        ElevationEncoding.Mapbox -> {
            MAPBOX_FLOOR + ((red * CHANNEL + green) * CHANNEL + blue) * MAPBOX_STEP
        }

        ElevationEncoding.Terrarium -> {
            red * CHANNEL + green + blue / CHANNEL.toDouble() - TERRARIUM_OFFSET
        }

        else -> {
            null
        }
    }
}
