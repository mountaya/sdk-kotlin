package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DomainRoundingStepTest {
    @Test
    fun twoStepsAreEqualWhereTheirNumbersAreAnUnboundedStepOnlyToAnother() {
        assertEquals(DomainRoundingStep(10.0, 200.0), DomainRoundingStep(10.0, 200.0))
        assertNotEquals(DomainRoundingStep(100.0, 1_000.0), DomainRoundingStep(100.0, null))
        assertEquals(DomainRoundingStep(100.0, null).hashCode(), DomainRoundingStep(100.0, null).hashCode())
    }
}
