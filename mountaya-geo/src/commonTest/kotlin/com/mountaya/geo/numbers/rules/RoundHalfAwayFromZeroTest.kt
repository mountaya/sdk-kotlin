package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoundHalfAwayFromZeroTest {
    /**
     * 0.85 is held as 0.84999999999999997..., and 2.675 as 2.67499999999999982...: the decimal a
     * reader writes is halfway, so each rounds up, where the binary value would round down.
     */
    @Test
    fun theDecimalValueIsRoundedNotTheBinaryOne() {
        assertEquals(0.9, roundHalfAwayFromZero(0.85, 1))
        assertEquals(2.68, roundHalfAwayFromZero(2.675, 2))
        assertEquals(1.01, roundHalfAwayFromZero(1.005, 2))
        assertEquals(24.3, roundHalfAwayFromZero(24.3, 1))
        assertEquals(0.5, roundHalfAwayFromZero(0.528, 1))
    }

    @Test
    fun aHalfRoundsAwayFromZeroOnBothSides() {
        assertEquals(3.0, roundHalfAwayFromZero(2.5, 0))
        assertEquals(-3.0, roundHalfAwayFromZero(-2.5, 0))
        assertEquals(1.0, roundHalfAwayFromZero(0.5, 0))
        assertEquals(-0.9, roundHalfAwayFromZero(-0.85, 1))
        assertEquals(2.0, roundHalfAwayFromZero(2.4999999999999996, 0))
    }

    @Test
    fun aValueThatRoundsToZeroIsAPositiveZero() {
        for (value in listOf(-0.3, -0.04, -0.0, 0.4)) {
            val digits = if (value == -0.04) 1 else 0
            val rounded = roundHalfAwayFromZero(value, digits)
            assertEquals(0.0, rounded)
            assertTrue(1.0 / rounded > 0.0, "$value rounds to $rounded")
        }
    }

    @Test
    fun aValueThatIsNotFiniteOrTooLargeToHoldAFractionIsLeftAsItIs() {
        assertTrue(roundHalfAwayFromZero(Double.NaN, 1).isNaN())
        assertEquals(Double.POSITIVE_INFINITY, roundHalfAwayFromZero(Double.POSITIVE_INFINITY, 0))
        assertEquals(1e300, roundHalfAwayFromZero(1e300, 2))
        assertEquals(12.0, roundHalfAwayFromZero(12.0, -3))
        assertEquals(12.345, roundHalfAwayFromZero(12.345, 99))
    }

    @Test
    fun theExactPowersOfTenAreExact() {
        assertEquals(1.0, powerOfTen(0))
        assertEquals(1000.0, powerOfTen(3))
        assertEquals(1e22, powerOfTen(22))
        assertEquals(1e23, powerOfTen(23), 1e8)
        assertEquals(1.0, powerOfTen(-2))
    }
}
