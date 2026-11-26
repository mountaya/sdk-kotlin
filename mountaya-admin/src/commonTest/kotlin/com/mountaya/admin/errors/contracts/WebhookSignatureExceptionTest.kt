package com.mountaya.admin

import com.mountaya.client.isApiException
import com.mountaya.client.isMountayaException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WebhookSignatureExceptionTest {
    @Test
    fun theReasonsAreSpelledAsTheContractSpellsThem() {
        assertEquals(
            listOf("missing-header", "malformed-header", "signature", "timestamp"),
            listOf(
                WebhookSignatureException.Reason.MissingHeader,
                WebhookSignatureException.Reason.MalformedHeader,
                WebhookSignatureException.Reason.Signature,
                WebhookSignatureException.Reason.Timestamp,
            ).map { it.value },
        )
        assertEquals("timestamp", WebhookSignatureException.Reason.Timestamp.toString())
    }

    @Test
    fun aRefusalPrintsTheContractsNameThenItsMessage() {
        val refused = WebhookSignatureException(WebhookSignatureException.Reason.Signature, "No v1 matches.")

        assertEquals("No v1 matches.", refused.message)
        assertEquals("WebhookSignatureError: No v1 matches.", refused.toString())
        assertEquals(WebhookSignatureException.Reason.Signature, refused.reason)
    }

    @Test
    fun aRefusalIsOfTheFamilyNoAnswerOfTheApiAndCarriesNoKey() {
        val refused = WebhookSignatureException(WebhookSignatureException.Reason.MissingHeader, "x")

        assertEquals("WebhookSignatureError", refused.name)
        assertTrue(isMountayaException(refused))
        assertFalse(isApiException(refused))
        assertNull(refused.idempotencyKey)
        assertNull(refused.cause)
    }
}
