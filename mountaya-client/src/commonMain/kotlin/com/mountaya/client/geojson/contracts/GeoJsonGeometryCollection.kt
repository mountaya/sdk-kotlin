package com.mountaya.client

import kotlinx.serialization.Serializable

/**
 * GeoJsonGeometryCollection is RFC 7946's `GeometryCollection` geometry. GeometryCollection is several geometries of any type, as one.
 *
 * On the wire it is an object whose `type` is `GeometryCollection`, and that member is written for it.
 */
@Serializable(with = GeoJsonGeometryCollectionSerializer::class)
public class GeoJsonGeometryCollection(
    /** The geometries, each distinguished by its own `type` member. RFC 7946 does not nest one collection inside another. */
    public val geometries: List<GeoJsonGeometry>,
    /** The bounding box RFC 7946 lets any GeoJSON object carry, or null where it carries none. */
    public val bbox: List<Double>? = null,
) : GeoJsonGeometry {
    override fun equals(other: Any?): Boolean =
        other is GeoJsonGeometryCollection && other.geometries == geometries && other.bbox == bbox

    override fun hashCode(): Int = geometries.hashCode() * 31 + bbox.hashCode()

    override fun toString(): String = "GeoJsonGeometryCollection($geometries" + (bbox?.let { ", bbox=$it" } ?: "") + ")"
}
