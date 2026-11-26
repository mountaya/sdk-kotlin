package com.mountaya.client

import kotlinx.serialization.Serializable

/**
 * GeoJsonLineString is RFC 7946's `LineString` geometry. LineString is a line through positions, in order, such as a route.
 *
 * On the wire it is an object whose `type` is `LineString`, and that member is written for it.
 */
@Serializable(with = GeoJsonLineStringSerializer::class)
public class GeoJsonLineString(
    /** The positions the line passes through, in order, at least two of them. */
    public val coordinates: List<List<Double>>,
    /** The bounding box RFC 7946 lets any GeoJSON object carry, or null where it carries none. */
    public val bbox: List<Double>? = null,
) : GeoJsonGeometry {
    override fun equals(other: Any?): Boolean =
        other is GeoJsonLineString && other.coordinates == coordinates && other.bbox == bbox

    override fun hashCode(): Int = coordinates.hashCode() * 31 + bbox.hashCode()

    override fun toString(): String = "GeoJsonLineString($coordinates" + (bbox?.let { ", bbox=$it" } ?: "") + ")"
}
