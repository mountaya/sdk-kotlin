package com.mountaya.geo

/**
 * terrainFromElevationTiles reads the terrain at [position], `[longitude, latitude]`, off [tiles] of [source]
 * (`geo.terrain.elevation-sample`): the height at the point and at its four neighbours one ten-thousandth of a
 * degree to its north, south, east and west, each the bilinear mix of the four pixel centres around it, and the
 * slope and the aspect `slopeAspectFromNeighbours` reads over the neighbours at the point's latitude.
 *
 * A row of two pixels is mixed as a + (b - a) × f from its western pixel to its eastern, then the northern row and
 * the southern the same way, a form that answers a pixel's own height exactly where the pixels agree, each pixel
 * read by `decodeElevation` in the source's encoding. A tile is found among [tiles] by its address, and the tiles
 * `elevationTilesAt` names are the ones it needs.
 *
 * Null where a place reads nothing, a tile it needs is not among [tiles], or a pixel reads no height.
 */
public fun terrainFromElevationTiles(
    position: List<Double>,
    source: ElevationSource,
    tiles: List<ElevationTile>,
): TerrainReading? {
    val places = elevationPlaces(position, source) ?: return null
    val worldPixels = (1L shl source.zoom) * source.tileSize
    val byAddress = tiles.associateBy { it.address }

    fun height(
        column: Long,
        row: Long,
    ): Double? {
        val (address, pixel) = elevationPixelAt(column, row, source, worldPixels)
        val tile = byAddress[address] ?: return null
        val packed = tile.pixelAt(pixel.first, pixel.second) ?: return null
        return decodeElevation(packed, source.encoding)
    }

    val heights =
        places.map { place ->
            val northWest = height(place.column, place.row) ?: return null
            val northEast = height(place.column + 1, place.row) ?: return null
            val southWest = height(place.column, place.row + 1) ?: return null
            val southEast = height(place.column + 1, place.row + 1) ?: return null
            val north = northWest + (northEast - northWest) * place.across
            val south = southWest + (southEast - southWest) * place.across
            north + (south - north) * place.down
        }
    val neighbours = TerrainNeighbours(north = heights[1], south = heights[2], east = heights[3], west = heights[4])
    val ground = slopeAspectFromNeighbours(neighbours, position[1], ELEVATION_NEIGHBOUR_DEGREES)
    return TerrainReading(heights[0], ground.slope, ground.aspect)
}
