package com.mountaya.client

import com.mountaya.client.generated.ApiErrorCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PersistedQueryRefusalTest {
    @Test
    fun eachRefusalIsSpelledByAMessageAndByACode() {
        for (refusal in PersistedQueryRefusal.entries) {
            assertTrue(refusal.messages.isNotEmpty(), "$refusal has no message")
            assertTrue(refusal.codes.isNotEmpty(), "$refusal has no code")
        }
    }

    @Test
    fun noWordSpellsTwoRefusals() {
        val messages = PersistedQueryRefusal.entries.flatMap { it.messages }
        val codes = PersistedQueryRefusal.entries.flatMap { it.codes }

        assertEquals(messages.distinct(), messages)
        assertEquals(codes.distinct(), codes)
    }

    @Test
    fun aParseFailureIsNoRefusalAndTheDataApisOwnDeclineIsAnEntryOfACodeAndAMessage() {
        assertFalse(PersistedQueryRefusal.entries.any { ApiErrorCode("GRAPHQL_PARSE_FAILED") in it.codes })
        assertEquals(
            listOf(ApiErrorCode("GRAPHQL_VALIDATION_FAILED") to "no operation provided"),
            PersistedQueryRefusal.unsupportedEntries,
        )
    }
}
