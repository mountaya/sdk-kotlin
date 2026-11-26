package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FormatLngLatTest {
    @Test
    fun theLatitudeComesFirstEachWithItsHemisphere() {
        assertEquals("45.9237°N 6.8652°E", formatLngLat(listOf(6.8652, 45.9237)))
        assertEquals("51.5072°N 0.1276°W", formatLngLat(listOf(-0.1276, 51.5072)))
        assertEquals("12.0464°S 77.0365°W", formatLngLat(listOf(-77.0365, -12.0464)))
        assertEquals("0.0000°N 0.0000°E", formatLngLat(listOf(0.0, 0.0)))
    }

    @Test
    fun aLongitudePastTheAntimeridianIsTheMeridianItNames() {
        assertEquals("45.9237°N 162.5000°W", formatLngLat(listOf(197.5, 45.9237)))
        assertEquals("12.0464°S 170.0000°E", formatLngLat(listOf(-190.0, -12.0464)))
    }

    /** Within the closed range a longitude is printed as written: 180 is east and -180 west. */
    @Test
    fun theAntimeridianKeepsTheSideItIsWrittenOn() {
        assertEquals("0.0000°N 180.0000°E", formatLngLat(listOf(180.0, 0.0)))
        assertEquals("0.0000°N 180.0000°W", formatLngLat(listOf(-180.0, 0.0)))
        assertEquals("0.0000°N 180.0000°W", formatLngLat(listOf(540.0, 0.0)))
    }

    @Test
    fun aHemisphereIsChosenAfterRounding() {
        assertEquals("45.0000°N 0.0000°E", formatLngLat(listOf(-0.00004, 45.0)))
        assertEquals("0.0000°N 6.0000°E", formatLngLat(listOf(6.0, -0.00001)))
        assertEquals("0.0001°S 0.0001°W", formatLngLat(listOf(-0.00005, -0.00005)))
        assertEquals("0.0000°N 0.0000°E", formatLngLat(listOf(-0.0, -0.0)))
    }

    @Test
    fun anAltitudeIsNotPrinted() {
        assertEquals("45.9237°N 6.8652°E", formatLngLat(listOf(6.8652, 45.9237, 1035.0)))
    }

    /** A latitude has no wrap of its own: past a pole is printed as written. */
    @Test
    fun aLatitudeIsPrintedAsWritten() {
        assertEquals("95.0000°N 10.0000°E", formatLngLat(listOf(10.0, 95.0)))
    }

    @Test
    fun aPositionThatNamesNoPlacePrintsNothing() {
        assertNull(formatLngLat(emptyList()))
        assertNull(formatLngLat(listOf(6.88)))
        assertNull(formatLngLat(listOf(Double.NaN, 45.0)))
        assertNull(formatLngLat(listOf(6.0, Double.NEGATIVE_INFINITY)))
    }
}
