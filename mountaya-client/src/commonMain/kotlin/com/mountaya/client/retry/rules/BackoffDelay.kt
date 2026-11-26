package com.mountaya.client

import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

/** The most a `Retry-After` wait is lengthened by its draw. */
internal val retryAfterJitter: Duration = 1.seconds

/**
 * The least a repeat of a 409 whose key is still in use waits, whatever its answer names, so that
 * every repeat spends the budget that bounds it.
 */
internal val keyInUseMinimum: Duration = 1.seconds

/**
 * Returns how long to wait before repeating attempt number [attempt], counted from 0, or null where
 * that wait does not fit what the waits the call took before it, [waited], left of the policy's
 * `waitBudget`: such a wait is not taken, and the attempt settles with the error its answer maps to.
 * A wait that ends exactly at the budget fits it.
 *
 * The wait is [draw], a uniform fraction from 0 to 1, of a ceiling that starts at the policy's
 * `baseDelay`, doubles with each attempt and stops at its `maxDelay`. Full jitter, rather than
 * the exponential itself, is the rule: every client that failed in the same moment would
 * otherwise repeat in the same moment, and against a fixed per-minute window that keeps the
 * window spent.
 *
 * [retryAfter] is a `Retry-After` the response carried, already parsed, and it replaces the
 * schedule outright, ceiling included, with its seconds plus [draw] of [retryAfterJitter], so the
 * clients one second refused do not all come back in the same millisecond. A wait shorter than
 * [minimum] is lengthened to it before it is held to the budget.
 */
internal fun backoffDelay(
    attempt: Int,
    draw: Double,
    retryAfter: Duration?,
    policy: RetryPolicy,
    waited: Duration = Duration.ZERO,
    minimum: Duration = Duration.ZERO,
): Duration? {
    val wait =
        if (retryAfter != null) {
            retryAfter + (draw * retryAfterJitter.toDouble(DurationUnit.MILLISECONDS)).milliseconds
        } else {
            val base = policy.baseDelay.toDouble(DurationUnit.MILLISECONDS)
            val max = policy.maxDelay.toDouble(DurationUnit.MILLISECONDS)
            // Doubling past the ceiling only ever reaches the ceiling, and a zero base stays zero
            // however far it doubles, so neither the exponent nor the product needs to be finite.
            val exponential = if (base <= 0.0) 0.0 else base * 2.0.pow(attempt)
            (draw * minOf(exponential, max)).milliseconds
        }
    val taken = maxOf(wait, minimum)
    return if (waited + taken <= policy.waitBudget) taken else null
}
