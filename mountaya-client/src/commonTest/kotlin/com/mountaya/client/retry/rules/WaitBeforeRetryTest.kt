package com.mountaya.client

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.testTimeSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** The virtual clock is what shows when a wait settled, and kotlinx-coroutines-test marks it experimental. */
@OptIn(ExperimentalCoroutinesApi::class)
class WaitBeforeRetryTest {
    private val reason = CancellationException("the caller left")

    /** The caller's own exception, unwrapped from any copy the coroutine machinery made of it in debug mode. */
    private fun Throwable.callersReason(): Throwable? =
        generateSequence(this) { it.cause }.firstOrNull { it === reason }

    @Test
    fun aWaitNobodyCancelsLastsItsDelay() =
        runTest {
            val started = testTimeSource.markNow()
            waitBeforeRetry(10.seconds)

            assertEquals(10.seconds, started.elapsedNow())
        }

    @Test
    fun aCancellationDuringTheWaitSettlesItThereAndThen() =
        runTest {
            val started = testTimeSource.markNow()
            var thrown: Throwable? = null
            var settledAfter: Duration? = null
            val waiting =
                launch {
                    try {
                        waitBeforeRetry(10.seconds)
                    } catch (cancelled: CancellationException) {
                        thrown = cancelled
                        settledAfter = started.elapsedNow()
                        throw cancelled
                    }
                }
            launch {
                delay(4.seconds)
                waiting.cancel(reason)
            }
            waiting.join()

            assertEquals(4.seconds, settledAfter)
            assertSame(reason, thrown?.callersReason())
        }

    @Test
    fun aCallerAlreadyCancelledSettlesWithoutWaiting() =
        runTest {
            val started = testTimeSource.markNow()
            var thrown: Throwable? = null
            val waiting =
                launch {
                    coroutineContext.job.cancel(reason)
                    try {
                        waitBeforeRetry(10.seconds)
                    } catch (cancelled: CancellationException) {
                        thrown = cancelled
                        throw cancelled
                    }
                }
            waiting.join()

            assertEquals(Duration.ZERO, started.elapsedNow())
            assertSame(reason, thrown)
        }

    @Test
    fun aCallerAlreadyCancelledIsNotLetThroughByAWaitOfZero() =
        runTest {
            var thrown: Throwable? = null
            var passed = false
            val waiting =
                launch {
                    coroutineContext.job.cancel(reason)
                    try {
                        waitBeforeRetry(Duration.ZERO)
                        passed = true
                    } catch (cancelled: CancellationException) {
                        thrown = cancelled
                        throw cancelled
                    }
                }
            waiting.join()

            assertFalse(passed)
            assertNotNull(thrown, "the wait throws")
        }

    @Test
    fun aWaitOfZeroForACallerStillThereReturnsAtOnce() =
        runTest {
            val started = testTimeSource.markNow()
            waitBeforeRetry(Duration.ZERO)

            assertEquals(Duration.ZERO, started.elapsedNow())
            assertTrue(coroutineContext.job.isActive)
        }
}
