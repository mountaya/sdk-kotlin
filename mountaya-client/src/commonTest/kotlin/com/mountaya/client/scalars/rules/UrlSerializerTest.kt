package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals

class UrlSerializerTest {
    @Test
    fun aUrlIsTheStringItHolds() {
        assertEquals(
            Url("https://www.example.com/refuge"),
            wireJson.decodeFromString(Url.serializer(), "\"https://www.example.com/refuge\""),
        )
        assertEquals(
            "\"https://www.example.com/refuge\"",
            wireJson.encodeToString(Url.serializer(), Url("https://www.example.com/refuge")),
        )
    }

    /**
     * An answer's web address is kept as the text the wire sent, and written back as it was read, so
     * writing an answer's JSON never refuses what reading it kept.
     */
    @Test
    fun aValueWithNoSchemeOrOneAParserWouldRewriteIsKeptAsWrittenBothWays() {
        for (text in listOf(
            "www.example.com",
            "HTTPS://REFUGE.example:443/a/../b",
            "https://refuge.example/sale-50%-off",
        )) {
            val read = wireJson.decodeFromString(UrlSerializer, "\"$text\"")
            assertEquals(text, read.toString())
            assertEquals("\"$text\"", wireJson.encodeToString(UrlSerializer, read))
        }
    }
}
