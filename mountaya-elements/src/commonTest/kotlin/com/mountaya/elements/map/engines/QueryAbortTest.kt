package com.mountaya.elements

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class QueryAbortTest {
    private class Refused : Exception()

    @Test
    fun aQueryThatAnswersFirstIsTheRacesAnswer() =
        runTest {
            assertEquals(42, QueryAbort().race { 42 })
        }

    @Test
    fun aQuerysOwnFailureIsTheRaces() =
        runTest {
            assertFailsWith<Refused> { QueryAbort().race<Int> { throw Refused() } }
        }

    @Test
    fun anAbortPulledBeforeTheQueryAnswersEndsTheRaceAsCancelledAndCancelsTheQuery() =
        runTest {
            val abort = QueryAbort()
            var cancelled = false
            abort.pull()

            assertFailsWith<CancellationException> {
                abort.race<Int> {
                    try {
                        awaitCancellation()
                    } finally {
                        cancelled = true
                    }
                }
            }
            assertTrue(cancelled, "the query is cancelled")
        }

    @Test
    fun anAbortPulledWhileTheQueryWaitsEndsTheRaceAtOnce() =
        runTest {
            val abort = QueryAbort()
            val race = async { runCatching { abort.race<Int> { awaitCancellation() } } }
            runCurrent()
            abort.pull()

            assertTrue(race.await().exceptionOrNull() is CancellationException)
        }

    @Test
    fun anAbortPulledAfterTheAnswerChangesNothing() =
        runTest {
            val abort = QueryAbort()
            val answer = abort.race { 7 }
            abort.pull()

            assertEquals(7, answer)
        }
}
