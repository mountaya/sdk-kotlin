package com.mountaya.geo

import com.mountaya.client.GeoJsonPoint
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * FEATURE_KIND_PROPERTY is the property naming what a drawn Data API feature is: `isochrone`, `suggestion`,
 * `matrix-connector`.
 */
public const val FEATURE_KIND_PROPERTY: String = "mtyaKind"

/** FEATURE_INDEX_PROPERTY is the property holding a drawn feature's place in the answer it came from. */
public const val FEATURE_INDEX_PROPERTY: String = "mtyaIndex"

/**
 * FEATURE_COLOR_PROPERTY is the property holding the colour a drawn feature paints in, where its kind is
 * coloured.
 */
public const val FEATURE_COLOR_PROPERTY: String = "mtyaColor"

/** FEATURE_WIDTH_PROPERTY is the property holding the width a matrix connector draws at, in pixels. */
public const val FEATURE_WIDTH_PROPERTY: String = "mtyaWidth"

/** The properties of a feature, [entries] in order, each member the datum states or null where it states none. */
internal fun featureProperties(vararg entries: Pair<String, JsonElement>): JsonObject =
    JsonObject(linkedMapOf(*entries))

/** [value] as a property, null where the datum states none. */
internal fun numberProperty(value: Number?): JsonElement = if (value == null) JsonNull else JsonPrimitive(value)

/** [value] as a property, null where the datum states none. */
internal fun textProperty(value: String?): JsonElement = if (value == null) JsonNull else JsonPrimitive(value)

/** [values] as a property, an array of numbers, null where the datum states none. */
internal fun numbersProperty(values: List<Double>?): JsonElement =
    if (values ==
        null
    ) {
        JsonNull
    } else {
        JsonArray(values.map { JsonPrimitive(it) })
    }

/** A point at [position], or no geometry where it names no place. */
internal fun pointOrNull(position: List<Double>?): GeoJsonPoint? =
    position?.takeIf { namesPlace(it) }?.let(::GeoJsonPoint)
