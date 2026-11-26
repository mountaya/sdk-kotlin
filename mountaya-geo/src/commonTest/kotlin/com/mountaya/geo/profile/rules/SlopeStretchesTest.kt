package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class SlopeStretchesTest {
    private fun samplesEvery25Meters(vararg elevations: Double) =
        elevations.mapIndexed { index, elevation -> ElevationPoint(index * 25.0, elevation, listOf(0.0, 0.0), index) }

    @Test
    fun theBucketsAreTheContracts() {
        assertEquals(
            listOf("steepDescent", "descent", "flat", "ascent", "steepAscent"),
            slopeBuckets.map { it.key },
        )
        assertEquals(listOf(Double.NEGATIVE_INFINITY, -10.0, -3.0, 3.0, 10.0), slopeBuckets.map { it.from })
        assertEquals(
            listOf("#e8590c", "#f59f00", "#2f9e44", "#f59f00", "#e8590c"),
            slopeBuckets.map { it.color },
        )
    }

    @Test
    fun longStretchesReadTheirOwnSlopeAndNeighboursOfOneBucketMerge() {
        val stretches = slopeStretches(samples(0.0 to 1_000.0, 200.0 to 1_000.0, 400.0 to 1_040.0, 600.0 to 1_080.0))

        assertEquals(
            listOf(
                ProfileClass(0.0, 200.0, "#2f9e44", "flat"),
                ProfileClass(200.0, 600.0, "#e8590c", "steepAscent"),
            ),
            stretches,
        )
    }

    @Test
    fun aStretchAsLongAsTheWindowReadsItsOwnSlope() {
        assertEquals(
            listOf("flat", "steepAscent"),
            slopeStretches(samples(0.0 to 1_000.0, 100.0 to 1_000.0, 200.0 to 1_030.0)).map { it.label },
        )
    }

    @Test
    fun denseSamplesReadTheGradeOverTheWindow() {
        val points =
            samplesEvery25Meters(
                1_000.0,
                1_000.0,
                1_000.0,
                1_000.0,
                1_000.0,
                1_001.0,
                1_012.0,
                1_001.0,
                1_000.0,
                1_000.0,
                1_000.0,
                1_000.0,
                1_000.0,
            )

        assertEquals(
            listOf(
                "flat 0.0 75.0",
                "ascent 75.0 100.0",
                "flat 100.0 175.0",
                "descent 175.0 200.0",
                "flat 200.0 300.0",
            ),
            slopeStretches(points).map { "${it.label} ${it.from} ${it.to}" },
        )
    }

    @Test
    fun fewerThanTwoSamplesOrNoBucketHasNoStretchAGradeUnderEveryBucketTakesTheFirst() {
        assertEquals(emptyList(), slopeStretches(samples(0.0 to 1_000.0)))
        assertEquals(emptyList(), slopeStretches(emptyList()))
        assertEquals(emptyList(), slopeStretches(samples(0.0 to 1_000.0, 200.0 to 1_000.0), emptyList()))

        val high = listOf(SlopeBucket(50.0, "cliff", "#000"))

        assertEquals(
            listOf("cliff"),
            slopeStretches(samples(0.0 to 1_000.0, 200.0 to 1_000.0), high).map { it.label },
        )
    }
}
