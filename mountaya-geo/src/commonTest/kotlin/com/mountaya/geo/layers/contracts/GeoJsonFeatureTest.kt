package com.mountaya.geo

import com.mountaya.client.GeoJsonPoint
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GeoJsonFeatureTest {
    @Test
    fun twoFeaturesAreEqualWhereTheyHoldTheSameGeometryAndProperties() {
        val kind = JsonObject(mapOf("mtyaKind" to JsonPrimitive("poi")))
        assertEquals(
            GeoJsonFeature(GeoJsonPoint(listOf(6.0, 45.0)), kind),
            GeoJsonFeature(GeoJsonPoint(listOf(6.0, 45.0)), kind),
        )
        assertNotEquals(
            GeoJsonFeature(GeoJsonPoint(listOf(0.0, 45.0)), kind),
            GeoJsonFeature(GeoJsonPoint(listOf(-0.0, 45.0)), kind),
        )
        assertEquals(GeoJsonFeature(null, kind).hashCode(), GeoJsonFeature(null, kind).hashCode())
    }
}
