package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertSlopeTest {
    /** Forty-five degrees is a hundred percent, not forty-five: a grade is the tangent in hundredths. */
    @Test
    fun aSlopeConvertsThroughTheTangent() {
        assertEquals(100.0, convertSlope(45.0, SlopeUnit.Degrees, SlopeUnit.Percentage), 1e-12)
        assertEquals(45.0, convertSlope(100.0, SlopeUnit.Percentage, SlopeUnit.Degrees))
        assertEquals(5.710593137499643, convertSlope(0.1, SlopeUnit.Ratio, SlopeUnit.Degrees), 1e-12)
        assertEquals(0.1, convertSlope(5.710593137499643, SlopeUnit.Degrees, SlopeUnit.Ratio), 1e-15)
        assertEquals(8.0, convertSlope(0.08, SlopeUnit.Ratio, SlopeUnit.Percentage))
        assertEquals(0.08, convertSlope(8.0, SlopeUnit.Percentage, SlopeUnit.Ratio))
    }

    @Test
    fun aDescentKeepsItsSign() {
        assertEquals(-5.710593137499643, convertSlope(-0.1, SlopeUnit.Ratio, SlopeUnit.Degrees), 1e-12)
        assertEquals(-8.0, convertSlope(-0.08, SlopeUnit.Ratio, SlopeUnit.Percentage))
    }

    /** A slope in its own unit is never taken through the tangent and back, which would cost it digits. */
    @Test
    fun aSlopeInItsOwnUnitIsReturnedAsItIs() {
        for (unit in SlopeUnit.entries) assertEquals(33.3, convertSlope(33.3, unit, unit))
    }
}
