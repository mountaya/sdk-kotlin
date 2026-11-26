package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class ConfigurationExceptionTest {
    @Test
    fun itsMessageIsTheOneGiven() {
        val failure = ConfigurationException("a publishable key starts with `pk_`")

        assertEquals("a publishable key starts with `pk_`", failure.message)
    }

    @Test
    fun itCarriesTheKeyItsCallWasFixedToAndWhatMadeTheRefusal() {
        val mint = IllegalStateException("the mint failed")
        val failure = ConfigurationException("no session", idempotencyKey = "order-7f3a", cause = mint)

        assertEquals("order-7f3a", failure.idempotencyKey)
        assertSame(mint, failure.cause)
        assertNull(ConfigurationException("x").idempotencyKey)
        assertNull(ConfigurationException("x").cause)
    }
}
