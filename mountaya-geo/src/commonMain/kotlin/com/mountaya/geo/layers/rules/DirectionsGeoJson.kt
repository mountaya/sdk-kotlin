package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlinx.serialization.json.JsonPrimitive

/**
 * directionsGeoJson draws each route [result] answers, one line feature each in its order: properties
 * `distance`, `duration` and `summary`, null where the route states none, `mtyaIndex` and `mtyaKind`
 * `directions`.
 */
public fun directionsGeoJson(result: DataSchema.DirectionsResult): GeoJsonFeatureCollection =
    GeoJsonFeatureCollection(
        result.routes.orEmpty().mapIndexed { index, route ->
            GeoJsonFeature(
                route.geometry,
                featureProperties(
                    "distance" to numberProperty(route.distance),
                    "duration" to numberProperty(route.duration),
                    "summary" to textProperty(route.summary),
                    FEATURE_INDEX_PROPERTY to JsonPrimitive(index),
                    FEATURE_KIND_PROPERTY to JsonPrimitive("directions"),
                ),
            )
        },
    )
