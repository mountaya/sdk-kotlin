package com.mountaya.client

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * AttemptBudget decides how long one attempt may take before it is given up as a request that
 * never arrived.
 *
 * The budget is a deadline the transport holds itself, from the moment the attempt starts to the
 * last byte of its answer, the wait for a first session token included. It is the call's and the
 * client's, never a retry policy's, since a policy is about the waits between attempts.
 */
internal object AttemptBudget {
    /** A client's budget where it states none. */
    val clientDefault: Duration = 30.seconds

    /**
     * What the operation's own budget is widened by, so the API's own 504 arrives before the client
     * gives up on it and a write the API is still running is not abandoned for a repeat that would
     * meet it in flight.
     */
    val margin: Duration = 5.seconds

    /**
     * Returns an attempt's budget: the call's own where it names one, so a caller can wait less or
     * more than the API would, and otherwise the larger of the client's and the operation's own
     * budget plus the margin. An operation that declares none, as an event stream does, takes the
     * client's.
     */
    fun budget(
        call: Duration?,
        client: Duration,
        operation: Duration?,
    ): Duration {
        if (call != null) return call
        if (operation == null) return client
        return maxOf(client, operation + margin)
    }

    /** Refuses a budget of zero or less, which no attempt could fit in, naming it as [part]. */
    fun check(
        budget: Duration,
        part: String,
    ) {
        if (budget <= Duration.ZERO) {
            throw ConfigurationException("$part is a time budget, and one of zero or less fits no attempt.")
        }
    }
}
