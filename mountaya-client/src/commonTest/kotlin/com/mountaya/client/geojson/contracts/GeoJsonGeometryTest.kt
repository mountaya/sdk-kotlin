package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals

class GeoJsonGeometryTest {
    @Test
    fun aWhenOverTheSevenGeometriesIsExhaustive() {
        val geometries: List<GeoJsonGeometry> =
            listOf(
                GeoJsonPoint(emptyList()),
                GeoJsonMultiPoint(emptyList()),
                GeoJsonLineString(emptyList()),
                GeoJsonMultiLineString(emptyList()),
                GeoJsonPolygon(emptyList()),
                GeoJsonMultiPolygon(emptyList()),
                GeoJsonGeometryCollection(emptyList()),
            )

        val names =
            geometries.map { geometry ->
                when (geometry) {
                    is GeoJsonPoint -> "Point"
                    is GeoJsonMultiPoint -> "MultiPoint"
                    is GeoJsonLineString -> "LineString"
                    is GeoJsonMultiLineString -> "MultiLineString"
                    is GeoJsonPolygon -> "Polygon"
                    is GeoJsonMultiPolygon -> "MultiPolygon"
                    is GeoJsonGeometryCollection -> "GeometryCollection"
                }
            }
        assertEquals(7, names.toSet().size)
    }
}
