package com.mountaya.geo

import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.generated.DataSchema
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class PoisGeoJsonTest {
    @Test
    fun eachPoiIsAPointCarryingItsFigures() {
        val result =
            DataSchema.PoisResult {
                pois =
                    listOf(
                        DataSchema.POI {
                            name = "Café"
                            category = DataSchema.POICategory("CAFE")
                            point =
                                listOf(6.1, 45.9)
                        },
                    )
            }
        val feature = poisGeoJson(result).features.single()
        assertEquals(GeoJsonPoint(listOf(6.1, 45.9)), feature.geometry)
        assertEquals(JsonNull, feature.properties["elevation"])
        assertEquals(JsonPrimitive(POI_RESULT_POINT_COLOR), feature.properties[FEATURE_COLOR_PROPERTY])
        assertEquals(
            listOf("category", "distanceMeters", "elevation", "name", "mtyaColor", "mtyaIndex", "mtyaKind"),
            feature.properties.keys.toList(),
        )
    }
}
