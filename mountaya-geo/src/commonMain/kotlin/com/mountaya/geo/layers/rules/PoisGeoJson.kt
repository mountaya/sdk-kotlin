package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlinx.serialization.json.JsonPrimitive

/** POI_RESULT_POINT_COLOR is the fill of a point of interest's marker. */
public const val POI_RESULT_POINT_COLOR: String = "#1c7ed6"

/**
 * poisGeoJson draws [result]'s points of interest, one point feature each in its order, no geometry
 * for one whose point names no place: properties `category`, `distanceMeters`, `elevation` and `name`,
 * null where the point states none, `mtyaColor` [POI_RESULT_POINT_COLOR], `mtyaIndex` and `mtyaKind`
 * `poi`.
 */
public fun poisGeoJson(result: DataSchema.PoisResult): GeoJsonFeatureCollection =
    GeoJsonFeatureCollection(
        result.pois.orEmpty().mapIndexed { index, poi ->
            GeoJsonFeature(
                pointOrNull(poi.point),
                featureProperties(
                    "category" to textProperty(poi.category?.value),
                    "distanceMeters" to numberProperty(poi.distanceMeters),
                    "elevation" to numberProperty(poi.elevation),
                    "name" to textProperty(poi.name),
                    FEATURE_COLOR_PROPERTY to JsonPrimitive(POI_RESULT_POINT_COLOR),
                    FEATURE_INDEX_PROPERTY to JsonPrimitive(index),
                    FEATURE_KIND_PROPERTY to JsonPrimitive("poi"),
                ),
            )
        },
    )
