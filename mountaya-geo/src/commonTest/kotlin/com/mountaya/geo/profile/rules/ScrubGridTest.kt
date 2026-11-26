package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScrubGridTest {
    @Test
    fun aScrubReads240Columns() {
        assertEquals(240, PROFILE_SCRUB_COLUMNS)
        assertEquals(240, scrubGrid(samples(0.0 to 1_000.0), ProfileRange(0.0, 10.0)).size)
    }

    @Test
    fun twoSamplesSplitTheColumnsAtTheirMidpoint() {
        val grid = scrubGrid(samples(0.0 to 1_000.0, 1_000.0 to 1_100.0), ProfileRange(0.0, 1_000.0))

        assertTrue(grid.subList(0, 120).all { it == 0 })
        assertTrue(grid.subList(120, grid.size).all { it == 1 })
    }

    @Test
    fun aWindowReadsItsOwnColumnsAndPastTheSamplesTheNearestEnd() {
        val points =
            samplesEvery100Meters(
                1_000.0,
                1_010.0,
                1_020.0,
                1_030.0,
                1_040.0,
                1_050.0,
                1_060.0,
                1_070.0,
                1_080.0,
                1_090.0,
                1_100.0,
            )
        val grid = scrubGrid(points, ProfileRange(300.0, 540.0))

        assertEquals(listOf(3, 3, 4, 4, 5), listOf(grid[0], grid[49], grid[50], grid[149], grid[150]))

        val past = scrubGrid(samples(0.0 to 1_000.0, 500.0 to 1_100.0), ProfileRange(600.0, 1_000.0))

        assertTrue(past.all { it == 1 })
    }

    @Test
    fun withNoSampleAColumnResolvesToNone() {
        assertTrue(scrubGrid(emptyList(), ProfileRange(0.0, 1_000.0)).all { it == null })
    }

    @Test
    fun aColumnStandsForTheDistanceAtItsCentre() {
        val window = ProfileRange(0.0, 1_000.0)

        assertEquals(2.0833333333333335, scrubDistance(0, window), 1e-12)
        assertEquals(997.9166666666666, scrubDistance(239, window), 1e-12)
        assertEquals(0.5, scrubDistance(0, ProfileRange(0.0, 240.0)))
    }
}
