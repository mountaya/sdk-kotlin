package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class WrapLongitudePastRangeTest {
    /** The wrap's arithmetic would cost these their last digits, and 180 would become -180. */
    @Test
    fun aLongitudeWithinTheClosedRangeKeepsTheDigitsItIsWrittenWith() {
        assertEquals(6.8652, wrapLongitudePastRange(6.8652))
        assertEquals(180.0, wrapLongitudePastRange(180.0))
        assertEquals(-180.0, wrapLongitudePastRange(-180.0))
        assertEquals(-0.0, wrapLongitudePastRange(-0.0))
    }

    @Test
    fun aLongitudePastTheRangeIsWrapped() {
        assertEquals(-170.0, wrapLongitudePastRange(190.0))
        assertEquals(179.5, wrapLongitudePastRange(-180.5))
        assertEquals(-180.0, wrapLongitudePastRange(540.0))
    }

    @Test
    fun aLongitudeThatIsNotFiniteWrapsToNaN() {
        assertEquals(Double.NaN, wrapLongitudePastRange(Double.POSITIVE_INFINITY))
        assertEquals(Double.NaN, wrapLongitudePastRange(Double.NaN))
    }
}
