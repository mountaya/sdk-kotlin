package com.mountaya.client

import kotlinx.serialization.Serializable

/**
 * GeoJsonMultiPolygon is RFC 7946's `MultiPolygon` geometry. MultiPolygon is several areas.
 *
 * On the wire it is an object whose `type` is `MultiPolygon`, and that member is written for it.
 */
@Serializable(with = GeoJsonMultiPolygonSerializer::class)
public class GeoJsonMultiPolygon(
    /** The polygons, each a list of linear rings. */
    public val coordinates: List<List<List<List<Double>>>>,
    /** The bounding box RFC 7946 lets any GeoJSON object carry, or null where it carries none. */
    public val bbox: List<Double>? = null,
) : GeoJsonGeometry {
    override fun equals(other: Any?): Boolean =
        other is GeoJsonMultiPolygon && other.coordinates == coordinates && other.bbox == bbox

    override fun hashCode(): Int = coordinates.hashCode() * 31 + bbox.hashCode()

    override fun toString(): String = "GeoJsonMultiPolygon($coordinates" + (bbox?.let { ", bbox=$it" } ?: "") + ")"
}
