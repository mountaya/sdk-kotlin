package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileAreaPathTest {
    @Test
    fun theAreaClosesTheLineAgainstTheBottomOfTheSquare() {
        val ridge =
            ElevationSeries(
                samples(0.0 to 2_400.0, 500.0 to 2_500.0, 1_000.0 to 2_600.0),
                1_000.0,
                2_400.0,
                2_600.0,
            )

        assertEquals(
            listOf(
                PlotCommand.Move(PlotPoint(0.0, 100.0)),
                PlotCommand.Line(PlotPoint(0.0, 96.0)),
                PlotCommand.Line(PlotPoint(50.0, 51.0)),
                PlotCommand.Line(PlotPoint(100.0, 6.0)),
                PlotCommand.Line(PlotPoint(100.0, 100.0)),
                PlotCommand.Close,
            ),
            profileAreaPath(ridge, ElevationDomain(2_400.0, 2_600.0)),
        )
    }

    @Test
    fun oneSampleDrawsADegenerateAreaAndNoSampleNone() {
        val one = ElevationSeries(samples(0.0 to 1_250.0), 1_000.0, 1_250.0, 1_250.0)

        assertEquals(
            listOf(
                PlotCommand.Move(PlotPoint(0.0, 100.0)),
                PlotCommand.Line(PlotPoint(0.0, 51.0)),
                PlotCommand.Line(PlotPoint(0.0, 100.0)),
                PlotCommand.Close,
            ),
            profileAreaPath(one, ElevationDomain(1_000.0, 1_500.0)),
        )

        val empty = ElevationSeries(emptyList(), 1_000.0, null, null)

        assertEquals(emptyList(), profileAreaPath(empty, emptyElevationDomain))
    }
}
