package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PersistedQueryAttemptTest {
    @Test
    fun onlyAnAttemptThatSendsADigestAndNoDocumentIsDigestOnly() {
        assertTrue(PersistedQueryAttempt("abc", sendsDocument = false).isDigestOnly)
        assertFalse(PersistedQueryAttempt("abc", sendsDocument = true).isDigestOnly)
        assertFalse(PersistedQueryAttempt(null, sendsDocument = true).isDigestOnly)
    }

    @Test
    fun anAttemptIsWhatItSends() {
        assertEquals(PersistedQueryAttempt("abc", false), PersistedQueryAttempt("abc", false))
        assertEquals(PersistedQueryAttempt("abc", false).hashCode(), PersistedQueryAttempt("abc", false).hashCode())
        assertNotEquals(PersistedQueryAttempt("abc", false), PersistedQueryAttempt("abc", true))
        assertEquals(
            "PersistedQueryAttempt(digest=abc, sendsDocument=false)",
            PersistedQueryAttempt("abc", false).toString(),
        )
    }
}
