package com.mountaya.geo

import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GeoJsonFeatureCollectionTest {
    @Test
    fun twoCollectionsAreEqualWhereTheyHoldTheSameFeaturesInOrder() {
        val a = GeoJsonFeature(null, JsonObject(emptyMap()))
        assertEquals(GeoJsonFeatureCollection(listOf(a)), GeoJsonFeatureCollection(listOf(a)))
        assertNotEquals(GeoJsonFeatureCollection(listOf(a)), GeoJsonFeatureCollection(emptyList()))
    }
}
