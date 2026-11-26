package com.mountaya.client

import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class GeoJsonGeometrySerializerTest {
    @Test
    fun aGeometryIsReadByItsType() {
        val read =
            wireJson.decodeFromString(
                GeoJsonGeometrySerializer,
                """{"coordinates":[[6.86,45.83]],"type":"LineString"}""",
            )

        assertEquals(GeoJsonLineString(listOf(listOf(6.86, 45.83))), assertIs<GeoJsonLineString>(read))
    }

    @Test
    fun aTypeOutsideRfc7946IsRefused() {
        for (text in listOf(
            """{"type":"Feature","geometry":null}""",
            """{"type":"point","coordinates":[0,0]}""",
            """{"coordinates":[0,0]}""",
        )) {
            assertFailsWith<SerializationException>(text) { wireJson.decodeFromString(GeoJsonGeometrySerializer, text) }
        }
    }

    @Test
    fun aPositionHoldsNumbers() {
        assertFailsWith<SerializationException> {
            wireJson.decodeFromString(GeoJsonGeometrySerializer, """{"type":"Point","coordinates":["6.86","45.83"]}""")
        }
    }

    @Test
    fun aSerializerForOneGeometryRefusesAnother() {
        assertFailsWith<SerializationException> {
            wireJson.decodeFromString(GeoJsonPointSerializer, """{"type":"LineString","coordinates":[[0,0],[1,1]]}""")
        }
        assertIs<GeoJsonPoint>(
            wireJson.decodeFromString(GeoJsonPointSerializer, """{"type":"Point","coordinates":[0,0]}"""),
        )
    }

    @Test
    fun anIntegerCoordinateIsReadAsANumber() {
        val point =
            wireJson.decodeFromString<GeoJsonGeometry>(
                GeoJsonGeometrySerializer,
                """{"type":"Point","coordinates":[6,45]}""",
            )

        assertEquals(GeoJsonPoint(listOf(6.0, 45.0)), point)
    }

    @Test
    fun everyGeometryCarriesItsBoundingBoxAndANullOneIsNone() {
        val box = listOf(0.0, 0.0, 1.0, 1.0)
        val read =
            listOf(
                """{"type":"Point","coordinates":[0,0],"bbox":[0,0,1,1]}""",
                """{"type":"MultiPoint","coordinates":[[0,0]],"bbox":[0,0,1,1]}""",
                """{"type":"MultiLineString","coordinates":[[[0,0],[1,1]]],"bbox":[0,0,1,1]}""",
                """{"type":"Polygon","coordinates":[[[0,0],[1,0],[1,1],[0,0]]],"bbox":[0,0,1,1]}""",
                """{"type":"MultiPolygon","coordinates":[[[[0,0],[1,0],[1,1],[0,0]]]],"bbox":[0,0,1,1]}""",
                """{"type":"GeometryCollection","geometries":[],"bbox":[0,0,1,1]}""",
            ).map { wireJson.decodeFromString(GeoJsonGeometry.serializer(), it) }

        assertEquals(
            listOf(box, box, box, box, box, box),
            read.map { geometry ->
                when (geometry) {
                    is GeoJsonPoint -> geometry.bbox
                    is GeoJsonMultiPoint -> geometry.bbox
                    is GeoJsonLineString -> geometry.bbox
                    is GeoJsonMultiLineString -> geometry.bbox
                    is GeoJsonPolygon -> geometry.bbox
                    is GeoJsonMultiPolygon -> geometry.bbox
                    is GeoJsonGeometryCollection -> geometry.bbox
                }
            },
        )
        assertEquals(
            null,
            (
                wireJson.decodeFromString(
                    GeoJsonGeometry.serializer(),
                    """{"type":"Point","coordinates":[0,0],"bbox":null}""",
                ) as GeoJsonPoint
            ).bbox,
        )
    }
}
