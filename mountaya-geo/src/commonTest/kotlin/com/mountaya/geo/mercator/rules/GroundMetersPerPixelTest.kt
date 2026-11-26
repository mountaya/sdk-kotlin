package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GroundMetersPerPixelTest {
    /** At zoom 0 on the equator one pixel spans the circumference over one 512-pixel tile. */
    @Test
    fun atZoomZeroOnTheEquatorAPixelIsTheCircumferenceOverOneTile() {
        assertEquals(78_271.51696484375, groundMetersPerPixel(0.0, 0.0))
        assertEquals(
            WEB_MERCATOR_CIRCUMFERENCE_METERS / WEB_MERCATOR_TILE_SIZE_PIXELS,
            groundMetersPerPixel(0.0, 0.0),
        )
        assertEquals(40_075_016.686, WEB_MERCATOR_CIRCUMFERENCE_METERS)
        assertEquals(512.0, WEB_MERCATOR_TILE_SIZE_PIXELS)
        assertEquals(85.051129, WEB_MERCATOR_MAX_LATITUDE)
    }

    @Test
    fun eachZoomHalvesThePixelAndTheLatitudeShrinksItByItsCosine() {
        assertEquals(39_135.758482421875, groundMetersPerPixel(1.0, 0.0))
        assertEquals(3.3245941323563954, groundMetersPerPixel(14.0, 45.9), 1e-12)
        assertEquals(152.87405657196047, groundMetersPerPixel(8.0, 60.0), 1e-9)
        assertEquals(groundMetersPerPixel(8.0, 60.0), groundMetersPerPixel(8.0, -60.0))
    }

    @Test
    fun aZoomThatIsNoFiniteNumberSpansNoGround() {
        assertEquals(0.0, groundMetersPerPixel(Double.NaN, 45.9))
        assertEquals(0.0, groundMetersPerPixel(Double.POSITIVE_INFINITY, 45.9))
        assertEquals(0.0, groundMetersPerPixel(Double.NEGATIVE_INFINITY, 45.9))
    }

    /** The latitude is read as written: a caller that needs a positive resolution holds it first. */
    @Test
    fun aLatitudeIsReadAsWritten() {
        assertTrue(groundMetersPerPixel(0.0, 120.0) < 0.0)
        assertTrue(groundMetersPerPixel(0.0, Double.NaN).isNaN())
    }

    @Test
    fun aDegreeOfTheProjectionIsItsCircumferenceOverATurn() {
        assertEquals(111_319.49079444444, METERS_PER_DEGREE_WEB_MERCATOR, 1e-9)
    }
}
