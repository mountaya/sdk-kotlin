package com.mountaya.elements

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.math.abs
import kotlin.math.floor

/** The whole numbers MapLibre Android writes back as their digits alone, every one a double holds exactly. */
private const val WHOLE_IDS_BELOW = 9.007199254740992e15

/**
 * writtenFeatureIds reads, off a style's document, [style], the id of every feature its GeoJSON sources hold inline, as
 * the document writes it: by source, then by the text MapLibre Android hands the id back as, a number or a text
 * (`map.query.feature-shape`). MapLibre Android writes every feature's id as text, so a number a GeoJSON source gives
 * is read back here; a source whose data is an address holds no feature the document writes, and a document that does
 * not parse holds none.
 */
internal fun writtenFeatureIds(style: String): Map<String, Map<String, QueriedFeature.Id>> {
    val document =
        try {
            Json.parseToJsonElement(style) as? JsonObject
        } catch (unreadable: SerializationException) {
            null
        } ?: return emptyMap()
    val sources = document["sources"] as? JsonObject ?: return emptyMap()
    return sources
        .mapNotNull { (id, source) ->
            val written = source as? JsonObject ?: return@mapNotNull null
            if ((written["type"] as? JsonPrimitive)?.content != "geojson") return@mapNotNull null
            val ids = featuresOf(written["data"]).mapNotNull { writtenId(it) }.toMap()
            if (ids.isEmpty()) null else id to ids
        }.toMap()
}

/** The features [data] holds inline: a FeatureCollection's, or a Feature alone, and none for an address. */
private fun featuresOf(data: JsonElement?): List<JsonObject> {
    val written = data as? JsonObject ?: return emptyList()
    return when ((written["type"] as? JsonPrimitive)?.content) {
        "FeatureCollection" -> (written["features"] as? JsonArray).orEmpty().filterIsInstance<JsonObject>()
        "Feature" -> listOf(written)
        else -> emptyList()
    }
}

/**
 * The id [feature] writes, keyed by the text MapLibre Android hands it back as: a number's own digits, a whole one
 * without a fraction, and a text as written; null where it writes none.
 */
private fun writtenId(feature: JsonObject): Pair<String, QueriedFeature.Id>? {
    val id = feature["id"] as? JsonPrimitive ?: return null
    if (id.isString) return id.content to QueriedFeature.Id.Text(id.content)
    val number = id.content.toDoubleOrNull() ?: return null
    val isWhole = number == floor(number) && abs(number) < WHOLE_IDS_BELOW
    return (if (isWhole) number.toLong().toString() else id.content) to QueriedFeature.Id.Number(number)
}
