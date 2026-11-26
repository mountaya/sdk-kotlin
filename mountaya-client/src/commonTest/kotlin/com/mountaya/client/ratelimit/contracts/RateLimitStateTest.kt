package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class RateLimitStateTest {
    @Test
    fun aStateIsItsPoliciesInOrder() {
        val minute = RateLimitPolicy("minute", 60)
        val month = RateLimitPolicy("month", 100_000)

        assertEquals(RateLimitState(listOf(minute, month)), RateLimitState(listOf(minute, month)))
        assertNotEquals(RateLimitState(listOf(month, minute)), RateLimitState(listOf(minute, month)))
    }
}
