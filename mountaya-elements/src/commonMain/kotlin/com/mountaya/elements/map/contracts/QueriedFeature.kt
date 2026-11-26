package com.mountaya.elements

import com.mountaya.client.GeoJsonGeometry
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/**
 * QueriedFeature is a feature a point query found on a map (`map.query.feature-shape`): a GeoJSON feature,
 * its id where its source gives it one, its properties and its geometry, with the layer that drew it and the
 * source layer it was read from.
 *
 * A renderer answers a feature as an object of its own, with its whole layer, its source, a state and the
 * tile it was cut from, which no host reads and no second renderer has to give, so a query answers what
 * every renderer can. A feature two layers drew, a line and the casing under it, is answered once for each,
 * each naming its own [layer].
 *
 * Its properties holding a list or an object are left out as it is built, since a vector tile carries none
 * and a GeoJSON source's is the renderer's own encoding of it, which no two renderers write alike. Two are
 * equal where every member is the same, each JSON value as the serialization library compares it.
 */
public class QueriedFeature(
    properties: Map<String, JsonElement>,
    /** The feature's geometry, as the renderer found it. */
    public val geometry: GeoJsonGeometry,
    /** The id of the layer that drew the feature. */
    public val layer: String,
    /** The source layer the feature was read from, where its layer reads a vector source, and null otherwise. */
    public val sourceLayer: String? = null,
    /** The feature's own id, or null where its source gives it none. */
    public val id: Id? = null,
) {
    /** The feature's properties that hold a string, a number, a boolean or null. */
    public val properties: Map<String, JsonElement> = properties.filterValues { it is JsonPrimitive }

    /**
     * Id is a feature's own id, a number or a text as its source gives it, as RFC 7946 allows. Two are equal
     * where they are of one kind and hold the same number or text, a number as the same number.
     */
    public sealed interface Id {
        /** Number is an id written as a number. */
        public class Number(
            /** The number. */
            public val value: Double,
        ) : Id {
            override fun equals(other: Any?): Boolean = other is Number && other.value.toBits() == value.toBits()

            override fun hashCode(): Int = value.toBits().hashCode()

            override fun toString(): String = "Number($value)"
        }

        /** Text is an id written as a string. */
        public class Text(
            /** The text. */
            public val value: String,
        ) : Id {
            override fun equals(other: Any?): Boolean = other is Text && other.value == value

            override fun hashCode(): Int = value.hashCode()

            override fun toString(): String = "Text($value)"
        }
    }

    override fun equals(other: Any?): Boolean =
        other is QueriedFeature &&
            other.id == id &&
            other.properties == properties &&
            other.geometry == geometry &&
            other.layer == layer &&
            other.sourceLayer == sourceLayer

    override fun hashCode(): Int {
        var hash = id?.hashCode() ?: 0
        hash = 31 * hash + properties.hashCode()
        hash = 31 * hash + geometry.hashCode()
        hash = 31 * hash + layer.hashCode()
        hash = 31 * hash + (sourceLayer?.hashCode() ?: 0)
        return hash
    }

    override fun toString(): String =
        "QueriedFeature(id=$id, properties=$properties, geometry=$geometry, layer=$layer, sourceLayer=$sourceLayer)"
}
