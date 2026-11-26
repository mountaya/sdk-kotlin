package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ElevationDomainTest {
    @Test
    fun twoDomainsAreEqualWhereTheFloorsAndCeilingsAreTheSameNumbers() {
        assertEquals(ElevationDomain(1_000.0, 1_850.0), ElevationDomain(1_000.0, 1_850.0))
        assertEquals(ElevationDomain(1_000.0, 1_850.0).hashCode(), ElevationDomain(1_000.0, 1_850.0).hashCode())
        assertNotEquals(ElevationDomain(-0.0, 50.0), ElevationDomain(0.0, 50.0))
        assertEquals(ElevationDomain(Double.NaN, 50.0), ElevationDomain(Double.NaN, 50.0))
    }
}
