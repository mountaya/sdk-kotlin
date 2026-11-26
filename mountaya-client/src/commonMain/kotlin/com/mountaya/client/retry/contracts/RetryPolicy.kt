package com.mountaya.client

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * RetryPolicy is how often a failed request is repeated and how long the client waits between
 * attempts.
 *
 * The defaults are the contract's: two attempts after the first, a 500 ms base, a 30 s ceiling
 * and a 60 s wait budget. A client configures its own, and a call overrides its client's through
 * its `RequestOptions`. A negative count, wait or budget is refused with [ConfigurationException]
 * where it is built, rather than read later as a wait of zero.
 */
public class RetryPolicy(
    /**
     * How many attempts may follow the first. Zero repeats nothing, but a 409 whose key is still in
     * use, which asks whether the first attempt has finished and is counted against no ceiling.
     */
    public val maxRetries: Int = 2,
    /** The ceiling of the first wait, which doubles with each attempt after it. */
    public val baseDelay: Duration = 500.milliseconds,
    /** The ceiling no wait's schedule grows past. A `Retry-After` the server sends is not held to it. */
    public val maxDelay: Duration = 30.seconds,
    /**
     * How long the waits of one call may last together. A wait that does not fit what the waits
     * before it left is not taken, and the attempt settles with the error its answer maps to.
     */
    public val waitBudget: Duration = 60.seconds,
) {
    init {
        if (maxRetries < 0) {
            throw ConfigurationException(
                "A retry policy repeats an attempt zero times or more, and this one says $maxRetries.",
            )
        }
        if (baseDelay.isNegative()) {
            throw ConfigurationException("A retry policy's base delay is zero or more, and this one is $baseDelay.")
        }
        if (maxDelay.isNegative()) {
            throw ConfigurationException("A retry policy's ceiling is zero or more, and this one is $maxDelay.")
        }
        if (waitBudget.isNegative()) {
            throw ConfigurationException("A retry policy's wait budget is zero or more, and this one is $waitBudget.")
        }
    }

    override fun equals(other: Any?): Boolean =
        other is RetryPolicy && other.maxRetries == maxRetries && other.baseDelay == baseDelay &&
            other.maxDelay == maxDelay && other.waitBudget == waitBudget

    override fun hashCode(): Int =
        ((maxRetries * 31 + baseDelay.hashCode()) * 31 + maxDelay.hashCode()) * 31 + waitBudget.hashCode()

    override fun toString(): String =
        "RetryPolicy(maxRetries=$maxRetries, baseDelay=$baseDelay, maxDelay=$maxDelay, waitBudget=$waitBudget)"

    /** The contract's policy. */
    public companion object {
        /**
         * The contract's policy: two retries after the first attempt, from a 500 ms base up to a 30 s
         * ceiling, every wait of a call out of one 60 s budget.
         */
        public val standard: RetryPolicy = RetryPolicy()
    }
}
