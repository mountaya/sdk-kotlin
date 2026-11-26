package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class CurveStopsTest {
    @Test
    fun oneStretchIsPaintedBetweenTwoHardEdgesUncoveredGroundStatedAsNoColour() {
        assertEquals(
            listOf(
                CurveStop(0.0, null),
                CurveStop(0.2, null),
                CurveStop(0.2, "#f00"),
                CurveStop(0.4, "#f00"),
                CurveStop(0.4, null),
                CurveStop(1.0, null),
            ),
            curveStops(listOf(CurveColor(200.0, 400.0, "#f00")), 1_000.0),
        )
    }

    @Test
    fun touchingStretchesOfOneColourStateTheirBoundaryOnce() {
        assertEquals(
            listOf(CurveStop(0.0, "#f00"), CurveStop(0.5, "#f00"), CurveStop(1.0, "#f00")),
            curveStops(listOf(CurveColor(0.0, 500.0, "#f00"), CurveColor(500.0, 1_000.0, "#f00")), 1_000.0),
        )
    }

    @Test
    fun aWindowPlacesTheStretchesItHoldsAndCutsOneCrossingItsEnd() {
        val window = ProfileRange(200.0, 700.0)

        assertEquals(
            listOf(CurveStop(0.0, "#f00"), CurveStop(0.5, "#f00"), CurveStop(0.5, null), CurveStop(1.0, null)),
            curveStops(listOf(CurveColor(0.0, 450.0, "#f00")), 1_000.0, window),
        )
        assertEquals(emptyList(), curveStops(listOf(CurveColor(800.0, 900.0, "#f00")), 1_000.0, window))
    }

    @Test
    fun noStretchOfAnyLengthIsNoGradient() {
        assertEquals(emptyList(), curveStops(emptyList(), 1_000.0))
        assertEquals(emptyList(), curveStops(listOf(CurveColor(300.0, 300.0, "#f00")), 1_000.0))
        assertEquals(emptyList(), curveStops(listOf(CurveColor(0.0, 300.0, "#f00")), 0.0))
    }
}
