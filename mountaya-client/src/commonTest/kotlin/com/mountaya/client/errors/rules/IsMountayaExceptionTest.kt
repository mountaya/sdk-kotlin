package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsMountayaExceptionTest {
    @Test
    fun aValueIsOfTheFamilyByTheNameItDeclaresAndOneTheApiAnsweredByItsRow() {
        for (status in listOf(400, 402, 409, 412, 418, 503)) {
            assertTrue(isMountayaException(ApiException.of(status)))
            assertTrue(isApiException(ApiException.of(status)))
        }
        assertTrue(isApiException(DataException()))
        assertTrue(isMountayaException(ConnectionException()))
        assertFalse(isApiException(ConnectionException()))
        assertFalse(isApiException(ConfigurationException("x")))
        assertTrue(isMountayaException(SelectionException(emptyList(), "x")))
        // The admin unit's `WebhookSignatureException` is of the family by the name it declares, as any class is.
        val refused =
            object : Exception("x"), NamedException {
                override val name: String = "WebhookSignatureError"
            }
        assertTrue(isMountayaException(refused))
        assertFalse(isApiException(refused))
        assertFalse(isMountayaException(IllegalStateException("ConflictError")))
        assertFalse(isMountayaException(null))
        assertFalse(isApiException(null))
    }
}
