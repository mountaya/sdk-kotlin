package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Every source here suspends on a deferred the test completes, and every read runs in the test's
 * background scope, so every step runs on the test's scheduler in order, and `runCurrent` lets a step
 * land without a clock.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ItineraryResourceCacheTest {
    private fun TestScope.cache(
        reads: ScriptedReads,
        client: String = "a",
    ) = ItineraryResourceCache(backgroundScope, reads.source(client))

    /** Has an element ask about [itineraryId] once and go, as one drawn for a moment does. */
    private fun readOnce(
        cache: ItineraryResourceCache,
        itineraryId: String,
        surface: RestSchema.ReadSurface? = null,
    ) = ItineraryResourceEngine(cache, itineraryId, surface).close()

    @Test
    fun aCacheKeeps16AnswersTheCapacityTheFixtureDeclares() {
        assertEquals(16, ItineraryResourceCache.capacity)
    }

    @Test
    fun theAnswersReadMostRecentlyAreKeptToTheBoundTheOldestAskedForAgain() =
        runTest {
            val reads = ScriptedReads()
            val cache = cache(reads)
            for (index in 0..ItineraryResourceCache.capacity) {
                readOnce(cache, "i-$index")
                runCurrent()
                reads.answer(reads.sent.size - 1)
                runCurrent()
                // Reading the first id again keeps it the most recent.
                if (index == 1) {
                    readOnce(cache, "i-0")
                    runCurrent()
                }
            }
            assertEquals(ItineraryResourceCache.capacity + 1, reads.sent.size)

            readOnce(cache, "i-0")
            runCurrent()
            assertEquals(ItineraryResourceCache.capacity + 1, reads.sent.size, "i-0 is kept")

            readOnce(cache, "i-1")
            runCurrent()
            assertEquals(ItineraryResourceCache.capacity + 2, reads.sent.size, "i-1 was dropped")
        }

    /**
     * A read in flight is counted: dropped past the bound, it still lands for the engines waiting on it,
     * and is not kept.
     */
    @Test
    fun aReadDroppedPastTheBoundWhileInFlightLandsForItsEngineAndIsNotKept() =
        runTest {
            val reads = ScriptedReads()
            val cache = cache(reads)
            val waiting = ItineraryResourceEngine(cache, itineraryId = "first")
            runCurrent()
            for (index in 1..ItineraryResourceCache.capacity) readOnce(cache, "i-$index")
            runCurrent()

            reads.answer(0)
            runCurrent()
            assertEquals(0, shownRead(waiting.state.value))

            readOnce(cache, "first")
            runCurrent()
            assertEquals(ItineraryResourceCache.capacity + 2, reads.sent.size, "first was asked for again")
        }

    @Test
    fun anEngineAskingWhileAReadIsOutJoinsIt() =
        runTest {
            val reads = ScriptedReads()
            val cache = cache(reads)
            val profile = ItineraryResourceEngine(cache, itineraryId = "itin-1")
            val table = ItineraryResourceEngine(cache, itineraryId = "itin-1")
            runCurrent()

            reads.answer(0)
            runCurrent()

            assertEquals(1, reads.sent.size)
            assertEquals(0, shownRead(profile.state.value))
            assertEquals(0, shownRead(table.state.value))
        }

    @Test
    fun answersAreKeptApartByCacheAndBySurface() =
        runTest {
            val reads = ScriptedReads()
            val a = cache(reads, "a")
            val b = cache(reads, "b")
            val draft = ItineraryResourceEngine(a, itineraryId = "itin-1")
            val other = ItineraryResourceEngine(b, itineraryId = "itin-1")
            val published =
                ItineraryResourceEngine(
                    a,
                    itineraryId = "itin-1",
                    surface = RestSchema.ReadSurface.Published,
                )
            runCurrent()

            repeat(3) { reads.answer(it) }
            runCurrent()

            assertEquals(listOf("a:itin-1:draft", "b:itin-1:draft", "a:itin-1:published"), reads.asked)
            assertEquals(listOf(0, 1, 2), listOf(draft, other, published).map { shownRead(it.state.value) })
        }

    @Test
    fun aFailureIsDroppedAtOnceSoTheNextQuestionAsksAgain() =
        runTest {
            val reads = ScriptedReads()
            val cache = cache(reads)
            val chart = ItineraryResourceEngine(cache, itineraryId = "itin-1")
            runCurrent()
            reads.fail(0)
            runCurrent()

            val table = ItineraryResourceEngine(cache, itineraryId = "itin-1")
            runCurrent()
            reads.answer(1)
            runCurrent()

            assertEquals(2, reads.sent.size)
            assertIs<ItineraryResource.Failed>(chart.state.value)
            assertEquals(1, shownRead(table.state.value))
        }

    @Test
    fun anInvalidationAsksAgainOnBothSurfacesForEveryEngineWatchingOncePerAnswer() =
        runTest {
            val reads = ScriptedReads()
            val cache = cache(reads)
            val draft = ItineraryResourceEngine(cache, itineraryId = "itin-1")
            val twin = ItineraryResourceEngine(cache, itineraryId = "itin-1")
            val published =
                ItineraryResourceEngine(
                    cache,
                    itineraryId = "itin-1",
                    surface = RestSchema.ReadSurface.Published,
                )
            val other = ItineraryResourceEngine(cache, itineraryId = "itin-2")
            runCurrent()
            repeat(3) { reads.answer(it) }
            runCurrent()

            cache.invalidate("itin-1")
            runCurrent()

            assertEquals(listOf("a:itin-1:draft", "a:itin-1:published"), reads.asked.drop(3))
            assertEquals(0, shownRead(draft.state.value), "each shows what it showed until the new answer lands")
            assertEquals(1, shownRead(published.state.value))

            reads.answer(3)
            runCurrent()

            assertEquals(3, shownRead(draft.state.value))
            assertEquals(3, shownRead(twin.state.value))
            assertEquals(1, shownRead(published.state.value))
            assertEquals(2, shownRead(other.state.value))
        }

    @Test
    fun anInvalidationOfNoItineraryDropsEveryAnswerOfItsCacheAndNoOther() =
        runTest {
            val reads = ScriptedReads()
            val a = cache(reads, "a")
            val b = cache(reads, "b")
            val one = ItineraryResourceEngine(a, itineraryId = "itin-1")
            val two = ItineraryResourceEngine(a, itineraryId = "itin-2")
            val elsewhere = ItineraryResourceEngine(b, itineraryId = "itin-1")
            runCurrent()
            repeat(3) { reads.answer(it) }
            runCurrent()

            a.invalidate()
            runCurrent()

            assertEquals(listOf("a:itin-1:draft", "a:itin-2:draft"), reads.asked.drop(3))
            assertEquals(listOf(0, 1, 2), listOf(one, two, elsewhere).map { shownRead(it.state.value) })
        }

    @Test
    fun anEngineAskingAfterAnInvalidationAsksAgainAndNoEngineWatchingIsAskedFor() =
        runTest {
            val reads = ScriptedReads()
            val cache = cache(reads)
            readOnce(cache, "itin-1")
            runCurrent()
            reads.answer(0)
            runCurrent()

            cache.invalidate("itin-1")
            runCurrent()
            assertEquals(1, reads.sent.size, "nothing watches, so nothing is asked")

            readOnce(cache, "itin-1")
            runCurrent()
            assertEquals(listOf("a:itin-1:draft", "a:itin-1:draft"), reads.asked)
        }

    /** The failure of a read an invalidation replaced must not evict the read that replaced it. */
    @Test
    fun aReplacedReadsFailureLeavesTheAnswerThatReplacedItInTheCache() =
        runTest {
            val reads = ScriptedReads()
            val cache = cache(reads)
            val watching = ItineraryResourceEngine(cache, itineraryId = "itin-1")
            runCurrent()

            cache.invalidate("itin-1")
            runCurrent()
            reads.fail(0)
            reads.answer(1)
            runCurrent()

            val late = ItineraryResourceEngine(cache, itineraryId = "itin-1")
            assertEquals(2, reads.sent.size)
            assertEquals(1, shownRead(late.state.value))
            assertEquals(1, shownRead(watching.state.value), "the replaced read's failure never lands")
        }

    /** A failed element is watching still: the app's invalidation is how it is read again. */
    @Test
    fun anInvalidationAsksAgainForAFailedEngineWhichShowsItsFailureUntilItLands() =
        runTest {
            val reads = ScriptedReads()
            val cache = cache(reads)
            val engine = ItineraryResourceEngine(cache, itineraryId = "itin-1")
            runCurrent()
            reads.fail(0)
            runCurrent()

            cache.invalidate()
            runCurrent()
            assertIs<ItineraryResource.Failed>(engine.state.value)

            reads.answer(1)
            runCurrent()
            assertEquals(1, shownRead(engine.state.value))
        }

    @Test
    fun aClosedEngineAndAnIdleOneAreNotAskedForByAnInvalidation() =
        runTest {
            val reads = ScriptedReads()
            val cache = cache(reads)
            val closed = ItineraryResourceEngine(cache, itineraryId = "itin-1")
            val idle = ItineraryResourceEngine(cache)
            runCurrent()
            reads.answer(0)
            runCurrent()
            closed.close()

            cache.invalidate()
            runCurrent()

            assertEquals(1, reads.sent.size)
            assertEquals(ItineraryResource.Idle, idle.state.value)
        }

    @Test
    fun anEngineMovedToAnotherCacheIsAskedForByThatCachesInvalidationsAlone() =
        runTest {
            val reads = ScriptedReads()
            val a = cache(reads, "a")
            val b = cache(reads, "b")
            val engine = ItineraryResourceEngine(a, itineraryId = "itin-1")
            engine.configure(b, "itin-1")
            runCurrent()
            assertEquals(listOf("a:itin-1:draft", "b:itin-1:draft"), reads.asked)

            a.invalidate()
            runCurrent()
            assertEquals(2, reads.sent.size, "the cache it left asks nothing for it")

            b.invalidate()
            runCurrent()
            assertEquals(listOf("b:itin-1:draft"), reads.asked.drop(2))
        }

    /**
     * Nobody can receive the answer of a cache whose scope ended, so the read is cancelled; a source that
     * honours cancellation ends its call then.
     */
    @Test
    fun aCacheWhoseScopeIsCancelledCancelsItsReadsInFlight() =
        runTest {
            val cancelled = CompletableDeferred<Boolean>()
            val scope = CoroutineScope(backgroundScope.coroutineContext + Job())
            val source =
                ItinerarySource { _, _ ->
                    try {
                        awaitCancellation()
                    } finally {
                        cancelled.complete(true)
                    }
                }
            val cache = ItineraryResourceCache(scope, source)
            ItineraryResourceEngine(cache, itineraryId = "itin-1").close()
            runCurrent()

            scope.cancel()
            runCurrent()

            assertTrue(cancelled.await())
        }
}
