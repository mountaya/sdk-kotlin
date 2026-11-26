package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileDomainTest {
    private val series =
        ElevationSeries(
            samples(0.0 to 1_000.0, 1_000.0 to 1_100.0, 2_000.0 to 1_500.0, 3_000.0 to 1_900.0, 4_000.0 to 1_300.0),
            4_000.0,
            1_000.0,
            1_900.0,
        )

    @Test
    fun withNoWindowTheStatedExtentIsFramed() {
        assertEquals(ElevationDomain(1_000.0, 1_900.0), profileDomain(series))
    }

    @Test
    fun aWindowFramesTheStretchItHoldsEachEndReadOnTheCurveInEitherOrder() {
        assertEquals(ElevationDomain(1_000.0, 1_300.0), profileDomain(series, ProfileRange(0.0, 1_500.0)))
        assertEquals(ElevationDomain(1_000.0, 1_300.0), profileDomain(series, ProfileRange(1_500.0, 0.0)))
        assertEquals(ElevationDomain(1_580.0, 1_740.0), profileDomain(series, ProfileRange(2_200.0, 2_600.0)))
    }

    @Test
    fun aWindowHoldingTheWholeCurveOrNoneOfItFramesTheStatedExtent() {
        assertEquals(ElevationDomain(1_000.0, 1_900.0), profileDomain(series, ProfileRange(0.0, 4_000.0)))
        assertEquals(ElevationDomain(1_000.0, 1_900.0), profileDomain(series, ProfileRange(5_000.0, 6_000.0)))
    }

    @Test
    fun aSeriesWithNoElevationIsFramedFrom0To50() {
        val flat = ElevationSeries(emptyList(), 5_000.0, null, null)

        assertEquals(emptyElevationDomain, profileDomain(flat))
        assertEquals(emptyElevationDomain, profileDomain(flat, ProfileRange(0.0, 100.0)))
    }
}
