package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HeaderValueTest {
    @Test
    fun aValueIsBlankWhereItIsEmptyOrWhitespaceAlone() {
        for (value in listOf("", " ", "\t\n", " ", "  　")) assertTrue(HeaderValue.isBlank(value), value)
        for (value in listOf("a", " a ", "\u0000", "\u001c")) assertFalse(HeaderValue.isBlank(value), value)
    }

    /**
     * A header carries printable ASCII alone in every SDK, so Latin-1's U+00A0 to U+00FF, which OkHttp under Ktor
     * does not send either, is refused with every control character (`sdk/SPEC.md` sections 1 and 13).
     */
    @Test
    fun aHeaderCarriesPrintableAsciiAloneNoLatin1AndNoControlCharacter() {
        for (value in listOf("pk_test_abc", "fr-CH, fr;q=0.9", "\"d42\"", " ", " ~", "W/\"x\"")) {
            assertTrue(HeaderValue.isCarried(value), value)
        }
        val refused =
            listOf("café", "ÿ", " ", "a\u0000", "a\tb", "a\nb", "a\rb", "\u001f", "\u007f", "\u0080", "Ā", "😀")
        for (value in refused) assertFalse(HeaderValue.isCarried(value), value)
    }
}
