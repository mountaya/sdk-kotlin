package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlinx.serialization.json.JsonPrimitive

/**
 * isochroneRamp is the sequential scale isochrone bands fill from, the nearest band palest, so the
 * eye goes to the dark edge, the limit of reach. A band past the last stop takes the ramp again from
 * the first, which is visibly wrong rather than subtly so.
 */
public val isochroneRamp: List<String> = listOf("#c5f6fa", "#99e9f2", "#66d9e8", "#3bc9db", "#22b8cf", "#15aabf")

/**
 * isochroneColor answers the fill of the isochrone band at [index] of an answer, its place counted
 * from 0 (`geo.layers.isochrone-color`): [isochroneRamp]'s colour there, the ramp taken again from its
 * first stop past its last, so the seventh band takes the first stop, and the first stop for an index
 * below zero, which is no place in an answer. Keyed by the band's place, never its value, so three
 * bands of a walk and three of a drive look alike.
 */
public fun isochroneColor(index: Int): String =
    // Checked before the remainder, so `Int.MIN_VALUE` is no case of its own.
    if (index < 0) isochroneRamp[0] else isochroneRamp[index % isochroneRamp.size]

/**
 * isochronesGeoJson draws [result]'s bands, one polygon feature each in the order the answer holds
 * them, which is the order they nest in, back to front: properties `area`, `center`, `reachFactor` and
 * `value`, null where the band states none, and `mtyaColor` ([isochroneColor] of its place),
 * `mtyaIndex` and `mtyaKind` `isochrone`.
 */
public fun isochronesGeoJson(result: DataSchema.IsochronesResult): GeoJsonFeatureCollection =
    GeoJsonFeatureCollection(
        result.isochrones.orEmpty().mapIndexed { index, band ->
            GeoJsonFeature(
                band.geometry,
                featureProperties(
                    "area" to numberProperty(band.area),
                    "center" to numbersProperty(band.center),
                    "reachFactor" to numberProperty(band.reachFactor),
                    "value" to numberProperty(band.value),
                    FEATURE_COLOR_PROPERTY to JsonPrimitive(isochroneColor(index)),
                    FEATURE_INDEX_PROPERTY to JsonPrimitive(index),
                    FEATURE_KIND_PROPERTY to JsonPrimitive("isochrone"),
                ),
            )
        },
    )
