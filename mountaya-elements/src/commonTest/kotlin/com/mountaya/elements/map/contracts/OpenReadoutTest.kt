package com.mountaya.elements

import com.mountaya.geo.TerrainReading
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OpenReadoutTest {
    @Test
    fun aReadoutOpensWithNoTerrainAndKeepsItsPlaceAsItsTerrainJoins() {
        val open = OpenReadout(listOf(6.8652, 45.9237), listOf(10.0, 20.0), 14.0)
        val read = open.withTerrain(TerrainReading(1807.0, 1.9, 236.3))

        assertNull(open.terrain)
        assertEquals(open.position, read.position)
        assertEquals(open.point, read.point)
        assertEquals(14.0, read.zoom)
        assertEquals(TerrainReading(1807.0, 1.9, 236.3), read.terrain)
    }
}
