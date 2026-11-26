package com.mountaya.client

import kotlinx.serialization.Serializable

/**
 * GeoJsonPolygon is RFC 7946's `Polygon` geometry. Polygon is an area, bounded by its exterior ring less any holes.
 *
 * On the wire it is an object whose `type` is `Polygon`, and that member is written for it.
 */
@Serializable(with = GeoJsonPolygonSerializer::class)
public class GeoJsonPolygon(
    /** The linear rings, exterior first and any others holes, each closed: at least four positions, the last repeating the first. */
    public val coordinates: List<List<List<Double>>>,
    /** The bounding box RFC 7946 lets any GeoJSON object carry, or null where it carries none. */
    public val bbox: List<Double>? = null,
) : GeoJsonGeometry {
    override fun equals(other: Any?): Boolean =
        other is GeoJsonPolygon && other.coordinates == coordinates && other.bbox == bbox

    override fun hashCode(): Int = coordinates.hashCode() * 31 + bbox.hashCode()

    override fun toString(): String = "GeoJsonPolygon($coordinates" + (bbox?.let { ", bbox=$it" } ?: "") + ")"
}
