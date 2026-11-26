package com.mountaya.geo

import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.generated.DataSchema
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlacesGeoJsonTest {
    @Test
    fun eachPlaceIsAPointLabelled() {
        val result =
            DataSchema.GeocodeResult {
                places =
                    listOf(
                        DataSchema.Place {
                            name = "Annecy"
                            category = DataSchema.PlaceCategory.PopulatedPlace
                            point =
                                listOf(6.13, 45.9)
                        },
                        DataSchema.Place {
                            name = "Nowhere"
                            point = listOf(6.13)
                        },
                    )
            }
        val features = placesGeoJson(result).features
        assertEquals(GeoJsonPoint(listOf(6.13, 45.9)), features[0].geometry)
        assertEquals(JsonPrimitive("POPULATED_PLACE"), features[0].properties["category"])
        assertEquals(JsonNull, features[0].properties["bounds"])
        assertEquals(JsonPrimitive(PLACE_POINT_COLOR), features[0].properties[FEATURE_COLOR_PROPERTY])
        // A place whose point names no place is drawn nowhere, its index kept.
        assertNull(features[1].geometry)
        assertEquals(JsonPrimitive(1), features[1].properties[FEATURE_INDEX_PROPERTY])
    }
}
