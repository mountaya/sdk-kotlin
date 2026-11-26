package com.mountaya.client

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

/**
 * GeoJsonGeometrySerializer reads any of RFC 7946's seven geometries by its `type` member, and
 * writes one with that member first.
 *
 * GeoJSON is JSON by definition, so it reads and writes JSON alone. A `type` outside the seven
 * fails the decode: RFC 7946 closes the set, and the Data API refuses any other.
 */
internal object GeoJsonGeometrySerializer : KSerializer<GeoJsonGeometry> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("com.mountaya.client.GeoJsonGeometry")

    override fun serialize(
        encoder: Encoder,
        value: GeoJsonGeometry,
    ) {
        jsonEncoder(encoder).encodeJsonElement(write(value))
    }

    override fun deserialize(decoder: Decoder): GeoJsonGeometry = read(jsonDecoder(decoder).decodeJsonElement())
}

/** GeoJsonPointSerializer reads and writes a [GeoJsonPoint], and fails on any other geometry. */
internal object GeoJsonPointSerializer : KSerializer<GeoJsonPoint> by typed("Point")

/** GeoJsonMultiPointSerializer reads and writes a [GeoJsonMultiPoint], and fails on any other geometry. */
internal object GeoJsonMultiPointSerializer : KSerializer<GeoJsonMultiPoint> by typed("MultiPoint")

/** GeoJsonLineStringSerializer reads and writes a [GeoJsonLineString], and fails on any other geometry. */
internal object GeoJsonLineStringSerializer : KSerializer<GeoJsonLineString> by typed("LineString")

/** GeoJsonMultiLineStringSerializer reads and writes a [GeoJsonMultiLineString], and fails on any other geometry. */
internal object GeoJsonMultiLineStringSerializer : KSerializer<GeoJsonMultiLineString> by typed("MultiLineString")

/** GeoJsonPolygonSerializer reads and writes a [GeoJsonPolygon], and fails on any other geometry. */
internal object GeoJsonPolygonSerializer : KSerializer<GeoJsonPolygon> by typed("Polygon")

/** GeoJsonMultiPolygonSerializer reads and writes a [GeoJsonMultiPolygon], and fails on any other geometry. */
internal object GeoJsonMultiPolygonSerializer : KSerializer<GeoJsonMultiPolygon> by typed("MultiPolygon")

/** GeoJsonGeometryCollectionSerializer reads and writes a [GeoJsonGeometryCollection], and fails on any other geometry. */
internal object GeoJsonGeometryCollectionSerializer :
    KSerializer<GeoJsonGeometryCollection> by typed("GeometryCollection")

/** A serializer for the one geometry whose `type` is [type]. */
private inline fun <reified T : GeoJsonGeometry> typed(type: String): KSerializer<T> =
    object : KSerializer<T> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("com.mountaya.client.GeoJson$type")

        override fun serialize(
            encoder: Encoder,
            value: T,
        ) {
            jsonEncoder(encoder).encodeJsonElement(write(value))
        }

        override fun deserialize(decoder: Decoder): T {
            val geometry = read(jsonDecoder(decoder).decodeJsonElement())
            return geometry as? T ?: fail("a GeoJSON `$type` was expected here, and a `${typeOf(geometry)}` arrived")
        }
    }

private fun read(element: JsonElement): GeoJsonGeometry {
    val geometry = element as? JsonObject ?: fail("a GeoJSON geometry is a JSON object")
    val type = (geometry["type"] as? JsonPrimitive)?.takeIf { it.isString }?.content
    return when (type) {
        "Point" -> {
            GeoJsonPoint(position(member(geometry, COORDINATES)), bbox(geometry))
        }

        "MultiPoint" -> {
            GeoJsonMultiPoint(array(member(geometry, COORDINATES)).map(::position), bbox(geometry))
        }

        "LineString" -> {
            GeoJsonLineString(array(member(geometry, COORDINATES)).map(::position), bbox(geometry))
        }

        "MultiLineString" -> {
            GeoJsonMultiLineString(array(member(geometry, COORDINATES)).map(::positions), bbox(geometry))
        }

        "Polygon" -> {
            GeoJsonPolygon(array(member(geometry, COORDINATES)).map(::positions), bbox(geometry))
        }

        "MultiPolygon" -> {
            GeoJsonMultiPolygon(
                array(member(geometry, COORDINATES)).map { polygon ->
                    array(polygon).map(::positions)
                },
                bbox(geometry),
            )
        }

        "GeometryCollection" -> {
            GeoJsonGeometryCollection(array(member(geometry, GEOMETRIES)).map(::read), bbox(geometry))
        }

        null -> {
            fail("a GeoJSON geometry carries its `type` as a string")
        }

        else -> {
            fail("`$type` is not one of RFC 7946's seven geometries")
        }
    }
}

