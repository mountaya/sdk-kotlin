package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometryCollection
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.GeoJsonMultiLineString
import com.mountaya.client.GeoJsonMultiPoint
import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.GeoJsonPolygon
import kotlin.test.Test
import kotlin.test.assertEquals

class LinesOfTest {
    private val first = listOf(listOf(6.0, 45.0), listOf(6.1, 45.0))
    private val second = listOf(listOf(7.0, 46.0), listOf(7.1, 46.0))

    @Test
    fun everyLineIsReadInTheOrderTheGeometryWritesIt() {
        assertEquals(listOf(first), linesOf(GeoJsonLineString(first)))
        assertEquals(listOf(first, second), linesOf(GeoJsonMultiLineString(listOf(first, second))))
        assertEquals(
            listOf(first, second),
            linesOf(
                GeoJsonGeometryCollection(
                    listOf(
                        GeoJsonLineString(first),
                        GeoJsonMultiPoint(listOf(listOf(6.05, 45.0))),
                        GeoJsonGeometryCollection(listOf(GeoJsonLineString(second))),
                    ),
                ),
            ),
        )
    }

    @Test
    fun aPointAPolygonOrNoGeometryHoldsNoLine() {
        assertEquals(emptyList(), linesOf(null))
        assertEquals(emptyList(), linesOf(GeoJsonPoint(listOf(6.0, 45.0))))
        assertEquals(emptyList(), linesOf(GeoJsonPolygon(listOf(first + listOf(listOf(6.0, 45.0))))))
    }
}
