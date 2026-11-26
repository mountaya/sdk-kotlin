package com.mountaya.conformance

import com.mountaya.geo.ELEVATION_NEIGHBOUR_DEGREES
import com.mountaya.geo.ElevationEncoding
import com.mountaya.geo.ElevationSource
import com.mountaya.geo.ElevationTile
import com.mountaya.geo.METERS_PER_DEGREE_MEAN_SPHERE
import com.mountaya.geo.elevationTilesAt
import com.mountaya.geo.terrainFromElevationTiles
import kotlinx.coroutines.test.TestScope

/**
 * `geo.terrain.elevation-sample`: the tiles of the case's `source` the terrain at its `position` is read off, by
 * `elevationTilesAt`, and the terrain `terrainFromElevationTiles` reads off the `tiles` at hand, each tile's `pixels`
 * packed as `decodeElevation` reads them, rows from the north. The neighbours' spacing is held to
 * `ELEVATION_NEIGHBOUR_DEGREES`, and the mean sphere's degree to the one `slopeAspectFromNeighbours` measures in.
 */
object GeoTerrainElevationSampleRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            mapOf(
                "metersPerDegreeMeanSphere" to METERS_PER_DEGREE_MEAN_SPHERE,
                "neighbourDegrees" to ELEVATION_NEIGHBOUR_DEGREES,
            ),
        )
        val input = case.fields
        val source = elevationSource(input.objectOrNull("source") ?: throw AssertionError("`source` is absent"))
        val position = fixturePosition(input["position"], "position")
        val tiles =
            input.items("tiles").map {
                elevationTile(it as? FixtureValue.Object ?: throw AssertionError("a tile is no object"))
            }
        val terrain = terrainFromElevationTiles(position, source, tiles)
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "reads" to FixtureValue.Array(elevationTilesAt(position, source).map(::tileAddress)),
                    "terrain" to
                        (
                            terrain?.let {
                                FixtureValue.Object(
                                    listOf(
                                        "aspect" to
                                            (
                                                it.aspect?.let { aspect -> FixtureValue.Number(aspect) }
                                                    ?: FixtureValue.Null
                                            ),
                                        "elevation" to FixtureValue.Number(it.elevation),
                                        "slope" to FixtureValue.Number(it.slope),
                                    ),
                                )
                            } ?: FixtureValue.Null
                        ),
                ),
            ),
        )
    }
}

/** The elevation source a fixture writes, `{encoding, tileSize, zoom}`. */
internal fun elevationSource(source: FixtureValue.Object): ElevationSource =
    ElevationSource(
        encoding = source.textOrNull("encoding")?.let(::ElevationEncoding),
        tileSize = source.number("tileSize").toInt(),
        zoom = source.number("zoom").toInt(),
    )

/** The pixels a fixture writes, rows from the north down, each pixel `[R, G, B]`, packed row by row. */
internal fun elevationPixels(rows: List<FixtureValue>): IntArray =
    rows
        .flatMap { row -> (row as? FixtureValue.Array)?.items ?: throw AssertionError("a row of pixels is no array") }
        .map { pixel ->
            elevationPixel(
                (pixel as? FixtureValue.Array)?.items?.map { (it as FixtureValue.Number).value.toInt() }
                    ?: throw AssertionError("a pixel is no array"),
            )
        }.toIntArray()

/** The tile a fixture writes, `{z, x, y, pixels}`, square. */
private fun elevationTile(tile: FixtureValue.Object): ElevationTile {
    val rows = tile.items("pixels")
    return ElevationTile(
        ElevationTile.Address(tile.number("z").toInt(), tile.number("x").toInt(), tile.number("y").toInt()),
        rows.size,
        elevationPixels(rows),
    )
}

/** A tile's address as a fixture writes it, `{x, y, z}`. */
internal fun tileAddress(address: ElevationTile.Address): FixtureValue =
    FixtureValue.Object(
        listOf(
            "x" to FixtureValue.Number(address.x.toDouble()),
            "y" to FixtureValue.Number(address.y.toDouble()),
            "z" to FixtureValue.Number(address.z.toDouble()),
        ),
    )
