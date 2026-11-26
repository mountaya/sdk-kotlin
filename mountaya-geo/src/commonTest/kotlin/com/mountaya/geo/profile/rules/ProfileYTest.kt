package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileYTest {
    private val domain = ElevationDomain(1_000.0, 2_000.0)

    @Test
    fun theCeilingStandsUnderTheTopInsetAndTheFloorOnTheBaselineUpsideDown() {
        assertEquals(6.0, profileY(2_000.0, domain))
        assertEquals(96.0, profileY(1_000.0, domain))
        assertEquals(51.0, profileY(1_500.0, domain))
        assertEquals(28.5, profileY(1_750.0, domain))
    }

    @Test
    fun anElevationOutsideTheDomainIsHeldToTheFrame() {
        assertEquals(6.0, profileY(2_500.0, domain))
        assertEquals(96.0, profileY(500.0, domain))
    }

    @Test
    fun aDomainWithNoSpanDrawsEveryElevationAtTheMiddle() {
        val flat = ElevationDomain(1_200.0, 1_200.0)

        assertEquals(51.0, profileY(1_200.0, flat))
        assertEquals(51.0, profileY(1_350.0, flat))
        assertEquals(51.0, profileY(1_200.0, ElevationDomain(1_300.0, 1_200.0)))
    }
}
