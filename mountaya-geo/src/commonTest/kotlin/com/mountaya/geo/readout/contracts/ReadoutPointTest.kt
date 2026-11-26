package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ReadoutPointTest {
    @Test
    fun aTerrainReadingStatesEachOfItsFigures() {
        assertEquals(ReadoutPoint.Terrain(1500.0, 2.0, null), ReadoutPoint.Terrain(TerrainReading(1500.0, 2.0, null)))
    }

    @Test
    fun twoPointsAreEqualWhereEveryMemberIs() {
        val point = ReadoutPoint(listOf(6.8652, 45.9237), 14.0, band = ReadoutPoint.Band(Double.NaN, 5.0))

        assertEquals(point, ReadoutPoint(listOf(6.8652, 45.9237), 14.0, band = ReadoutPoint.Band(Double.NaN, 5.0)))
        assertEquals(
            point.hashCode(),
            ReadoutPoint(listOf(6.8652, 45.9237), 14.0, band = ReadoutPoint.Band(Double.NaN, 5.0)).hashCode(),
        )
        assertNotEquals(point, ReadoutPoint(listOf(6.8652, 45.9237), 13.0, band = ReadoutPoint.Band(Double.NaN, 5.0)))
        assertNotEquals(ReadoutPoint.Poi("Refuge", "alpine_hut"), ReadoutPoint.Poi("Refuge", "hut"))
    }
}
