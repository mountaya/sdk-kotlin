package com.mountaya.geo

import com.mountaya.client.GeoJsonPoint
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FeaturePropertiesTest {
    @Test
    fun aPropertyTheDatumDoesNotStateIsNull() {
        assertEquals(JsonNull, numberProperty(null))
        assertEquals(JsonNull, textProperty(null))
        assertEquals(JsonNull, numbersProperty(null))
        assertEquals(JsonArray(listOf(JsonPrimitive(6.0), JsonPrimitive(45.0))), numbersProperty(listOf(6.0, 45.0)))
        assertEquals(listOf("a", "b"), featureProperties("a" to JsonNull, "b" to JsonNull).keys.toList())
    }

    @Test
    fun aPointIsDrawnOnlyWhereItNamesAPlace() {
        assertEquals(GeoJsonPoint(listOf(6.0, 45.0, 1200.0)), pointOrNull(listOf(6.0, 45.0, 1200.0)))
        assertNull(pointOrNull(listOf(6.0)))
        assertNull(pointOrNull(null))
        assertEquals("mtyaKind", FEATURE_KIND_PROPERTY)
    }
}
