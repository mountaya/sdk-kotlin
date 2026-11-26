package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class RetryPolicyTest {
    @Test
    fun theDefaultsAreTheContracts() {
        val policy = RetryPolicy()

        assertEquals(2, policy.maxRetries)
        assertEquals(500.milliseconds, policy.baseDelay)
        assertEquals(30.seconds, policy.maxDelay)
        assertEquals(60.seconds, policy.waitBudget)
    }

    @Test
    fun eachValueIsSetOnItsOwn() {
        val policy = RetryPolicy(maxDelay = 5.seconds)

        assertEquals(2, policy.maxRetries)
        assertEquals(500.milliseconds, policy.baseDelay)
        assertEquals(5.seconds, policy.maxDelay)
        assertEquals(60.seconds, policy.waitBudget)
        assertEquals(90.seconds, RetryPolicy(waitBudget = 90.seconds).waitBudget)
    }

    @Test
    fun theStandardPolicyIsTheContracts() {
        assertEquals(RetryPolicy(), RetryPolicy.standard)
        assertEquals(RetryPolicy().hashCode(), RetryPolicy.standard.hashCode())
        assertEquals(
            "RetryPolicy(maxRetries=2, baseDelay=500ms, maxDelay=30s, waitBudget=1m)",
            RetryPolicy.standard.toString(),
        )
        assertNotEquals(RetryPolicy(), RetryPolicy(waitBudget = 30.seconds))
    }

    @Test
    fun aNegativeCountOrWaitIsRefusedWhereItIsBuilt() {
        assertFailsWith<ConfigurationException> { RetryPolicy(maxRetries = -1) }
        assertFailsWith<ConfigurationException> { RetryPolicy(baseDelay = (-1).milliseconds) }
        assertFailsWith<ConfigurationException> { RetryPolicy(maxDelay = (-1).seconds) }
        assertFailsWith<ConfigurationException> { RetryPolicy(waitBudget = (-1).milliseconds) }
        assertEquals(0, RetryPolicy(maxRetries = 0).maxRetries)
        assertEquals(Duration.ZERO, RetryPolicy(waitBudget = Duration.ZERO).waitBudget)
    }
}
