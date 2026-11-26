package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TerrainFromElevationTilesTest {
    private val source = ElevationSource(ElevationEncoding.Mapbox, 4, 16)
    private val address = ElevationTile.Address(16, 34017, 23335)

    /** A Terrain-RGB pixel of [meters], a whole tenth of a meter from -10,000. */
    private fun height(meters: Double): Int {
        val steps = ((meters + 10_000.0) * 10.0).toInt()
        return ((steps shr 16) and 0xFF shl 16) or ((steps shr 8) and 0xFF shl 8) or (steps and 0xFF)
    }

    @Test
    fun pixelsThatAgreeGiveTheirOwnHeightOnFlatGround() {
        val tile = ElevationTile(address, 4, IntArray(16) { height(1500.0) })
        val terrain =
            assertNotNull(
                terrainFromElevationTiles(listOf(6.8652, 45.9237), source, listOf(tile)),
                "the tile reads a terrain",
            )

        assertEquals(1500.0, terrain.elevation, 1e-9)
        assertEquals(0.0, terrain.slope, 1e-9)
        assertNull(terrain.aspect)
    }

    @Test
    fun groundFallingToTheSouthFacesSouth() {
        // Each row from the north is lower than the one above it.
        val tile = ElevationTile(address, 4, IntArray(16) { index -> height(1000.0 - (index / 4) * 4.0) })
        val terrain =
            assertNotNull(
                terrainFromElevationTiles(listOf(6.8652, 45.9237), source, listOf(tile)),
                "the tile reads a terrain",
            )

        assertEquals(180.0, assertNotNull(terrain.aspect, "the terrain states its aspect"), 1e-6)
    }

    @Test
    fun aTileItNeedsAndWasNotHandedReadsNoTerrain() {
        val tile = ElevationTile(address, 4, IntArray(16) { height(1500.0) })

        assertNull(terrainFromElevationTiles(listOf(6.8665924072265625, 45.9237), source, listOf(tile)))
        assertNull(terrainFromElevationTiles(listOf(6.8652, 45.9237), source, emptyList()))
    }

    @Test
    fun anEncodingNoRuleNamesReadsNoTerrain() {
        val tile = ElevationTile(address, 4, IntArray(16) { height(1500.0) })

        assertNull(
            terrainFromElevationTiles(
                listOf(6.8652, 45.9237),
                ElevationSource(ElevationEncoding("custom"), 4, 16),
                listOf(tile),
            ),
        )
    }
}
