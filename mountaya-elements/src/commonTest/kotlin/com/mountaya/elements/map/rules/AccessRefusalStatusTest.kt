package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AccessRefusalStatusTest {
    @Test
    fun statuses401402And403RefuseAccessAndEveryOtherStatusOrNoneIsAFault() {
        assertEquals(listOf(401, 402, 403), accessRefusalStatuses)
        assertEquals(listOf(401, 402, 403), (100..599).filter { accessRefusalStatus(it) != null })
        assertEquals(402, accessRefusalStatus(402))
        assertNull(accessRefusalStatus(null))
        assertNull(accessRefusalStatus(404))
        assertNull(accessRefusalStatus(429))
    }

    @Test
    fun only401And403RefuseTheCredentialARequestCarriedA402TheEntitlementWhateverAsked() {
        assertEquals(listOf(401, 403), (100..599).filter { isCredentialRefusal(it) })
        assertTrue(isCredentialRefusal(401))
        assertTrue(isCredentialRefusal(403))
        assertFalse(isCredentialRefusal(402), "a 402 refuses the organization's entitlement, not the credential")
        assertFalse(isCredentialRefusal(404))
        assertFalse(isCredentialRefusal(null), "a request that reached no answer refuses nothing")
    }
}
