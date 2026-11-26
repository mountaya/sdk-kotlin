package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfilePolylineTest {
    private val domain = ElevationDomain(1_000.0, 1_500.0)
    private val climb =
        ElevationSeries(
            samplesEvery100Meters(
                1_000.0,
                1_050.0,
                1_100.0,
                1_150.0,
                1_200.0,
                1_250.0,
                1_300.0,
                1_350.0,
                1_400.0,
                1_450.0,
                1_500.0,
            ),
            1_000.0,
            1_000.0,
            1_500.0,
        )

    @Test
    fun aRidgeOnTwoEqualLegsIsDrawnFloorMiddleAndTop() {
        val ridge =
            ElevationSeries(
                samples(0.0 to 2_400.0, 500.0 to 2_500.0, 1_000.0 to 2_600.0),
                1_000.0,
                2_400.0,
                2_600.0,
            )

        assertEquals(
            listOf(PlotPoint(0.0, 96.0), PlotPoint(50.0, 51.0), PlotPoint(100.0, 6.0)),
            profilePolyline(ridge, ElevationDomain(2_400.0, 2_600.0)),
        )
    }

    @Test
    fun aWindowDrawsOneSampleBeyondEachEndUnclamped() {
        assertEquals(
            listOf(-25.0, 25.0, 75.0, 125.0),
            profilePolyline(climb, domain, ProfileRange(250.0, 450.0)).map { it.x },
        )
        assertEquals(
            listOf(-50.0, 0.0, 50.0, 100.0, 150.0),
            profilePolyline(climb, domain, ProfileRange(300.0, 500.0)).map { it.x },
        )
        assertEquals(
            listOf(-50.0, 0.0, 50.0, 100.0),
            profilePolyline(climb, domain, ProfileRange(1_000.0, 800.0)).map { it.x },
        )
    }

    @Test
    fun twoLinesMeetingAtOneDistanceAreJoinedThere() {
        val joined =
            ElevationSeries(
                samples(0.0 to 1_000.0, 500.0 to 1_200.0, 500.0 to 1_250.0, 1_000.0 to 1_300.0),
                1_000.0,
                1_000.0,
                1_300.0,
            )

        assertEquals(
            listOf(PlotPoint(0.0, 96.0), PlotPoint(50.0, 60.0), PlotPoint(50.0, 51.0), PlotPoint(100.0, 42.0)),
            profilePolyline(joined, domain),
        )
    }

    @Test
    fun aWindowTheSamplesDoNotReachAndASeriesWithNoSampleDrawNothing() {
        val short =
            ElevationSeries(
                samples(0.0 to 1_000.0, 250.0 to 1_250.0, 500.0 to 1_500.0),
                1_000.0,
                1_000.0,
                1_500.0,
            )

        assertEquals(emptyList(), profilePolyline(short, domain, ProfileRange(600.0, 800.0)))

        val empty = ElevationSeries(emptyList(), 1_000.0, null, null)

        assertEquals(emptyList(), profilePolyline(empty, domain))
    }

    @Test
    fun aWindowBeforeEverySampleDrawsTheFirstTwo() {
        val late = samples(500.0 to 1_000.0, 600.0 to 1_100.0, 700.0 to 1_200.0)

        assertEquals(listOf(0, 1), drawnSamples(late, ProfileRange(0.0, 100.0)).map { it.index })
        assertEquals(3, drawnSamples(late, null).size)
    }
}
