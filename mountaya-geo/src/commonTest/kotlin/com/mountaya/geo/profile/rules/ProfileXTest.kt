package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileXTest {
    @Test
    fun theSquareItsInsetsAndItsBaselineAreTheContracts() {
        assertEquals(100.0, PROFILE_VIEW_BOX)
        assertEquals(6.0, PROFILE_TOP_INSET)
        assertEquals(4.0, PROFILE_BOTTOM_INSET)
        assertEquals(96.0, PROFILE_BASELINE)
    }

    @Test
    fun aDistanceRunsFromTheWindowsStartAt0ToItsEndAt100() {
        assertEquals(50.0, profileX(500.0, 1_000.0))
        assertEquals(33.333, profileX(1_000.0, 3_000.0))
        assertEquals(50.0, profileX(450.0, 1_000.0, ProfileRange(200.0, 700.0)))
        assertEquals(25.0, profileX(325.0, 1_000.0, ProfileRange(700.0, 200.0)))
    }

    @Test
    fun aDistanceOutsideTheWindowIsHeldToTheFrame() {
        assertEquals(0.0, profileX(100.0, 1_000.0, ProfileRange(200.0, 700.0)))
        assertEquals(100.0, profileX(900.0, 1_000.0, ProfileRange(200.0, 700.0)))
        assertEquals(0.0, profileX(-5.0, 1_000.0))
    }

    @Test
    fun anItineraryOrAWindowOfNoLengthDrawsAtTheOrigin() {
        assertEquals(0.0, profileX(0.0, 0.0))
        assertEquals(0.0, profileX(300.0, 1_000.0, ProfileRange(300.0, 300.0)))
        assertEquals(0.0, profileX(300.0, Double.NaN))
    }

    @Test
    fun anUnclampedProjectionLeavesTheFrameRoundedToAThousandthAHalfUp() {
        val span = ProfileRange(250.0, 450.0)

        assertEquals(-25.0, projectX(200.0, span))
        assertEquals(125.0, projectX(500.0, span))
        assertEquals(0.0, projectX(1.0, ProfileRange(0.0, 0.0)))
        assertEquals(-25.0, plotUnit(-25.0005))
        assertEquals(25.001, plotUnit(25.0005))
    }
}
