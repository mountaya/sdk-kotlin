package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GeoJsonGeometryCollectionTest {
    private val geometry = GeoJsonGeometryCollection(listOf(GeoJsonPoint(listOf(6.86, 45.83))))

    @Test
    fun itIsWrittenWithItsTypeFirst() {
        assertEquals(
            """{"type":"GeometryCollection","geometries":[{"type":"Point","coordinates":[6.86,45.83]}]}""",
            wireJson.encodeToString(GeoJsonGeometryCollection.serializer(), geometry),
        )
    }

    @Test
    fun itIsReadBackAsItselfAndAsAnyGeometry() {
        assertEquals(
            geometry,
            wireJson.decodeFromString(
                GeoJsonGeometryCollection.serializer(),
                """{"type":"GeometryCollection","geometries":[{"type":"Point","coordinates":[6.86,45.83]}]}""",
            ),
        )
        assertEquals<GeoJsonGeometry>(
            geometry,
            wireJson.decodeFromString(
                GeoJsonGeometry.serializer(),
                """{"type":"GeometryCollection","geometries":[{"type":"Point","coordinates":[6.86,45.83]}]}""",
            ),
        )
    }

    @Test
    fun twoAreEqualWhenTheirGeometriesAre() {
        assertEquals(GeoJsonGeometryCollection(listOf(GeoJsonPoint(listOf(6.86, 45.83)))), geometry)
        assertEquals(
            GeoJsonGeometryCollection(listOf(GeoJsonPoint(listOf(6.86, 45.83)))).hashCode(),
            geometry.hashCode(),
        )
        assertNotEquals(GeoJsonGeometryCollection(emptyList()), geometry)
    }
}
