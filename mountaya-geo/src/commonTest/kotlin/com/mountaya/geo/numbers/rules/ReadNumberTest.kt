package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReadNumberTest {
    @Test
    fun aDecimalLiteralIsItsValueTheWhiteSpaceAtItsEndsLeftOut() {
        assertEquals(3_600.0, readNumber("3600"))
        assertEquals(1_234.56789, readNumber(" 1234.56789\n"))
        assertEquals(1_500.0, readNumber("+1.5e3"))
        assertEquals(-2.0, readNumber("-2"))
        assertEquals(0.5, readNumber(".5"))
        assertEquals(5.0, readNumber("5."))
        assertEquals(0.01, readNumber("1E-2"))
    }

    @Test
    fun theEmptyTextIsZeroAndTheInfinitiesAndPrefixedLiteralsReadAsEcmaScriptReadsThem() {
        assertEquals(0.0, readNumber(""))
        assertEquals(0.0, readNumber("  "))
        assertEquals(Double.POSITIVE_INFINITY, readNumber("Infinity"))
        assertEquals(Double.NEGATIVE_INFINITY, readNumber("-Infinity"))
        assertEquals(16.0, readNumber("0x10"))
        assertEquals(15.0, readNumber("0o17"))
        assertEquals(5.0, readNumber("0B101"))
    }

    @Test
    fun anythingElseIsNoNumber() {
        // The last is twelve in Arabic-Indic digits.
        val texts =
            listOf("abc", "1_000", "0x1p3", "nan", "inf", "-0x10", "1e", ".", "+", "0xG", "1 2", "\u0661\u0662")
        for (text in texts) {
            assertTrue(readNumber(text).isNaN(), text)
        }
    }
}
