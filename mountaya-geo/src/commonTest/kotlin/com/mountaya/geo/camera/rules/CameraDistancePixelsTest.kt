package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraDistancePixelsTest {
    @Test
    fun theCameraStandsOneAndAHalfCanvasHeightsFromTheScreen() {
        assertEquals(1_200.0, cameraDistancePixels(800.0))
        assertEquals(1_620.0, cameraDistancePixels(1_080.0))
        assertEquals(800.0, DEFAULT_VIEWPORT_HEIGHT_PIXELS)
        assertEquals(1.5, FIELD_OF_VIEW_DISTANCE_FACTOR)
    }

    @Test
    fun aHeightThatIsNoCanvasReadsTheNominalOne() {
        for (height in listOf(0.0, -1.0, -0.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertEquals(1_200.0, cameraDistancePixels(height), "for $height")
        }
    }
}
