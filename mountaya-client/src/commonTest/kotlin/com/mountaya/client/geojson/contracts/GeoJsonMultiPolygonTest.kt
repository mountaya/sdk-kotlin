package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GeoJsonMultiPolygonTest {
    private val geometry =
        GeoJsonMultiPolygon(
            listOf(listOf(listOf(listOf(0.0, 0.0), listOf(1.0, 0.0), listOf(1.0, 1.0), listOf(0.0, 0.0)))),
        )

    @Test
    fun itIsWrittenWithItsTypeFirst() {
        assertEquals(
            """{"type":"MultiPolygon","coordinates":[[[[0.0,0.0],[1.0,0.0],[1.0,1.0],[0.0,0.0]]]]}""",
            wireJson.encodeToString(GeoJsonMultiPolygon.serializer(), geometry),
        )
    }

    @Test
    fun itIsReadBackAsItselfAndAsAnyGeometry() {
        assertEquals(
            geometry,
            wireJson.decodeFromString(
                GeoJsonMultiPolygon.serializer(),
                """{"type":"MultiPolygon","coordinates":[[[[0.0,0.0],[1.0,0.0],[1.0,1.0],[0.0,0.0]]]]}""",
            ),
        )
        assertEquals<GeoJsonGeometry>(
            geometry,
            wireJson.decodeFromString(
                GeoJsonGeometry.serializer(),
                """{"type":"MultiPolygon","coordinates":[[[[0.0,0.0],[1.0,0.0],[1.0,1.0],[0.0,0.0]]]]}""",
            ),
        )
    }

    @Test
    fun twoAreEqualWhenTheirCoordinatesAre() {
        assertEquals(
            GeoJsonMultiPolygon(
                listOf(listOf(listOf(listOf(0.0, 0.0), listOf(1.0, 0.0), listOf(1.0, 1.0), listOf(0.0, 0.0)))),
            ),
            geometry,
        )
        assertEquals(
            GeoJsonMultiPolygon(
                listOf(listOf(listOf(listOf(0.0, 0.0), listOf(1.0, 0.0), listOf(1.0, 1.0), listOf(0.0, 0.0)))),
            ).hashCode(),
            geometry.hashCode(),
        )
        assertNotEquals(GeoJsonMultiPolygon(emptyList()), geometry)
    }
}
