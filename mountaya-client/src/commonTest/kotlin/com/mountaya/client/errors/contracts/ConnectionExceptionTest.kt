package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class ConnectionExceptionTest {
    @Test
    fun itSaysTheRequestNeverArrived() {
        assertEquals("the request never arrived", ConnectionException().message)
        assertNull(ConnectionException().cause)
    }

    @Test
    fun itCarriesTheKeyItsCallSentAndThePlatformsFailureBeneathIt() {
        val dropped = IllegalStateException("socket closed")
        val failure = ConnectionException("order-7f3a", dropped)

        assertEquals("order-7f3a", failure.idempotencyKey)
        assertSame(dropped, failure.cause)
        assertNull(ConnectionException().idempotencyKey)
    }
}
