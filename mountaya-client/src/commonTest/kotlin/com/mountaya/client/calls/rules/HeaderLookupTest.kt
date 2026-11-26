package com.mountaya.client

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class HeaderLookupTest {
    @Test
    fun aHeaderIsReadInAnyLetterCaseTwoSpellingsJoinedInTheOrderOfTheirNames() {
        val headers = mapOf("x-request-id" to "b", "X-Request-Id" to "a", "ETag" to "\"d1\"")

        assertEquals("a, b", HeaderLookup.value("X-REQUEST-ID", headers))
        assertEquals("\"d1\"", HeaderLookup.value("etag", headers))
        assertNull(HeaderLookup.value("Location", headers))
        assertTrue(HeaderLookup.contains("etag", headers))
        assertFalse(HeaderLookup.contains("If-Match", headers))
        // Only A to Z fold: a dotless capital is no letter of a header's name.
        assertEquals("i", HeaderLookup.asciiLowercase("I"))
        assertEquals("İ", HeaderLookup.asciiLowercase("İ"))
    }

    @Test
    fun aNameWithNoCapitalIsItsOwnLowercase() {
        val lowered = "x-request-id"

        assertSame(lowered, HeaderLookup.asciiLowercase(lowered))
        assertEquals("x-request-id", HeaderLookup.asciiLowercase("X-Request-ID"))
        assertEquals("ı-İ-é-x", HeaderLookup.asciiLowercase("ı-İ-é-X"))
    }

    /**
     * A set folded once reads every header as a lookup of the set reads it, and holds the names in the order the set
     * first spells each, as the response's headers were first folded, a name at a time, a lookup each.
     */
    @Test
    fun aSetFoldedOnceReadsEveryHeaderAsALookupOfTheSetReadsIt() {
        val random = Random(20261123)
        val spellings =
            listOf(
                listOf("X-Request-Id", "x-request-id", "X-REQUEST-ID"),
                listOf("ETag", "etag"),
                listOf("Retry-After"),
                listOf("RateLimit-Policy", "ratelimit-policy"),
                listOf("RateLimit", "Ratelimit"),
                listOf("Content-Type"),
                listOf("Location", "LOCATION"),
                listOf("İ-Header", "i̇-header", "I-Header"),
            )
        var joined = 0
        repeat(500) { case ->
            val headers = LinkedHashMap<String, String>()
            repeat(random.nextInt(0, 12)) {
                val names = spellings[random.nextInt(spellings.size)]
                headers[names[random.nextInt(names.size)]] = "v${random.nextInt(100)}"
            }
            val folded = HeaderLookup.fold(headers)
            val readOneByOne =
                headers.keys
                    .map(HeaderLookup::asciiLowercase)
                    .distinct()
                    .associateWith { name -> HeaderLookup.value(name, headers).orEmpty() }

            assertEquals(readOneByOne.toList(), folded.toList(), "case $case: $headers")
            for (name in spellings.flatten() + "Absent") {
                assertEquals(HeaderLookup.value(name, headers), folded[HeaderLookup.asciiLowercase(name)], name)
            }
            if (folded.values.any { ", " in it }) joined += 1
        }
        assertTrue(joined > 100, "the control: $joined sets spelled a name twice")
    }
}
