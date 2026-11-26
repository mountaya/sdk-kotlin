package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class UrlTest {
    @Test
    fun aUrlIsTheTextAsWritten() {
        assertEquals("https://proxy.example.com/mountaya", Url("https://proxy.example.com/mountaya").toString())
        assertEquals("HTTPS://Data.Mountaya.com/", Url("HTTPS://Data.Mountaya.com/").toString())
        assertEquals("gateway.example.com/v1", Url("gateway.example.com/v1").toString())
    }

    /**
     * The text is read as a member too, as every SDK reads it, so an address an answer carries is read as the wire
     * sent it without going through `toString`.
     */
    @Test
    fun aUrlsTextIsReadAsWritten() {
        assertEquals("HTTPS://Data.Mountaya.com/", Url("HTTPS://Data.Mountaya.com/").text)
        assertEquals("gateway.example.com/v1", Url("gateway.example.com/v1").text)
        assertEquals("", Url("").text)
    }

    @Test
    fun twoUrlsAreEqualWhenTheirTextIs() {
        assertEquals(Url("https://data.mountaya.com"), Url("https://data.mountaya.com"))
        assertNotEquals(Url("https://data.mountaya.com"), Url("https://data.mountaya.com/"))
    }

    @Test
    fun anySchemeIsAScheme() {
        for (text in listOf("http://localhost:8080", "urn:isbn:0451450523", "mountaya+test://x")) {
            assertTrue(Url(text).hasScheme, text)
        }
    }

    @Test
    fun aStringWithNoSchemeHoldsNone() {
        val schemeless =
            listOf("", "data.mountaya.com", "/v1/itineraries", "://x.com", "https:", "1http://x", "https:\nx")
        for (text in schemeless) {
            assertFalse(Url(text).hasScheme, text)
        }
    }
}
