package com.mountaya.admin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class WebhookSignatureTest {
    @Test
    fun theSignatureIsTheContractVectorsHmacSha256OfTDotBodyInLowercaseHex() {
        assertEquals(
            Delivery.VECTOR_SIGNATURE,
            WebhookSignature.expected("${Delivery.SIGNED_AT}", Delivery.BODY.encodeToByteArray(), Delivery.SECRET),
        )
    }

    @Test
    fun theKeyIsTheWholeSecretItsPrefixIncludedAndTheTIsSignedAsWritten() {
        val body = "{}".encodeToByteArray()
        val whole = WebhookSignature.expected("5", body, "whsk_a")

        assertNotEquals(WebhookSignature.expected("5", body, "a"), whole)
        assertNotEquals(WebhookSignature.expected("05", body, "whsk_a"), whole)
        assertEquals(64, whole.length)
        assertTrue(whole.all { it in "0123456789abcdef" })
    }

    @Test
    fun anyCandidateThatMatchesVerifiesWhereverItStandsAmongTheOthers() {
        assertTrue(WebhookSignature.verifies(listOf("ab", "cd"), "cd"))
        assertTrue(WebhookSignature.verifies(listOf("cd", "ab"), "cd"))
        assertFalse(WebhookSignature.verifies(listOf("ab", "ce"), "cd"))
        assertFalse(WebhookSignature.verifies(emptyList(), "cd"))
    }

    @Test
    fun twoSignaturesAreEqualOnlyByteForByteALengthApartNever() {
        assertTrue(WebhookSignature.equalInConstantTime("ad18", "ad18"))
        assertFalse(WebhookSignature.equalInConstantTime("ad19", "ad18"))
        assertFalse(WebhookSignature.equalInConstantTime("AD18", "ad18"))
        assertFalse(WebhookSignature.equalInConstantTime("ad1", "ad18"))
        assertFalse(WebhookSignature.equalInConstantTime("", "ad18"))
    }

    @Test
    fun bytesAreSpelledInLowercaseHexTwoCharactersAByte() {
        assertEquals("00ff7f80", WebhookSignature.hex(byteArrayOf(0, -1, 127, -128)))
        assertEquals("", WebhookSignature.hex(ByteArray(0)))
    }
}
