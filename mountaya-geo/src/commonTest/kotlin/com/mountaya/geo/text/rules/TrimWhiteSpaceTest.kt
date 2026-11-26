package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TrimWhiteSpaceTest {
    @Test
    fun theWhiteSpaceAtEachEndIsRemovedAndTheRestKept() {
        assertEquals("7:45", trimWhiteSpace("  7:45 "))
        assertEquals("a b", trimWhiteSpace("\t\n a b\r\n"))
        assertEquals("x", trimWhiteSpace("﻿ x　 "))
        assertEquals("", trimWhiteSpace("   "))
        assertEquals("plain", trimWhiteSpace("plain"))
    }

    @Test
    fun theSetIsWrittenOutCharacterByCharacter() {
        for (char in listOf('\u000B', '\u000C', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ')) {
            assertTrue(isWhiteSpace(char), "U+${char.code.toString(16)}")
        }
        // The next line control and the zero-width space are no white space here.
        for (char in listOf('\u0085', '​', '᠎', 'a', '\u0000')) {
            assertFalse(isWhiteSpace(char), "U+${char.code.toString(16)}")
        }
        assertEquals("\u0085x\u0085", trimWhiteSpace("\u0085x\u0085"))
        assertEquals("x", trimWhiteSpace("x\r\n"))
    }
}
