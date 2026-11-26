package com.mountaya.client

import kotlinx.serialization.Serializable

/**
 * GeoJsonMultiPoint is RFC 7946's `MultiPoint` geometry. MultiPoint is several positions that are not joined.
 *
 * On the wire it is an object whose `type` is `MultiPoint`, and that member is written for it.
 */
@Serializable(with = GeoJsonMultiPointSerializer::class)
public class GeoJsonMultiPoint(
    /** The positions, each `[longitude, latitude]` with an optional altitude. */
    public val coordinates: List<List<Double>>,
    /** The bounding box RFC 7946 lets any GeoJSON object carry, or null where it carries none. */
    public val bbox: List<Double>? = null,
) : GeoJsonGeometry {
    override fun equals(other: Any?): Boolean =
        other is GeoJsonMultiPoint && other.coordinates == coordinates && other.bbox == bbox

    override fun hashCode(): Int = coordinates.hashCode() * 31 + bbox.hashCode()

    override fun toString(): String = "GeoJsonMultiPoint($coordinates" + (bbox?.let { ", bbox=$it" } ?: "") + ")"
}
