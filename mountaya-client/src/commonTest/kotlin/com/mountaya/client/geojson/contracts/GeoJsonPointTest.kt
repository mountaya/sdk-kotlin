package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GeoJsonPointTest {
    private val geometry = GeoJsonPoint(listOf(6.86, 45.83, 1035.0))

    @Test
    fun itIsWrittenWithItsTypeFirst() {
        assertEquals(
            """{"type":"Point","coordinates":[6.86,45.83,1035.0]}""",
            wireJson.encodeToString(GeoJsonPoint.serializer(), geometry),
        )
    }

    @Test
    fun itIsReadBackAsItselfAndAsAnyGeometry() {
        assertEquals(
            geometry,
            wireJson.decodeFromString(
                GeoJsonPoint.serializer(),
                """{"type":"Point","coordinates":[6.86,45.83,1035.0]}""",
            ),
        )
        assertEquals<GeoJsonGeometry>(
            geometry,
            wireJson.decodeFromString(
                GeoJsonGeometry.serializer(),
                """{"type":"Point","coordinates":[6.86,45.83,1035.0]}""",
            ),
        )
    }

    @Test
    fun twoAreEqualWhenTheirCoordinatesAre() {
        assertEquals(GeoJsonPoint(listOf(6.86, 45.83, 1035.0)), geometry)
        assertEquals(GeoJsonPoint(listOf(6.86, 45.83, 1035.0)).hashCode(), geometry.hashCode())
        assertNotEquals(GeoJsonPoint(listOf(6.86, 45.83)), geometry)
    }
}
