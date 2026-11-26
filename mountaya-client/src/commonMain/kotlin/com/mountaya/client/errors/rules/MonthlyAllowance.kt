package com.mountaya.client

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * MonthlyAllowance is what a `429` refused for an organization's spent monthly allowance says to its
 * caller: nothing lifts it before the organization's next billing period begins but a larger allowance
 * in its agreement, so the error names when that is, in the seconds its `Retry-After` names and the
 * instant in UTC they end, rather than a wait a caller would sit out.
 */
internal object MonthlyAllowance {
    /** The policy the monthly allowance is counted under, as `RateLimit-Policy` names it. */
    val policy: String = "month"

    /** The latest instant the step names, the last second of the year 9999: a wait that ends past it is named in seconds alone. */
    private val latestInstant: Instant = Instant.fromEpochSeconds(253_402_300_799)

    /**
     * Returns whether a `429`'s rate-limit [state] says it was refused for a spent monthly allowance: its
     * `month` policy has nothing left, and no other policy it lists is spent.
     *
     * A refusal for the allowance is weighed before every per-minute bound, so it lists none of them
     * spent; a per-minute refusal while the allowance is spent to its last unit lists that bound spent
     * too, and stays a wait. A policy that states nothing left is not spent.
     */
    fun isSpent(state: RateLimitState?): Boolean {
        val policies = state?.policies.orEmpty()
        return policies.any { it.name == policy && it.remaining == 0L } &&
            policies.all { it.name == policy || it.remaining != 0L }
    }

    /**
     * Returns the sentence that names when the allowance renews: in the whole seconds [retryAfter] names,
     * counted from [answeredAt], the instant the answer arrived, and the instant in UTC they end, each left
     * out where it is not known, and the seconds where they are none.
     */
    fun step(
        retryAfter: Duration?,
        answeredAt: Instant?,
    ): String {
        val seconds = retryAfter?.inWholeSeconds ?: 0
        val time =
            if (seconds <= 0) {
                ""
            } else {
                val end = answeredAt?.let { it + seconds.seconds }?.takeIf { it <= latestInstant }
                " in ${NextStep.seconds(seconds)}" + (end?.let { " (at ${formatTimestampToTheSecond(it)})" } ?: "")
            }
        return "The organization's monthly allowance on this API is spent: it renews when the next billing period " +
            "begins$time, so this call is not repeated before then. A larger allowance is set in the " +
            "organization's agreement with Mountaya (https://mountaya.com/en/contact/)."
    }
}
