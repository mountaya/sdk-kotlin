package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CommittedPaceTest {
    @Test
    fun thePaceIsTheTotalDistanceOverTheTotalDuration() {
        assertEquals(
            7.5,
            committedPace(
                listOf(
                    restSegment(distance = 10_000.0, duration = 3_600.0),
                    restSegment(distance = 5_000.0, duration = 3_600.0),
                ),
            ),
        )
        // 10 km in 1.5 h, not the mean of 9 km/h and 2 km/h.
        assertEquals(
            6.666666666666667,
            committedPace(
                listOf(
                    restSegment(distance = 9_000.0, duration = 3_600.0),
                    restSegment(distance = 1_000.0, duration = 1_800.0),
                ),
            ),
        )
    }

    @Test
    fun aSegmentWithNoTimingOrNoUsableFigureIsLeftOut() {
        val segments =
            listOf(
                restSegment(distance = 10_000.0, duration = 3_600.0),
                restSegment(distance = 5_000.0),
                restSegment(duration = 1_800.0),
                restSegment(distance = 2_000.0, duration = 0.0),
                restSegment(distance = -5_000.0, duration = 3_600.0),
                restSegment(distance = Double.NaN, duration = 1_800.0),
                restSegment(distance = 3_000.0, duration = Double.POSITIVE_INFINITY),
            )

        assertEquals(10.0, committedPace(segments))
        assertEquals(0.0, committedPace(listOf(restSegment(distance = 0.0, duration = 60.0))))
    }

    @Test
    fun noSegmentLeftIsNoPace() {
        assertNull(committedPace(emptyList()))
        assertNull(committedPace(listOf(restSegment(distance = 1_000.0))))
    }
}
