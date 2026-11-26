package com.mountaya.elements

import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import com.mountaya.geo.GlyphSet
import com.mountaya.geo.HexColor
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** A marker's pin as an image, drawn on the device at its screen's density. */
class MarkerPinImageTest {
    private val accent = Pixels.Pixel(0x1c, 0x7e, 0xd6, 255)
    private val red = Pixels.Pixel(0xe0, 0x31, 0x31, 255)

    private val densityDpi: Int =
        InstrumentationRegistry
            .getInstrumentation()
            .targetContext.resources.displayMetrics.densityDpi

    /** A set whose one glyph fills its whole box, which shows where a glyph is drawn. */
    private val block =
        assertNotNull(
            GlyphSet.of(
                "block",
                GlyphSet.Paint.Fill,
                markup =
                    mapOf("block" to """<rect width="24" height="24"/>"""),
            ),
            "the block set is a set",
        )

    /** The pin of a marker in [color] naming [glyph], of [set], on the map's accent. */
    private fun pin(
        color: String?,
        glyph: String?,
        set: GlyphSet = GlyphSet.marker,
    ): Bitmap = markerPinImage(describeMarkerPin(color, glyph, set), HexColor("#1c7ed6"), set, densityDpi)

    private fun Bitmap.at(
        x: Double,
        y: Double,
    ): Pixels.Pixel = assertNotNull(Pixels.pixel(this, x, y), "($x, $y) is outside the image")

    @Test
    fun aPinIs28DensityIndependentPixelsSquareAtTheScreensDensityAColourImage() {
        val image = pin(null, null)
        val density = densityDpi / 160.0

        assertEquals(densityDpi, image.density)
        assertTrue(density >= 1.0)
        assertEquals((28 * density).roundToInt(), image.width)
        assertEquals(image.width, image.height)
        assertEquals(Bitmap.Config.ARGB_8888, image.config)
    }

    @Test
    fun aPinNamingNoColourIsTheAccentInsideItsWhiteRingAWhiteDotAtItsCentre() {
        val image = pin(null, null)

        // The ring's middle, a pixel and a half in from the edge: white.
        assertTrue(image.at(14.0, 1.5).isNear(Pixels.white), "${image.at(14.0, 1.5)}")
        assertTrue(image.at(26.5, 14.0).isNear(Pixels.white), "${image.at(26.5, 14.0)}")
        // Inside the ring, outside the dot 8 pixels across: the accent.
        assertTrue(image.at(14.0, 6.0).isNear(accent), "${image.at(14.0, 6.0)}")
        assertTrue(image.at(8.0, 14.0).isNear(accent), "${image.at(8.0, 14.0)}")
        // The dot.
        assertTrue(image.at(14.0, 14.0).isNear(Pixels.white), "${image.at(14.0, 14.0)}")
        // A corner, outside the disc: nothing.
        assertEquals(0, image.at(0.5, 0.5).alpha)
        assertEquals(0, image.at(27.5, 27.5).alpha)
    }

    @Test
    fun aColourFillsTheDiscAndAGlyphTheSetDrawsStandsWhiteInThe16PixelSquare() {
        val image = pin("#e03131", "block", block)

        // The glyph fills its square, from 6 to 22 on each axis, whatever its set's grid.
        assertTrue(image.at(14.0, 14.0).isNear(Pixels.white), "${image.at(14.0, 14.0)}")
        assertTrue(image.at(7.0, 7.0).isNear(Pixels.white), "${image.at(7.0, 7.0)}")
        assertTrue(image.at(21.0, 21.0).isNear(Pixels.white), "${image.at(21.0, 21.0)}")
        // Between the ring and the square: the marker's colour.
        assertTrue(image.at(14.0, 4.5).isNear(red), "${image.at(14.0, 4.5)}")
        assertTrue(image.at(4.5, 14.0).isNear(red), "${image.at(4.5, 14.0)}")
    }

    @Test
    fun aGlyphTheSetCannotDrawGivesWayToTheDotInTheMarkersColour() {
        val image = pin("#e03131", "no-such-glyph")

        assertTrue(image.at(14.0, 14.0).isNear(Pixels.white), "${image.at(14.0, 14.0)}")
        assertTrue(image.at(14.0, 8.0).isNear(red), "${image.at(14.0, 8.0)}")
    }

    @Test
    fun aFillThatIsNoColourFillsNoDiscItsRingAndItsCentreDrawnAllTheSame() {
        val image = pin("crimson", null)

        assertEquals(0, image.at(14.0, 6.0).alpha)
        assertTrue(image.at(14.0, 1.5).isNear(Pixels.white), "${image.at(14.0, 1.5)}")
        assertTrue(image.at(14.0, 14.0).isNear(Pixels.white), "${image.at(14.0, 14.0)}")
    }
}
