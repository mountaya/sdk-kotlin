package com.mountaya.elements

import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import com.mountaya.geo.GlyphSet
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** The marker sprite as an image, drawn on the device at its screen's density. */
class MarkerSpriteImageTest {
    private val red = Pixels.Pixel(224, 49, 49, 255)

    private val densityDpi: Int =
        InstrumentationRegistry
            .getInstrumentation()
            .targetContext.resources.displayMetrics.densityDpi

    /** The sprite of [glyph] of [set] in [color]. */
    private fun sprite(
        glyph: String,
        color: String,
        set: GlyphSet,
    ): Bitmap =
        markerSpriteImage(
            assertNotNull(describeMarkerSprite(glyph, color, set), "the glyph has a sprite"),
            assertNotNull(glyphFigures(glyph, set), "the set holds the glyph"),
            densityDpi,
        )

    private fun Bitmap.at(
        x: Double,
        y: Double,
    ): Pixels.Pixel = assertNotNull(Pixels.pixel(this, x, y), "($x, $y) is outside the image")

    @Test
    fun aSpriteIs48DensityIndependentPixelsSquareAtTheScreensDensityAColourImage() {
        val image = sprite("hut", "#e03131", GlyphSet.marker)
        val density = densityDpi / 160.0

        assertEquals(densityDpi, image.density)
        assertTrue(density >= 1.0)
        assertEquals((48 * density).roundToInt(), image.width)
        assertEquals(image.width, image.height)
        assertEquals(Bitmap.Config.ARGB_8888, image.config)
        assertTrue(image.hasAlpha())
    }

    @Test
    fun theDiscIsTheFeaturesColourUnderAWhiteBorderTheCornersLeftClear() {
        val image = sprite("hut", "#e03131", GlyphSet.marker)

        // Inside the disc, above the glyph's square: the feature's colour.
        assertTrue(image.at(24.0, 8.0).isNear(red), "${image.at(24.0, 8.0)}")
        // The border's middle, 21 pixels from the centre: white.
        assertTrue(image.at(24.0, 3.0).isNear(Pixels.white), "${image.at(24.0, 3.0)}")
        assertTrue(image.at(45.0, 24.0).isNear(Pixels.white), "${image.at(45.0, 24.0)}")
        // A corner, outside the disc and its border: nothing.
        assertEquals(0, image.at(1.0, 1.0).alpha)
        assertEquals(0, image.at(47.0, 47.0).alpha)
    }

    @Test
    fun theGlyphIsWhiteFittedToThe24PixelSquareAtTheCentreWhateverItsSetsGrid() {
        // A glyph filling its whole box shows where the square is drawn.
        val grid48 =
            assertNotNull(
                GlyphSet.of(
                    "grid48",
                    GlyphSet.Paint.Fill,
                    viewBox = 48.0,
                    markup =
                        mapOf("block" to """<rect width="48" height="48"/>"""),
                ),
                "the 48-unit set is a set",
            )
        val image = sprite("block", "#e03131", grid48)

        assertTrue(image.at(13.0, 13.0).isNear(Pixels.white), "${image.at(13.0, 13.0)}")
        assertTrue(image.at(34.5, 34.5).isNear(Pixels.white), "${image.at(34.5, 34.5)}")
        assertTrue(image.at(11.0, 24.0).isNear(red), "${image.at(11.0, 24.0)}")
        assertTrue(image.at(37.0, 24.0).isNear(red), "${image.at(37.0, 24.0)}")
    }

    @Test
    fun aStrokeIsDrawnAtTheSetsWidthScaledWithTheGlyphWithItsRoundEnds() {
        // A line across the middle of a 24-unit box, 2 units wide: 2 pixels in the sprite.
        val line =
            assertNotNull(
                GlyphSet.of(
                    "line",
                    GlyphSet.Paint.Stroke,
                    markup =
                        mapOf("line" to """<path d="M4 12h16"/>"""),
                ),
                "the line set is a set",
            )
        val image = sprite("line", "#e03131", line)

        assertTrue(image.at(24.0, 24.0).isNear(Pixels.white), "${image.at(24.0, 24.0)}")
        assertTrue(image.at(24.0, 26.5).isNear(red), "${image.at(24.0, 26.5)}")
        // The round cap reaches a unit past the line's end.
        assertTrue(image.at(31.5, 24.0).isNear(Pixels.white), "${image.at(31.5, 24.0)}")
        assertTrue(image.at(33.5, 24.0).isNear(red), "${image.at(33.5, 24.0)}")
    }

    @Test
    fun aColourThatIsNoColourFillsNoDiscAndTheBorderAndGlyphAreStillDrawn() {
        val image = sprite("hut", "teal", GlyphSet.marker)

        assertEquals(0, image.at(24.0, 8.0).alpha)
        assertTrue(image.at(24.0, 3.0).isNear(Pixels.white), "${image.at(24.0, 3.0)}")
    }
}
