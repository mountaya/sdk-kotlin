package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TerrainNeighboursTest {
    @Test
    fun twoNeighbourhoodsAreEqualWhenEveryElevationIs() {
        assertEquals(TerrainNeighbours(1.0, 2.0, 3.0, 4.0), TerrainNeighbours(1.0, 2.0, 3.0, 4.0))
        assertEquals(TerrainNeighbours(1.0, 2.0, 3.0, 4.0).hashCode(), TerrainNeighbours(1.0, 2.0, 3.0, 4.0).hashCode())
        assertNotEquals(TerrainNeighbours(1.0, 2.0, 3.0, 4.0), TerrainNeighbours(1.0, 2.0, 4.0, 3.0))
        // The numbers are compared as the same numbers: NaN as itself, and -0 as another than 0.
        assertEquals(TerrainNeighbours(Double.NaN, 2.0, 3.0, 4.0), TerrainNeighbours(Double.NaN, 2.0, 3.0, 4.0))
        assertNotEquals(TerrainNeighbours(0.0, 2.0, 3.0, 4.0), TerrainNeighbours(-0.0, 2.0, 3.0, 4.0))
    }

    @Test
    fun eachElevationIsNamedByItsSide() {
        val neighbours = TerrainNeighbours(north = 1199.0, south = 1201.0, east = 1200.5, west = 1199.5)

        assertEquals(
            listOf(1199.0, 1201.0, 1200.5, 1199.5),
            listOf(neighbours.north, neighbours.south, neighbours.east, neighbours.west),
        )
        assertEquals("TerrainNeighbours(north=1199.0, south=1201.0, east=1200.5, west=1199.5)", neighbours.toString())
    }
}
