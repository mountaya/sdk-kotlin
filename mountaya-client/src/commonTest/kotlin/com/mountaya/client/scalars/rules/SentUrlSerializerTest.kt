package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SentUrlSerializerTest {
    @Test
    fun aWebAddressARequestSendsIsWrittenAsItsText() {
        for (text in listOf(
            "https://gateway.example.com/v1",
            "HTTPS://GATEWAY.example:443/v1/../v2/caf%C3%A9",
            "urn:x:1",
        )) {
            assertEquals("\"$text\"", wireJson.encodeToString(SentUrlSerializer, Url(text)))
        }
    }

    @Test
    fun aWebAddressWithNoSchemeIsRefusedAsItIsWritten() {
        for (text in listOf("gateway.example.com/v1", "/v1/itineraries", "", "https:", "1http://x", "https:\nx")) {
            assertFailsWith<ConfigurationException>(text) { wireJson.encodeToString(SentUrlSerializer, Url(text)) }
            assertFailsWith<ConfigurationException>(text) { SentUrlSerializer.sentText(Url(text)) }
        }
    }

    @Test
    fun anAnswersWebAddressIsReadWhateverItHolds() {
        assertEquals(
            Url("gateway.example.com/v1"),
            wireJson.decodeFromString(SentUrlSerializer, "\"gateway.example.com/v1\""),
        )
    }
}
