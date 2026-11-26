package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsGroupedAtTest {
    @Test
    fun aNumberIsGroupedWhereEnoughDigitsStandInFrontOfItsFirstSeparator() {
        assertTrue(isGroupedAt(1_000.0, 1))
        assertFalse(isGroupedAt(999.0, 1))
        assertFalse(isGroupedAt(9_999.0, 2))
        assertTrue(isGroupedAt(10_000.0, 2))
        assertFalse(isGroupedAt(99_999.0, 3))
        assertTrue(isGroupedAt(100_000.0, 3))
    }

    @Test
    fun aNegativeNumberIsGroupedByItsMagnitudeAndAFractionCountsNoDigit() {
        assertTrue(isGroupedAt(-12_345.0, 2))
        assertFalse(isGroupedAt(-1_234.0, 2))
        assertFalse(isGroupedAt(9_999.9, 2))
    }
}
