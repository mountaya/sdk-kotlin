package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class ExchangeRequestTest {
    @Test
    fun aHeaderIsReadInAnyCaseAndARequestPrintsNoneOfThem() {
        val request =
            ExchangeRequest(
                "POST",
                Url("https://api.mountaya.com/v1/things"),
                mapOf("X-API-Key" to "pk_test_abc"),
                "{}".encodeToByteArray(),
            )

        assertEquals("pk_test_abc", request.header("x-api-key"))
        assertNull(request.header("X-Session-Token"))
        assertEquals("ExchangeRequest(POST https://api.mountaya.com/v1/things)", request.toString())
        assertEquals(
            request,
            ExchangeRequest(
                "POST",
                Url("https://api.mountaya.com/v1/things"),
                mapOf("X-API-Key" to "pk_test_abc"),
                "{}".encodeToByteArray(),
            ),
        )
        assertNotEquals(
            request,
            ExchangeRequest(
                "POST",
                Url("https://api.mountaya.com/v1/things"),
                mapOf("X-API-Key" to "pk_test_abc"),
            ),
        )
    }
}
