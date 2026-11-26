package com.mountaya.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration

/**
 * Deadline runs an operation against a time budget, the deadline the transport holds itself for
 * every attempt and every read of an event stream.
 *
 * An idle interval would let a server that writes a byte a minute hold an attempt for ever, which
 * is why the budget is a deadline from the start of the operation and not a timeout between two
 * reads. It is the same code on every engine, where an engine's own timeout is that engine's
 * reading of one, and it answers its end as a value, so a spent budget is never read as the
 * caller's own cancellation.
 */
internal object Deadline {
    /** Raced is how an operation run against a deadline ended. */
    sealed interface Raced<out Value> {
        /** The operation's own value. */
        class Finished<Value>(
            val value: Value,
        ) : Raced<Value>

        /** A budget spent before the operation finished, which was then cancelled. */
        object Expired : Raced<Nothing>
    }

    /**
     * Returns what [operation] returns, or [Raced.Expired] where [budget] runs out first.
     *
     * An operation that throws before the budget runs out throws through, and a cancelled caller
     * throws its `CancellationException`, which is never read as a spent budget.
     */
    suspend fun <Value> run(
        budget: Duration,
        operation: suspend CoroutineScope.() -> Value,
    ): Raced<Value> = withTimeoutOrNull(budget) { Raced.Finished(operation()) } ?: Raced.Expired
}
