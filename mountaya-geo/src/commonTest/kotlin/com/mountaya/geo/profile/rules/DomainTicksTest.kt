package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class DomainTicksTest {
    @Test
    fun aScaleIsLabelledAtItsFloorItsMiddleAndItsCeiling() {
        assertEquals(listOf(1_000.0, 1_425.0, 1_850.0), domainTicks(ElevationDomain(1_000.0, 1_850.0)))
        assertEquals(listOf(0.0, 25.0, 50.0), domainTicks(ElevationDomain(0.0, 50.0)))
    }
}
