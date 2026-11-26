package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Every source here suspends on a deferred the test completes, and every read runs in the test's
 * background scope, so every step of an engine runs on the test's scheduler in order, and `runCurrent`
 * lets a step land without a clock.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ItineraryResourceEngineTest {
    private fun TestScope.cache(
        reads: ScriptedReads,
        client: String = "a",
    ) = ItineraryResourceCache(backgroundScope, reads.source(client))

    private fun state(engine: ItineraryResourceEngine): String = named(engine.state.value)

    private fun distance(engine: ItineraryResourceEngine): Double? =
        (engine.state.value as? ItineraryResource.Resolved)?.itinerary?.characteristics?.distance

    @Test
    fun anEngineWithNoIdIsIdleAndAsksNothing() =
        runTest {
            val reads = ScriptedReads()
            val engine = ItineraryResourceEngine(cache(reads))
            runCurrent()

            assertEquals("idle", state(engine))
            assertTrue(reads.sent.isEmpty())
        }

    /**
     * A binding holds an empty id while it holds no itinerary; asked as an id, it would read as resolving
     * and cost a read for no itinerary.
     */
    @Test
    fun anEmptyIdIsNoIdTheEngineIsIdleAndAsksNothingBeforeAndAfterAnAnswer() =
        runTest {
            val reads = ScriptedReads()
            val cache = cache(reads)
            val engine = ItineraryResourceEngine(cache, itineraryId = "")
            runCurrent()
            assertEquals("idle", state(engine))
            assertTrue(reads.sent.isEmpty())

            engine.configure(cache, "itin-1")
            runCurrent()
            reads.answer(0)
            runCurrent()
            engine.configure(cache, "")

            assertEquals("idle", state(engine))
            assertEquals(listOf("a:itin-1:draft"), reads.asked)
        }

    @Test
    fun anEngineHandedAnIdAsksAtOnceAndIsResolvingWithNothingShownUntilItLands() =
        runTest {
            val reads = ScriptedReads()
            val engine = ItineraryResourceEngine(cache(reads), itineraryId = "itin-1")
            assertEquals("resolving", state(engine), "a question shows nothing as soon as it is asked")
            runCurrent()
            assertEquals(listOf("a:itin-1:draft"), reads.asked)

            reads.answer(0)
            runCurrent()

            val itinerary = assertIs<ItineraryResource.Resolved>(engine.state.value).itinerary
            assertNotNull(itinerary.geometry, "the itinerary holds its geometry")
            assertEquals(1_000.0, itinerary.characteristics.distance)
            assertEquals(listOf("read-0"), itinerary.legs.map { it.id })
            assertTrue(itinerary.waypoints.isEmpty())
        }

    @Test
    fun aFailureShowsNoProjectionOnlyWhatTheSourceThrew() =
        runTest {
            val reads = ScriptedReads()
            val engine = ItineraryResourceEngine(cache(reads), itineraryId = "itin-1")
            runCurrent()

            reads.fail(0)
            runCurrent()

            val failure = assertIs<ItineraryResource.Failed>(engine.state.value).cause
            assertEquals(0, assertIs<ScriptedReads.Failed>(failure).index)
        }

    @Test
    fun thePublishedSurfaceIsAskedWhenNamedAndKeptApartFromTheDraft() =
        runTest {
            val reads = ScriptedReads()
            val cache = cache(reads)
            val draft = ItineraryResourceEngine(cache, itineraryId = "itin-1")
            val published =
                ItineraryResourceEngine(
                    cache,
                    itineraryId = "itin-1",
                    surface = RestSchema.ReadSurface.Published,
                )
            runCurrent()

            reads.answer(1)
            runCurrent()

            assertEquals(listOf("a:itin-1:draft", "a:itin-1:published"), reads.asked)
            assertEquals("resolving", state(draft))
            assertEquals("read-1", state(published))
        }

    /**
     * A profile and a table on one screen are the case the cache is for: a read per element is wrong by
     * exactly one round trip.
     */
    @Test
    fun twoEnginesOverOneItineraryAndOneCacheMakeOneReadAndShowOneAnswer() =
        runTest {
            val reads = ScriptedReads()
            val cache = cache(reads)
            val profile = ItineraryResourceEngine(cache, itineraryId = "itin-1")
            val table = ItineraryResourceEngine(cache, itineraryId = "itin-1")
            runCurrent()

            reads.answer(0)
            runCurrent()

            assertEquals(1, reads.sent.size)
            val first = assertIs<ItineraryResource.Resolved>(profile.state.value)
            val second = assertIs<ItineraryResource.Resolved>(table.state.value)
            assertEquals(first, second)
        }

    /**
     * Two credentials may see two itineraries under one id: a shared answer would draw one reader's
     * itinerary for the other.
     */
    @Test
    fun twoCachesAskingOneIdEachReadTheirOwnAnswer() =
        runTest {
            val reads = ScriptedReads()
            val left = ItineraryResourceEngine(cache(reads, "a"), itineraryId = "itin-1")
            val right = ItineraryResourceEngine(cache(reads, "b"), itineraryId = "itin-1")
            runCurrent()

            reads.answer(0, distance = 1_000.0)
            reads.answer(1, distance = 2_000.0)
            runCurrent()

            assertEquals(listOf("a:itin-1:draft", "b:itin-1:draft"), reads.asked)
            assertEquals(1_000.0, distance(left))
            assertEquals(2_000.0, distance(right))
        }

    @Test
    fun anotherCacheIsAskedAgainAndNothingOfTheOneBeforeIsShownMeanwhile() =
        runTest {
            val reads = ScriptedReads()
            val engine = ItineraryResourceEngine(cache(reads, "a"), itineraryId = "itin-1")
            runCurrent()
            reads.answer(0, distance = 1_000.0)
            runCurrent()

            engine.configure(cache(reads, "b"), "itin-1")
            assertEquals("resolving", state(engine))
            runCurrent()
            reads.answer(1, distance = 2_000.0)
            runCurrent()

            assertEquals(listOf("a:itin-1:draft", "b:itin-1:draft"), reads.asked)
            assertEquals(2_000.0, distance(engine))
        }

    @Test
    fun anAnswerFromACacheNoLongerAskedNeverLands() =
        runTest {
            val reads = ScriptedReads()
            val engine = ItineraryResourceEngine(cache(reads, "a"), itineraryId = "itin-1")
            engine.configure(cache(reads, "b"), "itin-1")
            runCurrent()

            reads.answer(0)
            runCurrent()
            assertEquals("resolving", state(engine))

            reads.answer(1)
            runCurrent()
            assertEquals("read-1", state(engine))
        }

    @Test
    fun anAnswerOrAFailureForAnIdNoLongerAskedNeverLands() =
        runTest {
            val reads = ScriptedReads()
            val cache = cache(reads)
            val engine = ItineraryResourceEngine(cache, itineraryId = "itin-a")
            runCurrent()
            engine.configure(cache, "itin-b")
            runCurrent()

            reads.answer(1)
            runCurrent()
            reads.answer(0)
            runCurrent()
            assertEquals("read-1", state(engine))

            engine.configure(cache, "itin-c")
            runCurrent()
            engine.configure(cache, "itin-b")
            reads.fail(2)
            runCurrent()
            assertEquals("read-1", state(engine), "the answer held is shown again, and the failure is dropped")
            assertEquals(listOf("a:itin-a:draft", "a:itin-b:draft", "a:itin-c:draft"), reads.asked)
        }

    /**
     * Between a question changing and its answer landing, the itinerary before is someone else's under
     * this id, and showing it would call it current.
     */
    @Test
    fun aNewIdResolvesWithNothingOfTheOneBeforeIt() =
        runTest {
            val reads = ScriptedReads()
            val cache = cache(reads)
            val engine = ItineraryResourceEngine(cache, itineraryId = "itin-a")
            runCurrent()
            reads.answer(0)
            runCurrent()

            engine.configure(cache, "itin-b")

            assertEquals("resolving", state(engine))
        }

    @Test
    fun droppingTheIdReturnsToIdle() =
        runTest {
            val reads = ScriptedReads()
            val cache = cache(reads)
            val engine = ItineraryResourceEngine(cache, itineraryId = "itin-1")
            runCurrent()
            reads.answer(0)
            runCurrent()

            engine.configure(cache, null)

            assertEquals("idle", state(engine))
        }

    @Test
    fun aQuestionWhoseAnswerTheCacheHoldsIsShownFromItAtOnceWithNoRead() =
        runTest {
            val reads = ScriptedReads()
            val cache = cache(reads)
            val engine = ItineraryResourceEngine(cache, itineraryId = "itin-a")
            runCurrent()
            reads.answer(0)
            runCurrent()
            engine.configure(cache, "itin-b")
            runCurrent()

            engine.configure(cache, "itin-a")

            assertEquals("read-0", state(engine), "shown before the command returns")
            runCurrent()
            assertEquals(listOf("a:itin-a:draft", "a:itin-b:draft"), reads.asked)
        }

    @Test
    fun theSameQuestionAskedAgainAsksNothingAndAFailureIsNotAskedAgainByIt() =
        runTest {
            val reads = ScriptedReads()
            val cache = cache(reads)
            val engine = ItineraryResourceEngine(cache, itineraryId = "itin-1")
            runCurrent()
            repeat(3) { engine.configure(cache, "itin-1") }
            reads.fail(0)
            runCurrent()
            repeat(3) { engine.configure(cache, "itin-1") }
            runCurrent()

            assertEquals(1, reads.sent.size)
            assertEquals("failed", state(engine))
        }

    /**
     * A failed read is evicted at once: replaying a network blip for the life of the screen would keep
     * every later element failed.
     */
    @Test
    fun aFailureIsNotKeptSoTheNextQuestionAsksAgain() =
        runTest {
            val reads = ScriptedReads()
            val cache = cache(reads)
            val failing = ItineraryResourceEngine(cache, itineraryId = "itin-1")
            runCurrent()
            reads.fail(0)
            runCurrent()

            val retry = ItineraryResourceEngine(cache, itineraryId = "itin-1")
            runCurrent()
            reads.answer(1)
            runCurrent()

            assertEquals("failed", state(failing))
            assertEquals("read-1", state(retry))
            assertEquals(2, reads.sent.size)
        }

    /**
     * A `StateFlow` holds the state now and each that differs from the one before it; a collector that
     * runs as each is set is told every one, in order. It never ends: a close leaves it as it stands.
     */
    @Test
    fun aCollectorIsToldTheStateNowThenEveryChangeInOrder() =
        runTest {
            val reads = ScriptedReads()
            val cache = cache(reads)
            val engine = ItineraryResourceEngine(cache)
            val told = mutableListOf<String>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                engine.state.collect { told += named(it) }
            }

            engine.configure(cache, "itin-1")
            engine.configure(cache, "itin-1")
            runCurrent()
            reads.answer(0)
            runCurrent()
            engine.configure(cache, null)
            engine.close()
            runCurrent()

            assertEquals(listOf("idle", "resolving", "read-0", "idle"), told)
        }

    @Test
    fun aClosedEngineIsInertNoAnswerLandsAndNoQuestionIsAsked() =
        runTest {
            val reads = ScriptedReads()
            val cache = cache(reads)
            val engine = ItineraryResourceEngine(cache, itineraryId = "itin-1")
            runCurrent()

            engine.close()
            engine.close()
            reads.answer(0)
            engine.configure(cache, "itin-2")
            runCurrent()
            cache.invalidate()
            runCurrent()

            assertEquals("resolving", state(engine))
            assertEquals(1, reads.sent.size)
        }

    /**
     * The answer an engine stops waiting for is the cache's still: it is shared, so it is never cancelled,
     * and an element asking next is shown it.
     */
    @Test
    fun anAnswerThatLandsAfterItsEngineClosedIsKeptForTheNextEngine() =
        runTest {
            val reads = ScriptedReads()
            val cache = cache(reads)
            val first = ItineraryResourceEngine(cache, itineraryId = "itin-1")
            runCurrent()
            first.close()
            reads.answer(0)
            runCurrent()

            val next = ItineraryResourceEngine(cache, itineraryId = "itin-1")

            assertEquals("read-0", state(next))
            assertEquals(1, reads.sent.size)
        }

    /**
     * On a dispatcher that runs a read at once, as `Dispatchers.Main.immediate` does on its own thread and so
     * `viewModelScope`, a source that answers without suspending lands before the cache's read returns: the
     * engine shows that answer, a failure, and the answer an invalidation asks for again, each at once.
     */
    @Test
    fun anAnswerThatLandsWhileTheEngineAsksIsShownAsAFailureIs() =
        runTest {
            val sent = mutableListOf<String>()
            val cache =
                ItineraryResourceCache(backgroundScope + UnconfinedTestDispatcher(testScheduler)) { itineraryId, _ ->
                    val index = sent.size
                    sent += itineraryId
                    if (itineraryId == "itin-missing") throw ScriptedReads.Failed(index)
                    val line = lineString(listOf(6.0, 45.0), listOf(6.01, 45.0))
                    restItinerary(itineraryId, segments = listOf(restSegment("read-$index", 0, line, 1_000.0)))
                }

            val engine = ItineraryResourceEngine(cache, itineraryId = "itin-1")
            assertEquals("read-0", state(engine))

            cache.invalidate("itin-1")
            assertEquals("read-1", state(engine))

            engine.configure(cache, "itin-missing")
            assertEquals("failed", state(engine))
            assertEquals(listOf("itin-1", "itin-1", "itin-missing"), sent)
        }

    /** Every engine is closeable as a resource, so `use` stops it, and its cache asks nothing more for it. */
    @Test
    fun theEngineIsClosedAsAResource() =
        runTest {
            val reads = ScriptedReads()
            val cache = cache(reads)
            val engine = ItineraryResourceEngine(cache, itineraryId = "itin-1")

            engine.use { runCurrent() }
            cache.invalidate("itin-1")
            runCurrent()

            assertEquals(listOf("itin-1"), reads.sent.map { it.itineraryId })
        }
}
