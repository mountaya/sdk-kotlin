package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DecodeElevationTest {
    private fun pixel(
        red: Int,
        green: Int,
        blue: Int,
        alpha: Int = 255,
    ): Int = (alpha shl 24) or (red shl 16) or (green shl 8) or blue

    @Test
    fun terrainRgbWeighsEachChannelByItsPlace() {
        assertEquals(-10_000.0, decodeElevation(pixel(0, 0, 0), ElevationEncoding.Mapbox))
        assertEquals(0.0, decodeElevation(pixel(1, 134, 160), ElevationEncoding.Mapbox) ?: Double.NaN, 1e-9)
        assertEquals(1182.4, decodeElevation(pixel(1, 180, 208), ElevationEncoding.Mapbox) ?: Double.NaN, 1e-9)
    }

    @Test
    fun noEncodingReadsAsTerrainRgb() {
        assertEquals(
            decodeElevation(pixel(1, 165, 186), ElevationEncoding.Mapbox),
            decodeElevation(pixel(1, 165, 186), null),
        )
    }

    @Test
    fun terrariumReadsBlueAsTheFractionAndTheAlphaChannelIsReadByNeither() {
        assertEquals(1834.5, decodeElevation(pixel(135, 42, 128), ElevationEncoding.Terrarium))
        assertEquals(0.0, decodeElevation(pixel(128, 0, 0, alpha = 0), ElevationEncoding.Terrarium))
        assertEquals(-32_768.0, decodeElevation(pixel(0, 0, 0), ElevationEncoding.Terrarium))
    }

    @Test
    fun anEncodingNoRuleNamesReadsNoHeight() {
        assertNull(decodeElevation(pixel(1, 134, 160), ElevationEncoding("custom")))
        assertNull(decodeElevation(pixel(1, 134, 160), ElevationEncoding("Mapbox")))
    }
}
