package com.mountaya.client

import kotlinx.serialization.Serializable

/**
 * GeoJsonGeometry is any RFC 7946 geometry, which the REST API's `GeoJSON*` components and the
 * Data API's `GeoJSON` scalar both carry, so a geometry read from either surface is one Kotlin type.
 *
 * It is sealed, unlike the contract's own unions, because RFC 7946 rather than the contract defines
 * the set, and the set is closed there: a `when` over the seven geometries is exhaustive and stays
 * so. A position is `[longitude, latitude]` in WGS 84 decimal degrees, with an optional third
 * value carrying altitude in meters.
 */
@Serializable(with = GeoJsonGeometrySerializer::class)
public sealed interface GeoJsonGeometry
