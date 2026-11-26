package com.mountaya.client

import kotlinx.serialization.Serializable

/**
 * GeoJsonPoint is RFC 7946's `Point` geometry. Point is one position.
 *
 * On the wire it is an object whose `type` is `Point`, and that member is written for it.
 */
@Serializable(with = GeoJsonPointSerializer::class)
public class GeoJsonPoint(
    /** A single position: `[longitude, latitude]`, with an optional third value carrying altitude in meters. */
    public val coordinates: List<Double>,
    /** The bounding box RFC 7946 lets any GeoJSON object carry, or null where it carries none. */
    public val bbox: List<Double>? = null,
) : GeoJsonGeometry {
    override fun equals(other: Any?): Boolean =
        other is GeoJsonPoint && other.coordinates == coordinates && other.bbox == bbox

    override fun hashCode(): Int = coordinates.hashCode() * 31 + bbox.hashCode()

    override fun toString(): String = "GeoJsonPoint($coordinates" + (bbox?.let { ", bbox=$it" } ?: "") + ")"
}
