package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TerrainReadingTest {
    @Test
    fun twoReadingsAreEqualWhereTheyHoldTheSameNumbers() {
        assertEquals(TerrainReading(1500.0, 0.0, null), TerrainReading(1500.0, 0.0, null))
        assertEquals(TerrainReading(Double.NaN, 1.0, 90.0), TerrainReading(Double.NaN, 1.0, 90.0))
        assertNotEquals(TerrainReading(1500.0, 0.0, null), TerrainReading(1500.0, -0.0, null))
        assertNotEquals(TerrainReading(1500.0, 1.0, 90.0), TerrainReading(1500.0, 1.0, null))
    }
}
