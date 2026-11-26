package com.mountaya.geo

/**
 * GeoJsonFeatureCollection is RFC 7946's `FeatureCollection`: the [features] a renderer draws from
 * one source, in the order they draw.
 *
 * Two are equal where they hold the same features in the same order.
 */
public class GeoJsonFeatureCollection(
    /** The features, in order. */
    public val features: List<GeoJsonFeature>,
) {
    override fun equals(other: Any?): Boolean = other is GeoJsonFeatureCollection && other.features == features

    override fun hashCode(): Int = features.hashCode()

    override fun toString(): String = "GeoJsonFeatureCollection($features)"
}
