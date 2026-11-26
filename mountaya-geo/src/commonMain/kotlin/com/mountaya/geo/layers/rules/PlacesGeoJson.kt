package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlinx.serialization.json.JsonPrimitive

/** PLACE_POINT_COLOR is the fill of a geocoded place's marker. */
public const val PLACE_POINT_COLOR: String = "#f59f00"

/**
 * placesGeoJson draws [result]'s geocoded places, one point feature each in its order, no geometry
 * for one whose point names no place: properties `bounds`, `category`, `label`, `layer` and `name`,
 * null where the place states none, `mtyaColor` [PLACE_POINT_COLOR], `mtyaIndex` and `mtyaKind` `place`.
 */
public fun placesGeoJson(result: DataSchema.GeocodeResult): GeoJsonFeatureCollection =
    GeoJsonFeatureCollection(
        result.places.orEmpty().mapIndexed { index, place ->
            GeoJsonFeature(
                pointOrNull(place.point),
                featureProperties(
                    "bounds" to numbersProperty(place.bounds),
                    "category" to textProperty(place.category?.value),
                    "label" to textProperty(place.label),
                    "layer" to textProperty(place.layer?.value),
                    "name" to textProperty(place.name),
                    FEATURE_COLOR_PROPERTY to JsonPrimitive(PLACE_POINT_COLOR),
                    FEATURE_INDEX_PROPERTY to JsonPrimitive(index),
                    FEATURE_KIND_PROPERTY to JsonPrimitive("place"),
                ),
            )
        },
    )
