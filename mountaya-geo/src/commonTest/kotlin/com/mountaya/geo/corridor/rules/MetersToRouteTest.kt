package com.mountaya.geo

import com.mountaya.client.GeoJsonLineString
import kotlin.test.Test
import kotlin.test.assertEquals

class MetersToRouteTest {
    private val line = GeoJsonLineString(listOf(listOf(6.86, 45.85), listOf(6.9, 45.85)))

    @Test
    fun aPointBesideTheLineIsItsDistanceInTheLocalPlane() {
        assertEquals(111.1949266442996, metersToRoute(listOf(6.88, 45.851), listOf(line)), 1e-9)
        assertEquals(0.0, metersToRoute(listOf(6.88, 45.85), listOf(line)))
        assertEquals(3_872.581345548356, metersToRoute(listOf(6.95, 45.85), listOf(line)), 1e-9)
    }

    @Test
    fun theNearestSegmentOfAnyLineIsTheDistance() {
        val far = GeoJsonLineString(listOf(listOf(6.86, 45.9), listOf(6.9, 45.9)))
        val near = GeoJsonLineString(listOf(listOf(6.88, 45.8505), listOf(6.89, 45.8505)))

        assertEquals(55.59746332175475, metersToRoute(listOf(6.885, 45.85), listOf(far, near)), 1e-9)
    }

    @Test
    fun aSegmentAcrossTheAntimeridianStaysInOnePiece() {
        val across = GeoJsonLineString(listOf(listOf(-179.999, -1.0), listOf(-179.999, 1.0)))

        assertEquals(222.38985329017936, metersToRoute(listOf(179.999, 0.0), listOf(across)), 1e-9)
        assertEquals(
            0.0,
            metersToRoute(
                listOf(190.0, 0.0),
                listOf(GeoJsonLineString(listOf(listOf(-170.0, -1.0), listOf(-170.0, 1.0)))),
            ),
        )
    }

    /**
     * A segment written from 170 to -20 runs 190 degrees eastward past the antimeridian; its far
     * stretch is nearer the point a turn the other way.
     */
    @Test
    fun aLongSegmentIsMeasuredFromThePointATurnTheOtherWayWhereThatIsNearer() {
        val twenty = 20.0 * METERS_PER_DEGREE_MEAN_SPHERE

        assertEquals(
            twenty,
            metersToRoute(listOf(0.0, 0.0), listOf(GeoJsonLineString(listOf(listOf(170.0, 0.0), listOf(-20.0, 0.0))))),
            1e-6,
        )
        assertEquals(
            twenty,
            metersToRoute(listOf(0.0, 0.0), listOf(GeoJsonLineString(listOf(listOf(-20.0, 0.0), listOf(170.0, 0.0))))),
            1e-6,
        )
    }

    @Test
    fun aVertexThatNamesNoPlaceIsBridged() {
        val bridged =
            GeoJsonLineString(listOf(listOf(6.86, 45.85), listOf(6.88), listOf(Double.NaN, 45.0), listOf(6.9, 45.85)))

        assertEquals(111.1949266442996, metersToRoute(listOf(6.88, 45.851), listOf(bridged)), 1e-9)
    }

    @Test
    fun withNoSegmentToMeasureToTheDistanceIsInfinite() {
        assertEquals(Double.POSITIVE_INFINITY, metersToRoute(listOf(6.88, 45.851), emptyList()))
        assertEquals(
            Double.POSITIVE_INFINITY,
            metersToRoute(listOf(6.88, 45.851), listOf(GeoJsonLineString(listOf(listOf(6.88, 45.85))))),
        )
        assertEquals(
            Double.POSITIVE_INFINITY,
            metersToRoute(listOf(6.88, 45.851), listOf(GeoJsonLineString(emptyList()))),
        )
    }

    /** A segment never runs from one line into the next. */
    @Test
    fun twoLinesAreNeverJoined() {
        val west = GeoJsonLineString(listOf(listOf(0.0, 0.0)))
        val east = GeoJsonLineString(listOf(listOf(1.0, 0.0)))

        assertEquals(Double.POSITIVE_INFINITY, metersToRoute(listOf(0.5, 0.0), listOf(west, east)))
    }

    /** A point that names no place lies in no corridor. */
    @Test
    fun aPointThatNamesNoPlaceIsInfinitelyFar() {
        assertEquals(Double.POSITIVE_INFINITY, metersToRoute(listOf(6.88), listOf(line)))
        assertEquals(Double.POSITIVE_INFINITY, metersToRoute(listOf(Double.NaN, 45.85), listOf(line)))
    }
}
