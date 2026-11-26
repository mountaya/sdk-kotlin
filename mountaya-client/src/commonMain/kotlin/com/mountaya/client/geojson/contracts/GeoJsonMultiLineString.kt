package com.mountaya.client

import kotlinx.serialization.Serializable

/**
 * GeoJsonMultiLineString is RFC 7946's `MultiLineString` geometry. MultiLineString is several lines that are not joined.
 *
 * On the wire it is an object whose `type` is `MultiLineString`, and that member is written for it.
 */
@Serializable(with = GeoJsonMultiLineStringSerializer::class)
public class GeoJsonMultiLineString(
    /** The lines, each a list of positions in order. */
    public val coordinates: List<List<List<Double>>>,
    /** The bounding box RFC 7946 lets any GeoJSON object carry, or null where it carries none. */
    public val bbox: List<Double>? = null,
) : GeoJsonGeometry {
    override fun equals(other: Any?): Boolean =
        other is GeoJsonMultiLineString && other.coordinates == coordinates && other.bbox == bbox

    override fun hashCode(): Int = coordinates.hashCode() * 31 + bbox.hashCode()

    override fun toString(): String = "GeoJsonMultiLineString($coordinates" + (bbox?.let { ", bbox=$it" } ?: "") + ")"
}
