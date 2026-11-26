package com.mountaya.geo

import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.generated.DataSchema
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class DirectionsGeoJsonTest {
    @Test
    fun eachRouteIsALineCarryingItsMetrics() {
        val line = GeoJsonLineString(listOf(listOf(0.0, 0.0), listOf(1.0, 1.0)))
        val routes =
            DataSchema.DirectionsResult {
                routes =
                    listOf(
                        DataSchema.DirectionsRoute {
                            distance = 1200.0
                            duration = 900.0
                            summary = "Via the col"
                            geometry =
                                line
                        },
                        DataSchema.DirectionsRoute(),
                    )
            }
        val features = directionsGeoJson(routes).features
        assertEquals(line, features[0].geometry)
        assertEquals(JsonPrimitive("Via the col"), features[0].properties["summary"])
        assertEquals(JsonPrimitive("directions"), features[1].properties[FEATURE_KIND_PROPERTY])
        assertEquals(JsonNull, features[1].properties["summary"])
        assertEquals(null, features[1].geometry)
    }
}
