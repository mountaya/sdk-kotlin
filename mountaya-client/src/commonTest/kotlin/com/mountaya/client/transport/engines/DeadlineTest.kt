package com.mountaya.client

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class DeadlineTest {
    @Test
    fun anOperationThatFinishesInTimeIsAnsweredOneThatDoesNotIsExpiredAtItsBudget() =
        runTest {
            val finished =
                Deadline.run(1.seconds) {
                    delay(999.milliseconds)
                    "done"
                }
            assertIs<Deadline.Raced.Finished<String>>(finished)
            assertEquals("done", finished.value)

            val started = currentTime
            assertIs<Deadline.Raced.Expired>(Deadline.run(1.seconds) { awaitCancellation() })
            assertEquals(1_000, currentTime - started)

            // A value of null is the operation's own answer, never a spent budget.
            assertIs<Deadline.Raced.Finished<String?>>(Deadline.run(1.seconds) { null })
        }

    @Test
    fun theCallersCancellationIsNeverReadAsASpentBudget() =
        runTest {
            var thrown: Throwable? = null
            val caller =
                launch {
                    try {
                        Deadline.run(10.seconds) { awaitCancellation() }
                    } catch (cancelled: CancellationException) {
                        thrown = cancelled
                        throw cancelled
                    }
                }
            delay(100.milliseconds)
            caller.cancel(CancellationException("the caller left"))
            caller.join()

            assertTrue(caller.isCancelled)
            assertEquals("the caller left", thrown?.message)
        }

    @Test
    fun anOperationsOwnFailureComesThroughTheRaceUnchanged() =
        runTest {
            val failure = IllegalStateException("the operation failed")
            val thrown = kotlin.runCatching { Deadline.run(1.seconds) { throw failure } }.exceptionOrNull()

            // Coroutines may rethrow a copy that recovers the stack, so the failure is read by its kind and its words.
            assertIs<IllegalStateException>(thrown)
            assertEquals(failure.message, thrown.message)
        }
}
