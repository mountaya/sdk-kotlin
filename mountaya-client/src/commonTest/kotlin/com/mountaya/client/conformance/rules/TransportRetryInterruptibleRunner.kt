package com.mountaya.client

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.testTimeSource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * `transport.retry.interruptible`, with the fixture's abort signal read as coroutine cancellation
 * (SPEC.md section 9).
 *
 * `before` is a coroutine cancelled before it calls the wait, `during` one cancelled halfway
 * through it, and `never` one that is not cancelled. `none`, no signal at all, is the wait called
 * straight from the test's own coroutine rather than from a job of its own. The signal's reason
 * is the `CancellationException` the job is cancelled with. In debug mode the coroutine machinery
 * may rethrow a copy of it carrying the original as its cause, which is still the caller's
 * reason, so the cause chain is what is read. Time is the test's virtual time, whose clock
 * kotlinx-coroutines-test still marks experimental.
 */
@OptIn(ExperimentalCoroutinesApi::class)
object TransportRetryInterruptibleRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val delay = input.number("delayMs").milliseconds
        val reason = CancellationException("${case.name}: the caller's own reason")
        val started = scope.testTimeSource.markNow()
        var settledAfter: Duration? = null
        var thrown: Throwable? = null

        when (val abort = input.text("abort")) {
            "none" -> {
                waitBeforeRetry(delay)
                settledAfter = started.elapsedNow()
            }

            "before", "during", "never" -> {
                val waiting =
                    scope.launch {
                        if (abort == "before") coroutineContext.job.cancel(reason)
                        try {
                            waitBeforeRetry(delay)
                        } catch (cancelled: CancellationException) {
                            thrown = cancelled
                            throw cancelled
                        } finally {
                            settledAfter = started.elapsedNow()
                        }
                    }
                if (abort == "during") {
                    scope.launch {
                        delay(delay / 2)
                        waiting.cancel(reason)
                    }
                }
                waiting.join()
            }

            else -> {
                throw AssertionError("an abort of \"$abort\", which the runner does not know")
            }
        }

        val settled = settledAfter ?: throw AssertionError("the wait never settled")
        val carriesReason = generateSequence(thrown) { it.cause }.any { it === reason }
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "outcome" to FixtureValue.Text(if (thrown == null) "resolved" else "rejected"),
                    "reasonIsAbortReason" to FixtureValue.Bool(carriesReason),
                    "settledBeforeDelayElapsed" to FixtureValue.Bool(settled < delay),
                ),
            ),
        )
    }
}
