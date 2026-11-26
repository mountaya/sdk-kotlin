package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GeoJsonMultiLineStringTest {
    private val geometry = GeoJsonMultiLineString(listOf(listOf(listOf(6.86, 45.83), listOf(6.87, 45.84))))

    @Test
    fun itIsWrittenWithItsTypeFirst() {
        assertEquals(
            """{"type":"MultiLineString","coordinates":[[[6.86,45.83],[6.87,45.84]]]}""",
            wireJson.encodeToString(GeoJsonMultiLineString.serializer(), geometry),
        )
    }

    @Test
    fun itIsReadBackAsItselfAndAsAnyGeometry() {
        assertEquals(
            geometry,
            wireJson.decodeFromString(
                GeoJsonMultiLineString.serializer(),
                """{"type":"MultiLineString","coordinates":[[[6.86,45.83],[6.87,45.84]]]}""",
            ),
        )
        assertEquals<GeoJsonGeometry>(
            geometry,
            wireJson.decodeFromString(
                GeoJsonGeometry.serializer(),
                """{"type":"MultiLineString","coordinates":[[[6.86,45.83],[6.87,45.84]]]}""",
            ),
        )
    }

    @Test
    fun twoAreEqualWhenTheirCoordinatesAre() {
        assertEquals(GeoJsonMultiLineString(listOf(listOf(listOf(6.86, 45.83), listOf(6.87, 45.84)))), geometry)
        assertEquals(
            GeoJsonMultiLineString(listOf(listOf(listOf(6.86, 45.83), listOf(6.87, 45.84)))).hashCode(),
            geometry.hashCode(),
        )
        assertNotEquals(GeoJsonMultiLineString(emptyList()), geometry)
    }
}
