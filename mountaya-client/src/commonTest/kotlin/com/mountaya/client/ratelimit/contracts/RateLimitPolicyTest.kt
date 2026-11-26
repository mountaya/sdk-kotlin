package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

class RateLimitPolicyTest {
    @Test
    fun aPolicyIsItsNameItsQuotaAndTheFiguresItsAnswerStated() {
        val minute = RateLimitPolicy("minute", 60, 60.seconds, 12, 41.seconds)

        assertEquals(RateLimitPolicy("minute", 60, 60.seconds, 12, 41.seconds), minute)
        assertNotEquals(RateLimitPolicy("minute", 60), minute)
        assertNull(RateLimitPolicy("minute", 60).remaining)
        assertEquals(minute.hashCode(), RateLimitPolicy("minute", 60, 60.seconds, 12, 41.seconds).hashCode())
    }

    @Test
    fun aQuotaPastThirtyTwoBitsIsKeptWhole() {
        assertEquals(5_000_000_000L, RateLimitPolicy("month", 5_000_000_000L).quota)
    }
}
