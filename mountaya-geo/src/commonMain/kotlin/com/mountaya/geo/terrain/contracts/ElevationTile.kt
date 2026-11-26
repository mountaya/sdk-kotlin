package com.mountaya.geo

/**
 * ElevationTile is one tile of an elevation raster, read into its pixels (`geo.terrain.elevation-sample`): its
 * [address] on the tile grid, the edge of its square in pixels, [size], and its [pixels], rows from the north down
 * and each row's pixels from the west, each packed as `decodeElevation` reads one, red in bits 16 to 23, green in
 * 8 to 15 and blue in 0 to 7. Reading an image's bytes into pixels is the platform's own codec's, never this rule's.
 *
 * A tile whose pixels are fewer than its square holds reads as missing where a pixel past them is asked for. Two
 * are equal where their addresses, their sizes and their pixels are.
 */
public class ElevationTile(
    /** Where the tile stands on the grid. */
    public val address: Address,
    /** The edge of the tile's square, in pixels. */
    public val size: Int,
    /** The pixels, row by row from the north-west, each its channels packed. */
    public val pixels: IntArray,
) {
    /**
     * Address is a tile's place on the grid: its zoom [z], its column [x] from the antimeridian eastward and its row
     * [y] from the north. Two are equal where all three are.
     */
    public class Address(
        /** The zoom. */
        public val z: Int,
        /** The column, from 0 at the antimeridian eastward. */
        public val x: Int,
        /** The row, from 0 at the north edge of Web Mercator southward. */
        public val y: Int,
    ) {
        override fun equals(other: Any?): Boolean = other is Address && other.z == z && other.x == x && other.y == y

        override fun hashCode(): Int = (z * 31 + x) * 31 + y

        override fun toString(): String = "Address(z=$z, x=$x, y=$y)"
    }

    /** The packed pixel at [column] and [row] of the tile, or null where the tile holds none there. */
    internal fun pixelAt(
        column: Int,
        row: Int,
    ): Int? {
        if (column !in 0 until size || row !in 0 until size) return null
        return pixels.getOrNull(row * size + column)
    }

    override fun equals(other: Any?): Boolean =
        other is ElevationTile && other.address == address && other.size == size && other.pixels.contentEquals(pixels)

    override fun hashCode(): Int = (address.hashCode() * 31 + size) * 31 + pixels.contentHashCode()

    override fun toString(): String = "ElevationTile(address=$address, size=$size, ${pixels.size} pixels)"
}
