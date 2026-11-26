package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GeoJsonMultiPointTest {
    private val geometry = GeoJsonMultiPoint(listOf(listOf(6.86, 45.83), listOf(6.87, 45.84)))

    @Test
    fun itIsWrittenWithItsTypeFirst() {
        assertEquals(
            """{"type":"MultiPoint","coordinates":[[6.86,45.83],[6.87,45.84]]}""",
            wireJson.encodeToString(GeoJsonMultiPoint.serializer(), geometry),
        )
    }

    @Test
    fun itIsReadBackAsItselfAndAsAnyGeometry() {
        assertEquals(
            geometry,
            wireJson.decodeFromString(
                GeoJsonMultiPoint.serializer(),
                """{"type":"MultiPoint","coordinates":[[6.86,45.83],[6.87,45.84]]}""",
            ),
        )
        assertEquals<GeoJsonGeometry>(
            geometry,
            wireJson.decodeFromString(
                GeoJsonGeometry.serializer(),
                """{"type":"MultiPoint","coordinates":[[6.86,45.83],[6.87,45.84]]}""",
            ),
        )
    }

    @Test
    fun twoAreEqualWhenTheirCoordinatesAre() {
        assertEquals(GeoJsonMultiPoint(listOf(listOf(6.86, 45.83), listOf(6.87, 45.84))), geometry)
        assertEquals(
            GeoJsonMultiPoint(listOf(listOf(6.86, 45.83), listOf(6.87, 45.84))).hashCode(),
            geometry.hashCode(),
        )
        assertNotEquals(GeoJsonMultiPoint(emptyList()), geometry)
    }
}
