package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CurvePlacementTest {
    @Test
    fun aPlacementHoldsItsPinsDividersAndCutOffsAsGiven() {
        val col = ProfilePlace(ProfileWaypoint.AtDistance("col", 250.0), name = "Col")
        val pin = CurvePin("col#0", col, 250.0, 50.0, 51.0, 1_250.0)
        val divider = CurveDivider("col#0", col, 250.0, 50.0, 51.0)
        val placement = CurvePlacement(listOf(pin), listOf(divider), emptyList())

        assertEquals(listOf(pin), placement.pins)
        assertEquals(listOf(divider), placement.dividers)
        assertTrue(placement.cutoffs.isEmpty())
        assertEquals(CurvePlacement(listOf(pin), listOf(divider), emptyList()), placement)
    }
}
