package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ScaleByRatioTest {
    /** A pair that reduces to a whole factor multiplies once, so the answer is the exact product. */
    @Test
    fun aFactorThatReducesToAWholeNumberMultipliesOnce() {
        assertEquals(5_280.0, scaleByRatio(1.0, 16_093_440L, 3_048L))
        assertEquals(42.0, scaleByRatio(42.0, 10_000L, 10_000L))
    }

    @Test
    fun aFactorThatReducesToAWholeFractionDividesOnce() {
        assertEquals(0.07, scaleByRatio(7.0, 1L, 100L))
        assertEquals(0.008, scaleByRatio(1.0, 8_000L, 1_000_000L))
    }

    @Test
    fun anyOtherPairMultipliesByItsReducedNumeratorThenDividesByItsReducedDenominator() {
        assertEquals(0.95367431640625, scaleByRatio(1.0, 8_000_000L, 8_388_608L))
        assertEquals(10.0, scaleByRatio(36.0, 10_000_000_000L, 36_000_000_000L))
    }

    @Test
    fun aValueThatIsNotFiniteStaysWhatItIs() {
        assertEquals(Double.POSITIVE_INFINITY, scaleByRatio(Double.POSITIVE_INFINITY, 3L, 7L))
        assertEquals(Double.NaN, scaleByRatio(Double.NaN, 3L, 7L))
    }

    @Test
    fun anInversionDividesTheReducedRatioByTheValueAndZeroAnswersAnInfinity() {
        assertEquals(4.5, invertByRatio(13.333333333333334, 60L, 1L), 1e-12)
        assertEquals(21.45792, invertByRatio(4.5, 57_936_384_000_000L, 600_000_000_000L), 1e-12)
        assertEquals(Double.POSITIVE_INFINITY, invertByRatio(0.0, 60L, 1L))
        assertEquals(Double.NEGATIVE_INFINITY, invertByRatio(-0.0, 60L, 1L))
    }
}
