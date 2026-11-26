package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoomToAltitudeTest {
    @Test
    fun anAltitudeIsTheGroundResolutionAcrossTheCamerasDistanceFromTheScreen() {
        assertEquals(93_925_820.3578125, zoomToAltitude(0.0, 0.0))
        assertEquals(5_732.7771214485165, zoomToAltitude(14.0, 0.0), 1e-8)
        assertEquals(3_989.5129588276745, zoomToAltitude(14.0, 45.9), 1e-8)
        assertEquals(183_448.86788635256, zoomToAltitude(8.0, 60.0), 1e-6)
    }

    /** A clearance pass reads eye heights against terrain in meters, so no range holds the answer. */
    @Test
    fun theAltitudeIsHeldToNoRange() {
        assertEquals(zoomToAltitude(20.0, 45.9) / 16.0, zoomToAltitude(24.0, 45.9), 1e-12)
        assertEquals(zoomToAltitude(0.0, 0.0) * 4.0, zoomToAltitude(-2.0, 0.0))
    }

    @Test
    fun aShorterCanvasIsALowerAltitude() {
        assertEquals(zoomToAltitude(14.0, 45.9) / 2.0, zoomToAltitude(14.0, 45.9, 400.0), 1e-9)
        assertEquals(zoomToAltitude(14.0, 45.9), zoomToAltitude(14.0, 45.9, -400.0))
    }

    @Test
    fun aZoomThatIsNoFiniteNumberIsTheSafeMidAltitude() {
        for (zoom in listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            assertEquals(1_000.0, zoomToAltitude(zoom, 45.9), "for $zoom")
        }
        assertEquals(1_000.0, SAFE_MID_ALTITUDE_METERS)
    }

    @Test
    fun aLatitudeThatYieldsNoAltitudeIsTheSafeMidAltitude() {
        assertEquals(1_000.0, zoomToAltitude(14.0, Double.NaN))
        assertEquals(1_000.0, zoomToAltitude(14.0, 100.0))
        assertEquals(1_000.0, zoomToAltitude(-2_000.0, 0.0))
    }
}
