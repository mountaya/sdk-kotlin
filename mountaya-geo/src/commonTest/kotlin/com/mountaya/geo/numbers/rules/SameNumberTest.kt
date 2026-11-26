package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SameNumberTest {
    @Test
    fun aNumberIsTheSameAsItselfAndNoOther() {
        assertTrue(sameNumber(6.8652, 6.8652))
        assertTrue(sameNumber(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY))
        assertFalse(sameNumber(6.8652, 6.8653))
        assertFalse(sameNumber(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY))
    }

    @Test
    fun notANumberIsTheSameAsNotANumberAndTheTwoZerosAreTwoNumbers() {
        assertTrue(sameNumber(Double.NaN, Double.NaN))
        // A NaN with another payload is still the one NaN.
        assertTrue(sameNumber(Double.NaN, Double.fromBits(0x7ff8000000000001L)))
        assertFalse(sameNumber(Double.NaN, 0.0))
        assertFalse(sameNumber(0.0, -0.0))
        assertTrue(sameNumber(-0.0, -0.0))
    }

    @Test
    fun twoListsAreTheSameNumbersInTheSameOrder() {
        assertTrue(sameNumbers(listOf(6.0, Double.NaN), listOf(6.0, Double.NaN)))
        assertTrue(sameNumbers(emptyList(), emptyList()))
        assertFalse(sameNumbers(listOf(6.0, 45.0), listOf(45.0, 6.0)))
        assertFalse(sameNumbers(listOf(6.0, 45.0), listOf(6.0, 45.0, 1000.0)))
        assertFalse(sameNumbers(listOf(0.0), listOf(-0.0)))
    }

    @Test
    fun anAbsentFigureIsTheSameOnlyAsAnotherAbsentOne() {
        assertTrue(sameNumberOrNull(null, null))
        assertTrue(sameNumberOrNull(Double.NaN, Double.NaN))
        assertFalse(sameNumberOrNull(null, 0.0))
        assertFalse(sameNumberOrNull(0.0, null))
        assertFalse(sameNumberOrNull(0.0, -0.0))
    }
}
