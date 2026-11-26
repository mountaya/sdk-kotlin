package com.mountaya.elements

import com.mountaya.client.GeoJsonGeometry

/**
 * MapTarget is what a data element writes to the map while a reader points at it: the position its cursor marker
 * stands at, and the stretch lit on the emphasis slot, each null for none.
 *
 * Two are equal where both are.
 */
internal class MapTarget(
    val lngLat: List<Double>? = null,
    val stretch: GeoJsonGeometry? = null,
) {
    override fun equals(other: Any?): Boolean = other is MapTarget && other.lngLat == lngLat && other.stretch == stretch

    override fun hashCode(): Int = lngLat.hashCode() * 31 + stretch.hashCode()

    override fun toString(): String = "MapTarget(lngLat=$lngLat, stretch=$stretch)"
}
