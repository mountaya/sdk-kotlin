package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertLengthTest {
    /** Through the exact defining values, a mile is 5,280 feet exactly, never 5,280.0002. */
    @Test
    fun aMileIsExactlyFiveThousandTwoHundredAndEightyFeet() {
        assertEquals(5_280.0, convertLength(1.0, LengthUnit.Mile, LengthUnit.Foot))
        assertEquals(1_609.344, convertLength(1.0, LengthUnit.Mile, LengthUnit.Meter))
        assertEquals(0.3048, convertLength(1.0, LengthUnit.Foot, LengthUnit.Meter))
        assertEquals(2.54, convertLength(1.0, LengthUnit.Inch, LengthUnit.Centimeter))
        assertEquals(12.0, convertLength(1.0, LengthUnit.Foot, LengthUnit.Inch))
    }

    @Test
    fun aLengthInItsOwnUnitIsReturnedAsItIs() {
        for (unit in LengthUnit.entries) assertEquals(0.1, convertLength(0.1, unit, unit))
    }

    @Test
    fun aKilometerAndAMileConvertBothWays() {
        assertEquals(1.0, convertLength(1.609344, LengthUnit.Kilometer, LengthUnit.Mile), 1e-15)
        assertEquals(1.609344, convertLength(1.0, LengthUnit.Mile, LengthUnit.Kilometer))
    }

    @Test
    fun nothingIsClampedAndNoValueIsRefused() {
        assertEquals(-3.048, convertLength(-10.0, LengthUnit.Foot, LengthUnit.Meter))
        assertEquals(
            Double.NEGATIVE_INFINITY,
            convertLength(Double.NEGATIVE_INFINITY, LengthUnit.Mile, LengthUnit.Foot),
        )
        assertEquals(Double.NaN, convertLength(Double.NaN, LengthUnit.Meter, LengthUnit.Inch))
    }
}
