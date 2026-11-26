package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class DownsampleElevationTest {
    private fun samples(count: Int) =
        (0 until count).map { ElevationPoint(it.toDouble(), it.toDouble(), listOf(0.0, 0.0), it) }

    private fun kept(
        count: Int,
        maxPoints: Int,
    ) = downsampleElevation(samples(count), maxPoints).map { it.elevation.toInt() }

    @Test
    fun eachSlotKeepsTheNearestSampleAndBothEndsAreKept() {
        assertEquals(listOf(0, 2, 4, 6), kept(7, 4))
        assertEquals(listOf(0, 1, 2, 4, 5, 6), kept(7, 6))
    }

    @Test
    fun aSlotHalfwayBetweenTwoSamplesKeepsTheLater() {
        assertEquals(listOf(0, 1, 3, 4, 5), kept(6, 5))
        // 249 × 503 / 498 is 251.5 exactly, which a floating-point stride reads as 251.49999999999997.
        assertEquals(252, kept(504, 499)[249])
    }

    @Test
    fun theKeptSamplesAreRenumbered() {
        assertEquals(listOf(0, 1, 2, 3), downsampleElevation(samples(7), 4).map { it.index })
    }

    @Test
    fun aCeilingUnderTwoOrOneTheSeriesIsWithinKeepsEverySample() {
        assertEquals((0 until 7).toList(), kept(7, 1))
        assertEquals((0 until 7).toList(), kept(7, -3))
        assertEquals((0 until 7).toList(), kept(7, 7))
        assertEquals(emptyList(), kept(0, 4))
    }
}