private fun write(geometry: GeoJsonGeometry): JsonObject {
    val members =
        when (geometry) {
            is GeoJsonPoint -> {
                mapOf(TYPE to JsonPrimitive("Point"), COORDINATES to numbers(geometry.coordinates))
            }

            is GeoJsonMultiPoint -> {
                coordinates("MultiPoint", JsonArray(geometry.coordinates.map(::numbers)))
            }

            is GeoJsonLineString -> {
                coordinates("LineString", JsonArray(geometry.coordinates.map(::numbers)))
            }

            is GeoJsonMultiLineString -> {
                coordinates("MultiLineString", nested(geometry.coordinates))
            }

            is GeoJsonPolygon -> {
                coordinates("Polygon", nested(geometry.coordinates))
            }

            is GeoJsonMultiPolygon -> {
                coordinates("MultiPolygon", JsonArray(geometry.coordinates.map(::nested)))
            }

            is GeoJsonGeometryCollection -> {
                mapOf(
                    TYPE to JsonPrimitive("GeometryCollection"),
                    GEOMETRIES to JsonArray(geometry.geometries.map(::write)),
                )
            }
        }
    val box = boxOf(geometry) ?: return JsonObject(members)
    return JsonObject(members + (BBOX to numbers(box)))
}

/** The bounding box [geometry] carries, or null where it carries none. */
private fun boxOf(geometry: GeoJsonGeometry): List<Double>? =
    when (geometry) {
        is GeoJsonPoint -> geometry.bbox
        is GeoJsonMultiPoint -> geometry.bbox
        is GeoJsonLineString -> geometry.bbox
        is GeoJsonMultiLineString -> geometry.bbox
        is GeoJsonPolygon -> geometry.bbox
        is GeoJsonMultiPolygon -> geometry.bbox
        is GeoJsonGeometryCollection -> geometry.bbox
    }

/** The bounding box a geometry's object carries, or null where it carries none or `null`. */
private fun bbox(geometry: JsonObject): List<Double>? = geometry[BBOX]?.takeUnless { it is JsonNull }?.let(::position)

private const val TYPE = "type"
private const val COORDINATES = "coordinates"
private const val GEOMETRIES = "geometries"
private const val BBOX = "bbox"

private fun coordinates(
    type: String,
    value: JsonArray,
): Map<String, JsonElement> = mapOf(TYPE to JsonPrimitive(type), COORDINATES to value)

private fun numbers(position: List<Double>): JsonArray = JsonArray(position.map { JsonPrimitive(it) })

private fun nested(lines: List<List<List<Double>>>): JsonArray =
    JsonArray(lines.map { line -> JsonArray(line.map(::numbers)) })

private fun member(
    geometry: JsonObject,
    name: String,
): JsonElement = geometry[name] ?: fail("a GeoJSON geometry of this type carries `$name`")

private fun array(element: JsonElement): JsonArray =
    element as? JsonArray ?: fail("a GeoJSON coordinate list is a JSON array")

private fun positions(element: JsonElement): List<List<Double>> = array(element).map(::position)

private fun position(element: JsonElement): List<Double> =
    array(element).map { number ->
        (number as? JsonPrimitive)?.takeUnless { it.isString }?.doubleOrNull ?: fail("a GeoJSON position holds numbers")
    }

private fun typeOf(geometry: GeoJsonGeometry): String =
    when (geometry) {
        is GeoJsonPoint -> "Point"
        is GeoJsonMultiPoint -> "MultiPoint"
        is GeoJsonLineString -> "LineString"
        is GeoJsonMultiLineString -> "MultiLineString"
        is GeoJsonPolygon -> "Polygon"
        is GeoJsonMultiPolygon -> "MultiPolygon"
        is GeoJsonGeometryCollection -> "GeometryCollection"
    }

private fun jsonDecoder(decoder: Decoder): JsonDecoder =
    decoder as? JsonDecoder ?: fail("GeoJSON is read from JSON only")

private fun jsonEncoder(encoder: Encoder): JsonEncoder =
    encoder as? JsonEncoder ?: fail("GeoJSON is written as JSON only")

private fun fail(reason: String): Nothing = throw SerializationException("$reason.")
