package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry
import kotlinx.serialization.json.JsonObject

/**
 * GeoJsonFeature is RFC 7946's `Feature`: a [geometry], null for a datum the answer placed nowhere,
 * and the [properties] a renderer's expressions read and a click hands back, the datum itself and
 * the drawing properties Geo stamps on it.
 *
 * Two are equal where they hold the same geometry and the same properties.
 */
public class GeoJsonFeature(
    /** Where the feature is drawn, or null where its datum states no place. */
    public val geometry: GeoJsonGeometry?,
    /** The feature's properties, as the JSON object a renderer takes. */
    public val properties: JsonObject,
) {
    override fun equals(other: Any?): Boolean =
        other is GeoJsonFeature && other.geometry == geometry && other.properties == properties

    override fun hashCode(): Int = geometry.hashCode() * 31 + properties.hashCode()

    override fun toString(): String = "GeoJsonFeature($geometry, $properties)"
}
