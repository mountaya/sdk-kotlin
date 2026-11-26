package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometryCollection
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.GeoJsonMultiLineString
import com.mountaya.client.GeoJsonMultiPoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class BuildElevationSeriesTest {
    private val climb =
        listOf(
            listOf(6.8652, 45.9237, 1_035.0),
            listOf(6.8752, 45.9287, 1_120.0),
            listOf(6.8852, 45.9337, 1_118.0),
            listOf(6.8952, 45.9387, 1_290.0),
            listOf(6.9052, 45.9437, 1_240.0),
            listOf(6.9152, 45.9487, 1_480.0),
            listOf(6.9271, 45.9682, 1_252.0),
        )

    @Test
    fun eachVertexIsASampleOfDistanceAgainstAltitude() {
        val series = buildElevationSeries(GeoJsonLineString(climb))

        assertEquals(7, series.points.size)
        assertEquals(listOf(0, 1, 2, 3, 4, 5, 6), series.points.map { it.index })
        assertEquals(952.543779427426, series.points[1].distance, 1e-6)
        assertEquals(listOf(6.8752, 45.9287), series.points[1].lngLat)
        assertEquals(7_117.508508284167, series.totalDistance, 1e-6)
        assertEquals(1_035.0, series.minElevation)
        assertEquals(1_480.0, series.maxElevation)
    }

    @Test
    fun noGeometryIsAnItineraryOfNoLength() {
        val series = buildElevationSeries(null)

        assertFalse(series.hasElevation)
        assertEquals(0.0, series.totalDistance)
        assertNull(series.minElevation)
        assertNull(series.maxElevation)
    }

    /** The jump from one line to the next costs nothing; vias are no line. */
    @Test
    fun theLinesAreLaidEndToEndAndTheJumpBetweenThemCostsNothing() {
        val first = listOf(listOf(0.0, 0.0, 10.0), listOf(1.0, 0.0, 20.0))
        val second = listOf(listOf(5.0, 0.0, 30.0), listOf(6.0, 0.0, 40.0))
        val series =
            buildElevationSeries(
                GeoJsonGeometryCollection(
                    listOf(GeoJsonMultiLineString(listOf(first, second)), GeoJsonMultiPoint(listOf(listOf(9.0, 9.0)))),
                ),
            )

        assertEquals(listOf(0.0, 1.0, 1.0, 2.0), series.points.map { it.distance / METERS_PER_DEGREE_MEAN_SPHERE })
        assertEquals(2.0 * METERS_PER_DEGREE_MEAN_SPHERE, series.totalDistance)
    }

    @Test
    fun aVertexWithNoAltitudeAdvancesTheDistanceAndOneThatNamesNoPlaceIsMeasuredAcross() {
        val series =
            buildElevationSeries(
                GeoJsonLineString(
                    listOf(
                        listOf(0.0, 0.0, 10.0),
                        listOf(1.0, 0.0),
                        listOf(Double.NaN, 0.0, 99.0),
                        listOf(2.0, 0.0, Double.NaN),
                        listOf(3.0, 0.0, 40.0),
                    ),
                ),
            )

        assertEquals(listOf(0.0, 3.0), series.points.map { it.distance / METERS_PER_DEGREE_MEAN_SPHERE })
        assertEquals(listOf(10.0, 40.0), series.points.map { it.elevation })
        assertEquals(3.0 * METERS_PER_DEGREE_MEAN_SPHERE, series.totalDistance)
    }

    @Test
    fun aSeriesPastItsCeilingIsThinnedAndKeepsTheWholeLinesRange() {
        val series = buildElevationSeries(GeoJsonLineString(climb), maxPoints = 4)

        assertEquals(listOf(1_035.0, 1_118.0, 1_240.0, 1_252.0), series.points.map { it.elevation })
        assertEquals(1_480.0, series.maxElevation)
        assertEquals(7_117.508508284167, series.totalDistance, 1e-6)
        val overTheCeiling =
            buildElevationSeries(
                GeoJsonLineString((0..MAX_PROFILE_POINTS).map { listOf(it / 1_000.0, 45.0, it.toDouble()) }),
            )
        assertEquals(MAX_PROFILE_POINTS, overTheCeiling.points.size)
        assertEquals(
            listOf(0.0, MAX_PROFILE_POINTS.toDouble()),
            listOf(overTheCeiling.points.first().elevation, overTheCeiling.points.last().elevation),
        )
    }
}
