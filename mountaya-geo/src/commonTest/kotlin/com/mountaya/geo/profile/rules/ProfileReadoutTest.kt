package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ProfileReadoutTest {
    private val series =
        ElevationSeries(
            samplesAt(0.0 to 1_000.0, 200.0 to 1_020.0, 400.0 to 1_040.0, 600.0 to 1_030.0),
            600.0,
            1_000.0,
            1_040.0,
        )

    /** A series' samples at the distances and altitudes given, each at the origin and numbered by its place. */
    private fun samplesAt(vararg pairs: Pair<Double, Double>): List<ElevationPoint> =
        pairs.mapIndexed { index, (distance, altitude) ->
            ElevationPoint(distance, altitude, listOf(0.0, 0.0), index)
        }

    @Test
    fun theDefaultFiguresAreWhereTheCursorStandsThenWhatItTookToGetThere() {
        assertEquals(
            listOf(ReadoutStat.Distance, ReadoutStat.Elevation, ReadoutStat.Ascent, ReadoutStat.Descent),
            defaultReadoutStats,
        )
        val readout = profileReadout(series, series.points[3])

        assertEquals(listOf(600.0, 1_030.0, 40.0, 10.0), readout.cells.map { it.value })
        assertEquals(-2.862405226111748, assertNotNull(readout.grade, "the readout states a grade"), 1e-9)
        assertNull(readout.stretch)
    }

    @Test
    fun theFiguresInTheOrderAskedTheClimbReadOffTheOneScan() {
        val readout = profileReadout(series, series.points[2], listOf(ReadoutStat.Descent, ReadoutStat.Distance))

        assertEquals(
            listOf(ReadoutCell(ReadoutStat.Descent, 0.0), ReadoutCell(ReadoutStat.Distance, 400.0)),
            readout.cells,
        )
        val jitter =
            ElevationSeries(
                samplesAt(0.0 to 1_000.0, 50.0 to 1_003.0, 100.0 to 1_001.0, 150.0 to 1_006.0),
                150.0,
                1_000.0,
                1_006.0,
            )
        assertEquals(listOf(150.0, 1_006.0, 6.0, 0.0), profileReadout(jitter, jitter.points[3]).cells.map { it.value })
    }

    @Test
    fun theStretchItStandsInTheLaterAtASharedBoundaryAndNoneInAGap() {
        val stretches =
            listOf(
                ProfileClass(0.0, 400.0, "#2f9e44", "Path"),
                ProfileClass(400.0, 600.0, "#e8590c", "Trail"),
            )

        assertEquals("Trail", profileReadout(series, series.points[2], stretches = stretches).stretch?.label)
        assertEquals("Path", profileReadout(series, series.points[1], stretches = stretches).stretch?.label)
        val short = listOf(ProfileClass(0.0, 300.0, "#2f9e44", "Path"))
        assertNull(profileReadout(series, series.points[3], stretches = short).stretch)
    }

    @Test
    fun oneSampleHasNoGrade() {
        val one = ElevationSeries(samplesAt(0.0 to 1_000.0), 0.0, 1_000.0, 1_000.0)
        val readout = profileReadout(one, one.points[0])

        assertNull(readout.grade)
        assertEquals(listOf(0.0, 1_000.0, 0.0, 0.0), readout.cells.map { it.value })
    }
}
