package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BoundsWithPixelPaddingTest {
    private val box = listOf(6.0, 45.0, 8.0, 46.0)

    private fun assertBox(
        expected: List<Double>,
        actual: List<Double>,
    ) {
        assertEquals(expected.size, actual.size)
        expected.indices.forEach { assertEquals(expected[it], actual[it], 1e-12, "edge $it of $actual") }
    }

    /**
     * A pixel of longitude is 360 / (512 * 2^zoom) degrees at every latitude, and a pixel of
     * latitude that span times the cosine of the centre latitude.
     */
    @Test
    fun eachEdgeGrowsByItsPaddingInTheDegreesAPixelSpans() {
        assertBox(
            listOf(5.9828338623046875, 44.987968095057106, 8.017166137695312, 46.012031904942894),
            boundsWithPixelPadding(box, PixelPadding(100.0), 12.0, 45.5),
        )
        assertBox(
            listOf(5.990386962890625, 44.969920237642775, 8.009613037109375, 46.00673786676802),
            boundsWithPixelPadding(
                box,
                PixelPadding(top = 56.0, right = 56.0, bottom = 250.0, left = 56.0),
                12.0,
                45.5,
            ),
        )
        assertBox(
            listOf(-1.274658203125, -1.274658203125, 1.274658203125, 1.274658203125),
            boundsWithPixelPadding(listOf(-1.0, -1.0, 1.0, 1.0), PixelPadding(100.0), 8.0, 0.0),
        )
    }

    @Test
    fun oneZoomOutDoublesTheDegrees() {
        val near = boundsWithPixelPadding(box, PixelPadding(100.0), 12.0, 45.5)
        val far = boundsWithPixelPadding(box, PixelPadding(100.0), 11.0, 45.5)

        assertEquals((6.0 - near[0]) * 2.0, 6.0 - far[0], 1e-12)
        assertEquals((near[3] - 46.0) * 2.0, far[3] - 46.0, 1e-12)
    }

    @Test
    fun theCentreLatitudeIsHeldWithinTheProjectionsLimit() {
        val atTheLimit = boundsWithPixelPadding(box, PixelPadding(100.0), 6.0, 85.051129)

        assertEquals(atTheLimit, boundsWithPixelPadding(box, PixelPadding(100.0), 6.0, 89.0))
        assertEquals(atTheLimit, boundsWithPixelPadding(box, PixelPadding(100.0), 6.0, 120.0))
        assertEquals(
            boundsWithPixelPadding(box, PixelPadding(100.0), 6.0, -85.051129),
            boundsWithPixelPadding(box, PixelPadding(100.0), 6.0, -89.0),
        )
    }

    @Test
    fun theEdgesMoveAsWritten() {
        assertBox(
            listOf(179.4828338623047, 44.987968095057106, 180.5171661376953, 46.012031904942894),
            boundsWithPixelPadding(listOf(179.5, 45.0, 180.5, 46.0), PixelPadding(100.0), 12.0, 45.5),
        )
        // A padding under zero insets the box; nothing is reordered.
        assertBox(
            listOf(6.0171661376953125, 45.012031904942894, 7.9828338623046875, 45.987968095057106),
            boundsWithPixelPadding(box, PixelPadding(-100.0), 12.0, 45.5),
        )
    }

    @Test
    fun aZoomThatIsNoFiniteNumberLeavesTheBoxAsWritten() {
        assertEquals(box, boundsWithPixelPadding(box, PixelPadding(100.0), Double.NaN, 45.5))
        assertEquals(box, boundsWithPixelPadding(box, PixelPadding(100.0), Double.POSITIVE_INFINITY, 45.5))
        assertEquals(box, boundsWithPixelPadding(box, PixelPadding(0.0), 12.0, 45.5))
    }

    @Test
    fun nothingIsRefused() {
        val unplaced = boundsWithPixelPadding(box, PixelPadding(100.0), 12.0, Double.NaN)
        val placed = boundsWithPixelPadding(box, PixelPadding(100.0), 12.0, 45.5)

        // A pixel of longitude spans the same degrees at every latitude, so only the latitudes are lost.
        assertEquals(listOf(placed[0], placed[2]), listOf(unplaced[0], unplaced[2]))
        assertTrue(unplaced[1].isNaN() && unplaced[3].isNaN())
        assertEquals(List(4) { Double.NaN }, boundsWithPixelPadding(listOf(6.0, 45.0), PixelPadding(100.0), 12.0, 45.5))
    }
}
