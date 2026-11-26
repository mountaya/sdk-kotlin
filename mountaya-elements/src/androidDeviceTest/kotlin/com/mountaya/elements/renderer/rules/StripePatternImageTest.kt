package com.mountaya.elements

import androidx.test.platform.app.InstrumentationRegistry
import com.mountaya.geo.RoadAccessKind
import com.mountaya.geo.ShapePaint
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** A stripe tile as an image, drawn on the device at its screen's density. */
class StripePatternImageTest {
    private val densityDpi: Int =
        InstrumentationRegistry
            .getInstrumentation()
            .targetContext.resources.displayMetrics.densityDpi

    @Test
    fun aRestrictedStretchsTileIs16PixelsSquareItsStripesInItsKindsColourOnWhite() {
        val image = stripePatternImage(roadAccessStripePattern(RoadAccessKind.Permit), densityDpi)
        val stripe = Pixels.Pixel(0x9c, 0x36, 0xb5, 255)

        assertEquals((16 * densityDpi / 160.0).roundToInt(), image.width)
        assertEquals(densityDpi, image.density)
        // On the stripe through the tile, its corner to corner diagonal, and on the half past each corner, which
        // completes the next tile's.
        for ((x, y) in listOf(8.0 to 8.0, 0.2 to 0.2, 15.8 to 15.8)) {
            val drawn = assertNotNull(Pixels.pixel(image, x, y), "the point lies inside the image")
            assertTrue(drawn.isNear(stripe), "($x, $y): $drawn")
        }
        // Between the stripes, the white ground.
        for ((x, y) in listOf(4.0 to 4.0, 12.0 to 12.0)) {
            val drawn = assertNotNull(Pixels.pixel(image, x, y), "the point lies inside the image")
            assertTrue(drawn.isNear(Pixels.white), "($x, $y): $drawn")
        }
    }

    @Test
    fun aPatternWithNoGroundLeavesItsGapsClear() {
        val pattern =
            StripePattern(
                16.0,
                4.0,
                "#f03e3e",
                null,
                ShapePaint.LineCap.Round,
                listOf(listOf(listOf(0.0, 16.0), listOf(16.0, 0.0))),
            )
        val image = stripePatternImage(pattern, densityDpi)

        assertEquals(255, Pixels.pixel(image, 8.0, 8.0)?.alpha)
        assertEquals(0, Pixels.pixel(image, 2.0, 2.0)?.alpha)
    }
}
