package com.mountaya.conformance

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.GeoJsonGeometryCollection
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.GeoJsonMultiLineString
import com.mountaya.client.GeoJsonMultiPoint
import com.mountaya.client.GeoJsonMultiPolygon
import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.GeoJsonPolygon

/**
 * The GeoJSON geometry [value] writes, built as the client's own type, every position kept as short
 * as the fixture writes it and a number JSON cannot carry decoded where it stands, which a JSON
 * decoder would refuse. Fails with a sentence when the value is not a geometry.
 */
internal fun fixtureGeometry(value: FixtureValue): GeoJsonGeometry {
    val geometry = value as? FixtureValue.Object ?: throw AssertionError("a geometry is not an object")
    return when (val type = geometry.textOrNull("type")) {
        "Point" -> {
            GeoJsonPoint(fixturePosition(geometry["coordinates"], "coordinates"))
        }

        "MultiPoint" -> {
            GeoJsonMultiPoint(positions(geometry.items("coordinates")))
        }

        "LineString" -> {
            GeoJsonLineString(positions(geometry.items("coordinates")))
        }

        "MultiLineString" -> {
            GeoJsonMultiLineString(geometry.items("coordinates").map { positions(lists(it)) })
        }

        "Polygon" -> {
            GeoJsonPolygon(geometry.items("coordinates").map { positions(lists(it)) })
        }

        "MultiPolygon" -> {
            GeoJsonMultiPolygon(
                geometry.items("coordinates").map { polygon ->
                    lists(polygon).map { positions(lists(it)) }
                },
            )
        }

        "GeometryCollection" -> {
            GeoJsonGeometryCollection(geometry.items("geometries").map(::fixtureGeometry))
        }

        else -> {
            throw AssertionError("`$type` is not a GeoJSON geometry")
        }
    }
}

private fun positions(items: List<FixtureValue>): List<List<Double>> = items.map { fixturePosition(it, "coordinates") }

private fun lists(value: FixtureValue): List<FixtureValue> =
    (value as? FixtureValue.Array)?.items ?: throw AssertionError("`coordinates` nests a value that is not an array")

/** [geometry] as RFC 7946 writes it, the members sorted as a fixture writes them, each number as it stands. */
internal fun writtenGeometry(geometry: GeoJsonGeometry): FixtureValue {
    fun position(values: List<Double>): FixtureValue = FixtureValue.Array(values.map { FixtureValue.Number(it) })

    fun line(positions: List<List<Double>>): FixtureValue = FixtureValue.Array(positions.map(::position))

    fun rings(lines: List<List<List<Double>>>): FixtureValue = FixtureValue.Array(lines.map(::line))

    fun shaped(
        type: String,
        coordinates: FixtureValue,
    ) = FixtureValue.Object(listOf("coordinates" to coordinates, "type" to FixtureValue.Text(type)))

    return when (geometry) {
        is GeoJsonPoint -> {
            shaped("Point", position(geometry.coordinates))
        }

        is GeoJsonMultiPoint -> {
            shaped("MultiPoint", line(geometry.coordinates))
        }

        is GeoJsonLineString -> {
            shaped("LineString", line(geometry.coordinates))
        }

        is GeoJsonMultiLineString -> {
            shaped("MultiLineString", rings(geometry.coordinates))
        }

        is GeoJsonPolygon -> {
            shaped("Polygon", rings(geometry.coordinates))
        }

        is GeoJsonMultiPolygon -> {
            shaped("MultiPolygon", FixtureValue.Array(geometry.coordinates.map(::rings)))
        }

        is GeoJsonGeometryCollection -> {
            FixtureValue.Object(
                listOf(
                    "geometries" to FixtureValue.Array(geometry.geometries.map(::writtenGeometry)),
                    "type" to FixtureValue.Text("GeometryCollection"),
                ),
            )
        }
    }
}
