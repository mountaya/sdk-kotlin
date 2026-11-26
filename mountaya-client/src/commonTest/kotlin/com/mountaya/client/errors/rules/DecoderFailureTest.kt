package com.mountaya.client

import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DecoderFailureTest {
    private val token = "sess_test_4f9a2c71d8e3"

    /** The failure the client's decoder throws for a session whose user is no string, beside its token. */
    private fun thrown(): IllegalArgumentException =
        assertFailsWith<IllegalArgumentException> {
            wireJson.decodeFromString(
                RestSchema.Session.serializer(),
                """{"expires_at":"2026-11-23T10:05:00Z","user_id":["u1"],"token":"$token"}""",
            )
        }

    @Test
    fun theDecodersOwnFailureCopiesTheInputItReadSoATokenRidesInIt() {
        // What the rule is for: the decoder this client decodes with writes the body into its message.
        assertTrue(token in thrown().message.orEmpty(), "kotlinx.serialization no longer copies its input")
    }

    @Test
    fun theFailureRebuiltSaysWhereAndWhyAndHoldsNoByteOfTheInputAndNoCause() {
        val failure = decoderFailure(thrown())

        val message = failure.message.orEmpty()
        assertTrue("user_id" in message, message)
        assertFalse("JSON input" in message, message)
        assertFalse(token in message, message)
        assertFalse(token in failure.stackTraceToString())
        assertNull(failure.cause)
    }

    @Test
    fun aFailureThatCopiedNothingKeepsItsMessageWholeAndOneWithNoneHasNone() {
        val said = "The envelope carries no `data`."
        assertEquals(said, decoderFailure(SerializationException(said)).message)
        assertNull(decoderFailure(IllegalArgumentException()).message)
    }
}
