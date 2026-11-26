package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals

class EncodePathSegmentTest {
    @Test
    fun theUnreservedCharactersAreKept() {
        assertEquals("7a4e1c98-3b52_4f60.9d81~x", encodePathSegment("7a4e1c98-3b52_4f60.9d81~x"))
    }

    @Test
    fun aSeparatorIsEscapedSoAValueIsOneSegment() {
        assertEquals("a%2Fb", encodePathSegment("a/b"))
        assertEquals("%3F%23%25%20", encodePathSegment("?#% "))
    }

    @Test
    fun aDotSegmentArrivesAsTheTextItIs() {
        assertEquals("..", encodePathSegment(".."))
    }

    @Test
    fun anythingElseIsItsUtf8BytesInUppercaseHexadecimal() {
        assertEquals("%C3%A9cole", encodePathSegment("\u00e9cole"))
        assertEquals("%F0%9F%8F%94", encodePathSegment("\uD83C\uDFD4"))
        assertEquals("", encodePathSegment(""))
    }
}
