package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class FourDecimalPlacesTest {
    @Test
    fun aValueIsWrittenWithFourPlacesInLatinDigitsAndAFullStop() {
        assertEquals("0.0000", fourDecimalPlaces(0.0))
        assertEquals("6.8652", fourDecimalPlaces(6.8652))
        assertEquals("162.5000", fourDecimalPlaces(162.5))
        assertEquals("12.0464", fourDecimalPlaces(12.0464))
    }

    /**
     * The binary value is what is rounded: 6.86525 is held as 6.865249999..., and 1.00005 as
     * 1.0000500000000001...
     */
    @Test
    fun theBinaryValueIsRoundedNotTheDecimalItWasWrittenAs() {
        assertEquals("6.8652", fourDecimalPlaces(6.86525))
        assertEquals("1.0001", fourDecimalPlaces(1.00005))
        assertEquals("45.0001", fourDecimalPlaces(45.00005))
        assertEquals("0.0001", fourDecimalPlaces(0.00005))
        assertEquals("0.0000", fourDecimalPlaces(0.00004999999999999999))
    }

    /** 0.03125 is exactly halfway in binary, and a half rounds away from zero. */
    @Test
    fun aValueExactlyHalfwayTakesTheLargerPlace() {
        assertEquals("0.0313", fourDecimalPlaces(0.03125))
        assertEquals("45.0313", fourDecimalPlaces(45.03125))
    }

    @Test
    fun aFractionThatRoundsToAWholeOneCarriesIntoTheWholeNumber() {
        assertEquals("1.0000", fourDecimalPlaces(0.99995))
        assertEquals("180.0000", fourDecimalPlaces(179.99995))
        assertEquals("0.5000", fourDecimalPlaces(0.4999999999999999))
    }

    @Test
    fun theSmallestValuesRoundToZero() {
        assertEquals("0.0000", fourDecimalPlaces(Double.MIN_VALUE))
        assertEquals("0.0000", fourDecimalPlaces(2.2250738585072014E-308))
    }

    /** A whole number past what a Long holds is written in full, never in an exponent. */
    @Test
    fun everyFiniteMagnitudeIsWrittenInFull() {
        assertEquals("4503599627370495.5000", fourDecimalPlaces(4503599627370495.5))
        assertEquals("9007199254740992.0000", fourDecimalPlaces(9007199254740992.0))
        assertEquals("10000000000000000000.0000", fourDecimalPlaces(1e19))
        assertEquals("9223372036854775808.0000", fourDecimalPlaces(9.223372036854775808E18))
        assertEquals("9223372036854777856.0000", fourDecimalPlaces(9.223372036854777856E18))
        assertEquals(
            "179769313486231570814527423731704356798070567525844996598917476803157260780028538760589558632766878171540458953514382464234321326889464182768467546703537516986049910576551282076245490090389328944075868508455133942304583236903222948165808559332123348274797826204144723168738177180919299881250404026184124858368.0000",
            fourDecimalPlaces(Double.MAX_VALUE),
        )
    }
}
