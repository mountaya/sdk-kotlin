package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class AltitudeToZoomTest {
    @Test
    fun theSameAltitudeIsAnotherZoomAtAnotherLatitude() {
        assertEquals(15.934271690397807, altitudeToZoom(1_500.0, 0.0), 1e-12)
        assertEquals(15.411250131748451, altitudeToZoom(1_500.0, 45.9), 1e-12)
        assertEquals(14.934271690397807, altitudeToZoom(1_500.0, 60.0), 1e-12)
        // Eight times as high is three zooms out.
        assertEquals(12.411250131748451, altitudeToZoom(12_000.0, 45.9), 1e-12)
    }

    @Test
    fun theZoomIsHeldWithinTheZoomsAFlightIsFramedAt() {
        assertEquals(16.0, altitudeToZoom(400.0, 45.9))
        assertEquals(16.0, altitudeToZoom(1.0, 45.9))
        assertEquals(4.0, altitudeToZoom(100_000_000.0, 0.0))
        assertEquals(16.0, ALTITUDE_ZOOM_CEILING)
        assertEquals(4.0, ALTITUDE_ZOOM_FLOOR)
    }

    /**
     * A canvas twice as tall puts the camera twice as far from the screen: twice the altitude shows
     * the ground at the zoom the nominal canvas shows it at, where on that canvas it is a zoom out.
     */
    @Test
    fun aTallerCanvasIsACloserZoom() {
        assertEquals(altitudeToZoom(1_500.0, 45.9), altitudeToZoom(3_000.0, 45.9, 1_600.0), 1e-12)
        assertEquals(altitudeToZoom(1_500.0, 45.9) - 1.0, altitudeToZoom(3_000.0, 45.9), 1e-12)
        assertEquals(altitudeToZoom(1_500.0, 45.9), altitudeToZoom(1_500.0, 45.9, 800.0))
        assertEquals(altitudeToZoom(1_500.0, 45.9), altitudeToZoom(1_500.0, 45.9, Double.NaN))
        assertEquals(altitudeToZoom(1_500.0, 45.9), altitudeToZoom(1_500.0, 45.9, 0.0))
    }

    @Test
    fun anAltitudeThatIsNoFiniteNumberAboveZeroIsTheSafeMidZoom() {
        for (altitude in listOf(0.0, -0.0, -5.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            assertEquals(12.0, altitudeToZoom(altitude, 45.9), "for $altitude")
        }
        assertEquals(12.0, SAFE_MID_ZOOM)
    }

    @Test
    fun aLatitudeThatYieldsNoZoomIsTheSafeMidZoom() {
        assertEquals(12.0, altitudeToZoom(1_500.0, Double.NaN))
        assertEquals(12.0, altitudeToZoom(1_500.0, 100.0))
    }

    @Test
    fun anAltitudeAndItsZoomRoundTrip() {
        assertEquals(1_500.0, zoomToAltitude(altitudeToZoom(1_500.0, 45.9), 45.9), 1e-9)
        assertEquals(14.0, altitudeToZoom(zoomToAltitude(14.0, 45.9), 45.9), 1e-12)
    }
}
