package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class AsciiLowercaseTest {
    @Test
    fun onlyTheAsciiCapitalsAreFolded() {
        assertEquals("#3b82f6", asciiLowercase("#3B82F6"))
        assertEquals("currentcolor", asciiLowercase("CurrentColor"))
        assertEquals("İstanbul é", asciiLowercase("İSTANBUL é"))
        assertEquals("ß", asciiLowercase("ß"))
    }

    @Test
    fun textWithNoCapitalIsAnsweredAsItIs() {
        val text = "already small"
        assertSame(text, asciiLowercase(text))
    }
}
