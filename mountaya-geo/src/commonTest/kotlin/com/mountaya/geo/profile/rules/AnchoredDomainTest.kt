package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class AnchoredDomainTest {
    private val series =
        ElevationSeries(
            samples(0.0 to 1_035.0, 1_000.0 to 1_220.0, 2_000.0 to 1_480.0, 3_000.0 to 1_850.0),
            3_000.0,
            1_035.0,
            1_850.0,
        )

    @Test
    fun theRouteFramesTheItineraryWhateverTheWindow() {
        assertEquals(
            ElevationDomain(1_000.0, 1_850.0),
            anchoredDomain(series, ProfileAnchor.Route, ProfileRange(0.0, 500.0)),
        )
    }

    @Test
    fun seaLevelMovesTheFloorOfTheRoutesFrameToZeroItsCeilingKept() {
        assertEquals(
            ElevationDomain(0.0, 1_850.0),
            anchoredDomain(series, ProfileAnchor.SeaLevel, ProfileRange(0.0, 500.0)),
        )

        val flat = ElevationSeries(emptyList(), 100.0, null, null)

        assertEquals(ElevationDomain(0.0, 50.0), anchoredDomain(flat, ProfileAnchor.SeaLevel))
    }

    @Test
    fun theWindowFramesTheStretchItHoldsAndTheRouteWhereNoneIsGiven() {
        assertEquals(
            ElevationDomain(1_030.0, 1_220.0),
            anchoredDomain(series, ProfileAnchor.Window, ProfileRange(0.0, 1_000.0)),
        )
        assertEquals(ElevationDomain(1_000.0, 1_850.0), anchoredDomain(series, ProfileAnchor.Window))
    }
}
