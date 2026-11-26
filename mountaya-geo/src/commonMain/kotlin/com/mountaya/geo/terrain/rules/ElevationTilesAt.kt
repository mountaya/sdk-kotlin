package com.mountaya.geo

import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.tan

/**
 * ELEVATION_NEIGHBOUR_DEGREES is how far from a point its four neighbours are read off the elevation tiles, in
 * degrees: one ten-thousandth, the spacing `terrainFromElevationTiles` hands `slopeAspectFromNeighbours`.
 */
public const val ELEVATION_NEIGHBOUR_DEGREES: Double = 0.0001

/** Half a degree of a turn: the longitude Web Mercator's x starts from. */
private const val HALF_TURN_DEGREES: Double = 180.0

/**
 * ElevationPlace is one place the terrain is read at, placed on the world's pixel grid at the source's zoom: the
 * column and the row of the north-western of the four pixel centres around it, and how far past them it stands
 * toward the next column and the next row, each from 0 to 1.
 */
internal class ElevationPlace(
    val column: Long,
    val row: Long,
    val across: Double,
    val down: Double,
)

/**
 * The five places the terrain at [position] is read at, in the order the point, then its neighbours to the north,
 * south, east and west, each placed on [source]'s world pixel grid by Web Mercator, or null where one of them reads
 * nothing: a position that names no place, or a place whose pixels reach past the world's top or bottom edge.
 *
 * A place's x is (longitude + 180) / 360 × 2^zoom × tileSize and its y (1 - ln(tan(π/4 + latitude / 2)) / π) / 2
 * × 2^zoom × tileSize, and a pixel's centre stands half a pixel past its corner, so the pixels around it are those
 * of the columns floor(x - 0.5) and the one after and the rows floor(y - 0.5) and the one after. A column is taken
 * by whole turns of the world, so the antimeridian is crossed.
 */
internal fun elevationPlaces(
    position: List<Double>,
    source: ElevationSource,
): List<ElevationPlace>? {
    if (position.size < 2 || source.tileSize <= 0 || source.zoom < 0) return null
    val longitude = position[0]
    val latitude = position[1]
    if (!longitude.isFinite() || !latitude.isFinite()) return null
    val world = (1L shl source.zoom).toDouble() * source.tileSize
    val places =
        listOf(
            longitude to latitude,
            longitude to latitude + ELEVATION_NEIGHBOUR_DEGREES,
            longitude to latitude - ELEVATION_NEIGHBOUR_DEGREES,
            longitude + ELEVATION_NEIGHBOUR_DEGREES to latitude,
            longitude - ELEVATION_NEIGHBOUR_DEGREES to latitude,
        )
    return places.map { (lng, lat) ->
        val x = (lng + HALF_TURN_DEGREES) / DEGREES_PER_TURN * world
        val radians = lat * PI / HALF_TURN_DEGREES
        val y = (1 - ln(tan(PI / 4 + radians / 2)) / PI) / 2 * world
        if (!x.isFinite() || !y.isFinite()) return null
        val u = x - 0.5
        val v = y - 0.5
        val column = floor(u)
        val row = floor(v)
        // A pixel past the world's top or bottom edge reads nothing.
        if (row < 0 || row + 1 >= world) return null
        ElevationPlace(column.toLong(), row.toLong(), u - column, v - row)
    }
}

/**
 * The tile and the pixel within it that the world pixel at [column] and [row] stands in, the column taken by whole
 * turns of a world [worldPixels] wide.
 */
internal fun elevationPixelAt(
    column: Long,
    row: Long,
    source: ElevationSource,
    worldPixels: Long,
): Pair<ElevationTile.Address, Pair<Int, Int>> {
    val wrapped = ((column % worldPixels) + worldPixels) % worldPixels
    val size = source.tileSize.toLong()
    return ElevationTile.Address(source.zoom, (wrapped / size).toInt(), (row / size).toInt()) to
        ((wrapped % size).toInt() to (row % size).toInt())
}

/**
 * elevationTilesAt answers the tiles of [source] the terrain at [position], `[longitude, latitude]`, is read off
 * (`geo.terrain.elevation-sample`): those holding the four pixel centres around the point and around each of its
 * neighbours one ten-thousandth of a degree to its north, south, east and west, at the source's deepest zoom,
 * each once, ordered by column then row. A map reads each through `getElevationTile` before it reads the terrain
 * with `terrainFromElevationTiles`.
 *
 * None where a place reads nothing, a position that names no place or one past Web Mercator's latitude, since no
 * tile could give its terrain.
 */
public fun elevationTilesAt(
    position: List<Double>,
    source: ElevationSource,
): List<ElevationTile.Address> {
    val places = elevationPlaces(position, source) ?: return emptyList()
    val worldPixels = (1L shl source.zoom) * source.tileSize
    val tiles = LinkedHashSet<ElevationTile.Address>()
    for (place in places) {
        for (row in place.row..place.row + 1) {
            for (column in place.column..place.column + 1) {
                tiles += elevationPixelAt(column, row, source, worldPixels).first
            }
        }
    }
    return tiles.sortedWith(compareBy({ it.x }, { it.y }))
}
