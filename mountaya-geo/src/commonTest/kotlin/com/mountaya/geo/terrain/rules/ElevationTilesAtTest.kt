package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ElevationTilesAtTest {
    private val source = ElevationSource(ElevationEncoding.Mapbox, 4, 16)

    @Test
    fun aPointAndItsNeighboursInOneTileReadThatTile() {
        assertEquals(listOf(ElevationTile.Address(16, 34017, 23335)), elevationTilesAt(listOf(6.8652, 45.9237), source))
    }

    @Test
    fun aPointNearATilesEdgeReadsTheTileBesideItOrderedByColumn() {
        assertEquals(
            listOf(ElevationTile.Address(16, 34017, 23335), ElevationTile.Address(16, 34018, 23335)),
            elevationTilesAt(listOf(6.8665924072265625, 45.9237), source),
        )
    }

    @Test
    fun theAntimeridianIsCrossedByWholeTurns() {
        val tiles = elevationTilesAt(listOf(179.99999, 45.9237), source)

        assertEquals(listOf(0, 65535), tiles.map { it.x })
    }

    @Test
    fun aPointPastWebMercatorsLatitudeOrNoPlaceReadsNoTile() {
        assertTrue(elevationTilesAt(listOf(6.8652, 85.06), source).isEmpty())
        assertTrue(elevationTilesAt(listOf(Double.NaN, 45.0), source).isEmpty())
        assertTrue(elevationTilesAt(listOf(6.8652), source).isEmpty())
    }
}
