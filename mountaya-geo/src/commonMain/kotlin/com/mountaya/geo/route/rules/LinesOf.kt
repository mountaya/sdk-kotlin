package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.GeoJsonGeometryCollection
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.GeoJsonMultiLineString
import com.mountaya.client.GeoJsonMultiPoint
import com.mountaya.client.GeoJsonMultiPolygon
import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.GeoJsonPolygon

/**
 * Returns the lines [geometry] holds, in the order it writes them, each a list of positions: a
 * LineString is one line and each line of a MultiLineString is one, a GeometryCollection holds the
 * lines of what it holds at any depth, and a point, a MultiPoint (a directions segment's vias) or a
 * polygon is no line. No geometry holds none.
 *
 * The one statement of what a line of an itinerary is, which every walk of one reads.
 */
internal fun linesOf(geometry: GeoJsonGeometry?): List<List<List<Double>>> =
    when (geometry) {
        null -> emptyList()
        is GeoJsonLineString -> listOf(geometry.coordinates)
        is GeoJsonMultiLineString -> geometry.coordinates
        is GeoJsonGeometryCollection -> geometry.geometries.flatMap(::linesOf)
        is GeoJsonPoint, is GeoJsonMultiPoint, is GeoJsonPolygon, is GeoJsonMultiPolygon -> emptyList()
    }
