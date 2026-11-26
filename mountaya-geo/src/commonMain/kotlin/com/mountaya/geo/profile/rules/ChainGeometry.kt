package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.GeoJsonGeometryCollection

/**
 * chainGeometry is the one geometry a chain of [legs] is profiled as: a GeometryCollection of each leg's geometry
 * in the chain's order, a leg with none left out, null where none carries one. The chain is also the curve, so a
 * profile handed legs draws the line they come from and its chips can never describe a line they were not cut
 * from (`sdk/SPEC.md` section 7).
 */
public fun chainGeometry(legs: List<ItineraryLeg>): GeoJsonGeometry? {
    val geometries = legs.mapNotNull { it.geometry }
    if (geometries.isEmpty()) return null
    return GeoJsonGeometryCollection(geometries)
}
