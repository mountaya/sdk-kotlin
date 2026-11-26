package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class ElevationTileTest {
    private val address = ElevationTile.Address(16, 34017, 23335)

    @Test
    fun aPixelIsReadRowByRowFromTheNorthWest() {
        val tile = ElevationTile(address, 2, intArrayOf(1, 2, 3, 4))

        assertEquals(1, tile.pixelAt(0, 0))
        assertEquals(2, tile.pixelAt(1, 0))
        assertEquals(3, tile.pixelAt(0, 1))
        assertEquals(4, tile.pixelAt(1, 1))
    }

    @Test
    fun aPixelPastTheTileOrPastThePixelsItHoldsIsNone() {
        val short = ElevationTile(address, 2, intArrayOf(1, 2, 3))

        assertNull(short.pixelAt(1, 1))
        assertNull(short.pixelAt(2, 0))
        assertNull(short.pixelAt(0, -1))
    }

    @Test
    fun twoTilesAreEqualWhereTheirAddressesSizesAndPixelsAre() {
        val tile = ElevationTile(address, 2, intArrayOf(1, 2, 3, 4))

        assertEquals(tile, ElevationTile(ElevationTile.Address(16, 34017, 23335), 2, intArrayOf(1, 2, 3, 4)))
        assertEquals(tile.hashCode(), ElevationTile(address, 2, intArrayOf(1, 2, 3, 4)).hashCode())
        assertNotEquals(tile, ElevationTile(address, 2, intArrayOf(1, 2, 3, 5)))
        assertNotEquals(address, ElevationTile.Address(16, 34018, 23335))
    }
}
