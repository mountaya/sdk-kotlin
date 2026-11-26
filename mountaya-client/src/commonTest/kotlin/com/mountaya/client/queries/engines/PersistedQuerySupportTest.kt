package com.mountaya.client

import com.mountaya.client.generated.ApiErrorCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PersistedQuerySupportTest {
    @Test
    fun aFirstAttemptSendsTheDigestAloneWhereOneWasComputedAndTheDocumentOtherwise() {
        val support = PersistedQuerySupport()

        assertEquals(PersistedQueryAttempt("abc", sendsDocument = false), support.firstAttempt("abc"))
        assertEquals(PersistedQueryAttempt(null, sendsDocument = true), support.firstAttempt(null))
    }

    @Test
    fun aDeclinedProtocolIsNotProbedAgainForTheTransportsLifetime() {
        val support = PersistedQuerySupport()

        val refusal = support.read(listOf(ErrorEntry("PersistedQueryNotSupported", null)), support.firstAttempt("abc"))

        assertEquals(PersistedQueryRefusal.Unsupported, refusal)
        assertTrue(support.isDeclined)
        assertEquals(PersistedQueryAttempt(null, sendsDocument = true), support.firstAttempt("def"))
    }

    /**
     * Memoising a failure would turn one bad moment into a permanent opt-out, so only a declined
     * protocol changes what the next operation sends.
     */
    @Test
    fun aRegistrationAndADigestThatCouldNotBeComputedAreRememberedByNothing() {
        val support = PersistedQuerySupport()

        support.read(listOf(ErrorEntry("PersistedQueryNotFound", null)), support.firstAttempt("abc"))
        support.read(
            listOf(ErrorEntry("no operation provided", ApiErrorCode("GRAPHQL_VALIDATION_FAILED"))),
            support.firstAttempt(null),
        )

        assertFalse(support.isDeclined)
        assertEquals(PersistedQueryAttempt("def", sendsDocument = false), support.firstAttempt("def"))
    }
}
