package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsCombiningMarkTest {
    @Test
    fun aMarkOfEveryKindIsACombiningMark() {
        assertTrue(isCombiningMark(0x0301), "combining acute")
        assertTrue(isCombiningMark(0x093F), "Devanagari vowel sign i, a spacing mark")
        assertTrue(isCombiningMark(0x064E), "Arabic fatha")
        assertTrue(isCombiningMark(0x20DD), "combining enclosing circle")
        assertTrue(isCombiningMark(0x1D165), "musical symbol combining stem, beyond the basic plane")
    }

    @Test
    fun aLetterADigitAndAJoinerAreNoCombiningMark() {
        assertFalse(isCombiningMark('e'.code))
        assertFalse(isCombiningMark(0x0915), "Devanagari ka")
        assertFalse(isCombiningMark(0x200D), "zero width joiner")
        assertFalse(isCombiningMark(0x1F600), "an emoji")
        assertFalse(isCombiningMark(-1))
    }
}
