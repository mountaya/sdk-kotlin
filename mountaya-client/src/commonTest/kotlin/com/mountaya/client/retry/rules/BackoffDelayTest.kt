package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class BackoffDelayTest {
    private val policy = RetryPolicy()

    @Test
    fun theCeilingDoublesWithEachAttemptUntilItReachesTheMaximum() {
        val ceilings = (0..7).map { backoffDelay(attempt = it, draw = 1.0, retryAfter = null, policy = policy) }

        assertEquals(
            listOf(500, 1_000, 2_000, 4_000, 8_000, 16_000, 30_000, 30_000).map { it.milliseconds },
            ceilings,
        )
    }

    @Test
    fun theWaitIsTheDrawsFractionOfTheCeiling() {
        assertEquals(Duration.ZERO, backoffDelay(attempt = 3, draw = 0.0, retryAfter = null, policy = policy))
        assertEquals(2.seconds, backoffDelay(attempt = 3, draw = 0.5, retryAfter = null, policy = policy))
        assertEquals(1_000.milliseconds, backoffDelay(attempt = 3, draw = 0.25, retryAfter = null, policy = policy))
    }

    @Test
    fun aRetryAfterReplacesTheScheduleWithItsSecondsAndUpToOneMoreSecond() {
        assertEquals(42.seconds, backoffDelay(attempt = 0, draw = 0.0, retryAfter = 42.seconds, policy = policy))
        assertEquals(
            42_500.milliseconds,
            backoffDelay(attempt = 0, draw = 0.5, retryAfter = 42.seconds, policy = policy),
        )
        assertEquals(43.seconds, backoffDelay(attempt = 6, draw = 1.0, retryAfter = 42.seconds, policy = policy))
        assertEquals(Duration.ZERO, backoffDelay(attempt = 5, draw = 0.0, retryAfter = Duration.ZERO, policy = policy))
    }

    /**
     * A wait that ends exactly at the budget fits it, and one past what the earlier waits left is
     * not taken.
     */
    @Test
    fun aWaitIsTakenOnlyWhereItFitsWhatTheWaitsBeforeItLeft() {
        assertEquals(59.seconds, backoffDelay(0, 0.0, 59.seconds, policy, waited = 1.seconds))
        assertNull(backoffDelay(0, 0.001, 59.seconds, policy, waited = 1.seconds))
        assertNull(backoffDelay(0, 0.0, 61.seconds, policy))
        assertNull(backoffDelay(0, 1.0, null, policy, waited = 59_600.milliseconds))
    }

    @Test
    fun aWiderBudgetWaitsOutALongerWaitAndABudgetOfNothingWaitsNone() {
        assertEquals(90.seconds, backoffDelay(0, 0.0, 90.seconds, RetryPolicy(waitBudget = 120.seconds)))
        assertNull(backoffDelay(0, 0.5, null, RetryPolicy(waitBudget = Duration.ZERO)))
        assertEquals(Duration.ZERO, backoffDelay(0, 0.0, null, RetryPolicy(waitBudget = Duration.ZERO)))
    }

    @Test
    fun anAttemptFarPastTheCeilingStaysAtTheCeiling() {
        assertEquals(30.seconds, backoffDelay(attempt = Int.MAX_VALUE, draw = 1.0, retryAfter = null, policy = policy))
    }

    @Test
    fun aZeroBaseNeverWaits() {
        val immediate = RetryPolicy(baseDelay = Duration.ZERO)

        val wait = backoffDelay(attempt = Int.MAX_VALUE, draw = 1.0, retryAfter = null, policy = immediate)

        assertEquals(Duration.ZERO, wait)
    }

    @Test
    fun aWaitShorterThanItsMinimumIsLengthenedToItBeforeItIsHeldToTheBudget() {
        val policy =
            RetryPolicy(maxRetries = 0, baseDelay = Duration.ZERO, maxDelay = Duration.ZERO, waitBudget = 2.seconds)

        assertEquals(keyInUseMinimum, backoffDelay(0, 0.0, Duration.ZERO, policy, minimum = keyInUseMinimum))
        assertEquals(1.5.seconds, backoffDelay(0, 0.5, 1.seconds, policy, minimum = keyInUseMinimum))
        assertNull(backoffDelay(0, 0.0, null, policy, waited = 1.5.seconds, minimum = keyInUseMinimum))
    }

    @Test
    fun aRetryAfterPastTheBudgetIsNotWaited() {
        assertNull(backoffDelay(0, 0.0, 2_147_484.seconds, RetryPolicy.standard))
    }
}
