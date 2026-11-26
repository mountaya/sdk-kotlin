package com.mountaya.elements

import com.mountaya.client.Url
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MapRequestTest {
    @Test
    fun aRequestIsWhereItGoesAndWhatItCarriesEachHeaderAsWrittenAndALogShowsNoValue() {
        val url = Url("https://tiles.mountaya.com/v1/zones/1/2/3")
        val request = MapRequest(url, mapOf("X-App" to "café"))

        assertEquals(url, request.url)
        assertEquals(MapRequest(url, mapOf("X-App" to "café")), request)
        assertNotEquals(MapRequest(url, mapOf("X-App" to "café")), request)
        assertNotEquals(MapRequest(url), request)
        assertTrue(MapRequest(url).headers.isEmpty())
        assertFalse(MapRequest(url, mapOf("X-API-Key" to "pk_test_a")).toString().contains("pk_test_a"))
    }
}
