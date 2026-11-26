package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteLengthMetersTest {
    @Test
    fun theLengthIsTheSumOfTheGreatCircleLegs() {
        assertEquals(
            2.0 * METERS_PER_DEGREE_MEAN_SPHERE,
            routeLengthMeters(listOf(listOf(0.0, 0.0), listOf(1.0, 0.0), listOf(2.0, 0.0))),
        )
        assertEquals(
            135_786.09062692837,
            routeLengthMeters(listOf(listOf(6.0, 45.0), listOf(Double.NaN, 45.0), listOf(7.0, 46.0))),
            1e-6,
        )
    }

    @Test
    fun aLineOfOneVertexOrNoneHasNoLength() {
        assertEquals(0.0, routeLengthMeters(listOf(listOf(6.0, 45.0))))
        assertEquals(0.0, routeLengthMeters(emptyList()))
        assertEquals(0.0, routeLengthMeters(listOf(listOf(6.0), listOf(Double.NaN, 45.0))))
    }
}
