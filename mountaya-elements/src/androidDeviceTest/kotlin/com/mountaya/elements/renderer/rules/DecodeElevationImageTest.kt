package com.mountaya.elements

import android.graphics.Bitmap
import android.graphics.Color
import com.mountaya.geo.ElevationTile
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** An elevation tile's image read into its pixels by the platform's own codec. */
class DecodeElevationImageTest {
    private val address = ElevationTile.Address(16, 34017, 23335)

    private fun png(
        width: Int,
        height: Int,
        color: Int,
    ): ByteArray {
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
        return ByteArrayOutputStream().also { image.compress(Bitmap.CompressFormat.PNG, 100, it) }.toByteArray()
    }

    @Test
    fun aPngIsReadIntoItsChannelsAsWritten() {
        val tile =
            assertNotNull(decodeElevationImage(png(4, 4, Color.rgb(1, 134, 160)), address, 4), "the image decodes")

        assertEquals(address, tile.address)
        assertEquals(4, tile.size)
        assertEquals(16, tile.pixels.size)
        assertEquals(Color.rgb(1, 134, 160) and 0xffffff, tile.pixels.first() and 0xffffff)
    }

    @Test
    fun bytesThatAreNoImageOrNoSquareReadNothing() {
        assertNull(decodeElevationImage(byteArrayOf(1, 2, 3), address, 4))
        assertNull(decodeElevationImage(png(4, 2, Color.BLACK), address, 4))
    }
}
