package com.mountaya.client

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlin.time.Duration

/**
 * Waits [delay] before a repeated attempt, and settles at once when the caller is cancelled.
 *
 * A cancellation during the wait ends it there and then, which is the moment a caller is most
 * likely to be watching. A caller already cancelled when the wait begins settles without arming
 * a timer at all, and so does one handed a wait of zero: `delay` alone would return from a zero
 * wait without looking, and the next attempt would be sent for a caller who left. Either way the
 * caller's own `CancellationException` is what is thrown, never one of this SDK's.
 */
internal suspend fun waitBeforeRetry(delay: Duration) {
    currentCoroutineContext().ensureActive()
    delay(delay)
}
