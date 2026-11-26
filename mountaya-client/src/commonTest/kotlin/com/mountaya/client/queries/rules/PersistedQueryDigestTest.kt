package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PersistedQueryDigestTest {
    @Test
    fun theDigestIsSha256OverTheDocumentInLowercaseHexadecimal() {
        assertEquals(
            "9974f6119be398dbe7263214788ac8741299b9dd637bc45e7aa5967a44a4830d",
            persistedQueryDigest("query Directions { directions { distance } }"),
        )
    }

    @Test
    fun aCharacterOutsideAsciiIsDigestedAsItsUtf8Bytes() {
        // The ellipsis is three bytes in UTF-8 and two in UTF-16, so only the UTF-8 bytes give this digest.
        assertEquals(
            "4fa90df59d65ba47a9d93ecf3c703c9ffce8eb928c979bdc1f75313d8f0d2d4d",
            persistedQueryDigest("query Directions { … }"),
        )
    }

    @Test
    fun whitespaceIsPartOfTheDocument() {
        assertNotEquals(
            persistedQueryDigest("query Directions { directions { distance } }"),
            persistedQueryDigest("query Directions {  directions { distance } }"),
        )
    }

    @Test
    fun theDigestIsSixtyFourLowercaseHexadecimalDigits() {
        val digest = persistedQueryDigest("")

        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", digest)
        assertEquals(64, digest.length)
        assertEquals(digest.lowercase(), digest)
    }
}
