package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ExtentDomainTest {
    @Test
    fun theNarrowestSpanTheEmptyDomainAndTheRoundingStepsAreTheContracts() {
        assertEquals(50.0, MIN_DOMAIN_SPAN_METERS)
        assertEquals(ElevationDomain(0.0, 50.0), emptyElevationDomain)
        assertEquals(
            listOf(
                DomainRoundingStep(10.0, 200.0),
                DomainRoundingStep(50.0, 1_000.0),
                DomainRoundingStep(100.0, null),
            ),
            domainRoundingSteps,
        )
    }

    @Test
    fun anExtentIsRoundedOutwardByAStepItsSpanChooses() {
        assertEquals(ElevationDomain(1_000.0, 1_850.0), extentDomain(1_035.0, 1_850.0))
        assertEquals(ElevationDomain(1_000.0, 1_210.0), extentDomain(1_003.0, 1_203.0))
        assertEquals(ElevationDomain(1_000.0, 1_250.0), extentDomain(1_003.0, 1_204.0))
        assertEquals(ElevationDomain(1_000.0, 2_050.0), extentDomain(1_010.0, 2_010.0))
        assertEquals(ElevationDomain(1_000.0, 2_100.0), extentDomain(1_010.0, 2_011.0))
    }

    @Test
    fun aFlatExtentIsWidenedToFiftyMetersAboutItsMiddle() {
        assertEquals(ElevationDomain(70.0, 130.0), extentDomain(100.0, 104.0))
        assertEquals(ElevationDomain(70.0, 130.0), extentDomain(100.0, 100.0))
    }

    @Test
    fun anExtentWithABoundMissingIsTheEmptyDomain() {
        assertEquals(emptyElevationDomain, extentDomain(null, 100.0))
        assertEquals(emptyElevationDomain, extentDomain(100.0, null))
    }
}
