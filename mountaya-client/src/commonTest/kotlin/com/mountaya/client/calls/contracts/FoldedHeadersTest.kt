package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FoldedHeadersTest {
    @Test
    fun aHeaderIsReadInAnyLetterCaseTwoSpellingsJoinedInTheOrderOfTheirNames() {
        val headers =
            FoldedHeaders(mapOf("x-request-id" to "b", "ETag" to "\"d1\"", "X-Request-Id" to "a", "Vary" to "Origin"))

        assertEquals("a, b", headers["X-REQUEST-ID"])
        assertEquals("\"d1\"", headers["etag"])
        assertNull(headers["Location"])
        assertEquals(mapOf("x-request-id" to "a, b", "etag" to "\"d1\"", "vary" to "Origin"), headers.byName)
        assertEquals(listOf("x-request-id", "etag", "vary"), headers.byName.keys.toList())
    }
}
