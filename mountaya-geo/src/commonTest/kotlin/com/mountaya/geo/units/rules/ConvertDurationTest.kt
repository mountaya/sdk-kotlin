package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertDurationTest {
    @Test
    fun eachUnitHoldsAWholeNumberOfMilliseconds() {
        assertEquals(86_400_000.0, convertDuration(1.0, DurationUnit.Day, DurationUnit.Millisecond))
        assertEquals(24.0, convertDuration(1.0, DurationUnit.Day, DurationUnit.Hour))
        assertEquals(3_600.0, convertDuration(1.0, DurationUnit.Hour, DurationUnit.Second))
        assertEquals(60.0, convertDuration(1.0, DurationUnit.Minute, DurationUnit.Second))
        assertEquals(1.5, convertDuration(90_000.0, DurationUnit.Millisecond, DurationUnit.Minute))
    }

    @Test
    fun aDurationInItsOwnUnitIsReturnedAsItIs() {
        for (unit in DurationUnit.entries) assertEquals(0.1, convertDuration(0.1, unit, unit))
    }
}
