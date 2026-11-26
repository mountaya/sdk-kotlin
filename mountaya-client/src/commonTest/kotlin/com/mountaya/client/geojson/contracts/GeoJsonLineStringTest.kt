package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GeoJsonLineStringTest {
    private val geometry = GeoJsonLineString(listOf(listOf(6.86, 45.83), listOf(6.87, 45.84)))

    @Test
    fun itIsWrittenWithItsTypeFirst() {
        assertEquals(
            """{"type":"LineString","coordinates":[[6.86,45.83],[6.87,45.84]]}""",
            wireJson.encodeToString(GeoJsonLineString.serializer(), geometry),
        )
    }

    @Test
    fun itIsReadBackAsItselfAndAsAnyGeometry() {
        assertEquals(
            geometry,
            wireJson.decodeFromString(
                GeoJsonLineString.serializer(),
                """{"type":"LineString","coordinates":[[6.86,45.83],[6.87,45.84]]}""",
            ),
        )
        assertEquals<GeoJsonGeometry>(
            geometry,
            wireJson.decodeFromString(
                GeoJsonGeometry.serializer(),
                """{"type":"LineString","coordinates":[[6.86,45.83],[6.87,45.84]]}""",
            ),
        )
    }

    @Test
    fun twoAreEqualWhenTheirCoordinatesAre() {
        assertEquals(GeoJsonLineString(listOf(listOf(6.86, 45.83), listOf(6.87, 45.84))), geometry)
        assertEquals(
            GeoJsonLineString(listOf(listOf(6.86, 45.83), listOf(6.87, 45.84))).hashCode(),
            geometry.hashCode(),
        )
        assertNotEquals(GeoJsonLineString(emptyList()), geometry)
    }

    @Test
    fun aBoundingBoxIsReadKeptAndWrittenBack() {
        val read =
            wireJson.decodeFromString(
                GeoJsonGeometry.serializer(),
                """{"type":"LineString","coordinates":[[1,2],[3,4]],"bbox":[1,2,3,4]}""",
            )

        assertEquals(GeoJsonLineString(listOf(listOf(1.0, 2.0), listOf(3.0, 4.0)), listOf(1.0, 2.0, 3.0, 4.0)), read)
        assertEquals(
            """{"type":"LineString","coordinates":[[1.0,2.0],[3.0,4.0]],"bbox":[1.0,2.0,3.0,4.0]}""",
            wireJson.encodeToString(GeoJsonGeometry.serializer(), read),
        )
        assertNotEquals(GeoJsonLineString(listOf(listOf(1.0, 2.0), listOf(3.0, 4.0))), read)
        assertEquals("GeoJsonLineString([[1.0, 2.0], [3.0, 4.0]], bbox=[1.0, 2.0, 3.0, 4.0])", read.toString())
    }
}
