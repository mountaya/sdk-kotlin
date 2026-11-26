package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Every pause, round and request runs in the test's background scope, so `runCurrent` lets each land in the order
 * sent, and the test's virtual time, moved by `advanceTimeBy`, is the clock the pauses are waited out on.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchEngineTest {
    /** The two branches most cases ask, itineraries then zones. */
    private val pair = listOf(DataSchema.SearchTarget.Itineraries, DataSchema.SearchTarget.Zones)

    /** A camera at latitude 0, where every edge of a box is exact. */
    private val camera = MapViewport(listOf(6.5, 0.0), 10.0)

    /** The box a round confined to [camera] sends. */
    private val box = listOf(6.32421875, -0.17578125, 6.67578125, 0.17578125)

    private val places = listOf(DataSchema.SearchTarget.Places)

    private fun TestScope.engine(
        held: HeldSearch,
        query: String = "",
        targets: List<DataSchema.SearchTarget> = pair,
        camera: MapViewport? = null,
        isOpen: Boolean = false,
        isConfined: Boolean = false,
        pause: Duration = Duration.ZERO,
        language: DataSchema.Language? = null,
        limit: SearchLimit? = null,
        onError: ((Throwable) -> Unit)? = null,
    ): SearchEngine =
        SearchEngine(
            backgroundScope,
            SearchConfiguration(held.fetch, language, limit, targets, onError = onError),
            query,
            camera,
            isOpen,
            isConfined,
            assertNotNull(SearchPause.of(pause), "a test's pause is zero or more"),
        )

    /** Moves the test's time on by [ms] and lets every coroutine due land. */
    private fun TestScope.wait(ms: Long) {
        advanceTimeBy(ms.milliseconds)
        runCurrent()
    }

    /** This configuration with the members given changed, every other kept. */
    private fun SearchConfiguration.with(
        fetch: SearchFetch? = this.fetch,
        language: DataSchema.Language? = this.language,
        limit: SearchLimit? = this.limit,
        targets: List<DataSchema.SearchTarget> = this.targets,
        wording: SearchWording = this.wording,
        onError: ((Throwable) -> Unit)? = this.onError,
    ): SearchConfiguration = SearchConfiguration(fetch, language, limit, targets, wording, onError)

    @Test
    fun aSearchHoldingNoQueryAsksNothingAndShowsEverySectionSettledWithNothing() =
        runTest {
            val held = HeldSearch()
            val engine = engine(held)
            runCurrent()

            assertTrue(held.sent.isEmpty())
            assertEquals("|idle|-|-|itineraries:s,zones:s", shown(engine.state.value))
            engine.close()
        }

    @Test
    fun aQueryTheSearchStartsWithIsAskedAtOnceOneRequestPerBranchInTheirOrder() =
        runTest {
            val held = HeldSearch()
            val engine = engine(held, query = "mont", targets = searchSectionOrder)
            assertTrue(engine.state.value.isSearching, "the skeleton before any request leaves")
            runCurrent()

            assertEquals(searchSectionOrder, held.branches)
            for ((index, input) in held.sent.withIndex()) {
                assertEquals("mont", input.query)
                assertTrue(input.focus == null && input.bbox == null && input.limit == null)
                assertNull(input.language)
                val flags =
                    with(input.targets) { listOf(itineraries, places, ways, pois, zones, collections) }
                assertEquals((0 until 6).map { it == index }, flags.map { it == true }, "one branch alone")
                assertTrue(flags.all { it != null }, "every flag stated")
            }
            assertEquals(
                "mont|searching|-|-|itineraries:l,places:l,ways:l,pois:l,zones:l,collections:l",
                shown(engine.state.value),
            )
            held.release()
            engine.close()
        }

    @Test
    fun answersLandingInAnyOrderFillTheirSectionsInTheOrderTheTargetsListThem() =
        runTest {
            val held = HeldSearch()
            val engine =
                engine(
                    held,
                    query = "mont",
                    targets = listOf(DataSchema.SearchTarget.Zones, DataSchema.SearchTarget.Places),
                )
            runCurrent()

            held.answer(1, listOf("Mont Blanc"))
            runCurrent()
            assertEquals("mont|searching|-|-|zones:l,places:s[Mont Blanc]", shown(engine.state.value))
            held.answer(0, listOf("Couloir du Mont"))
            runCurrent()
            assertEquals("mont|idle|settled|-|zones:s[Couloir du Mont],places:s[Mont Blanc]", shown(engine.state.value))
            engine.close()
        }

    @Test
    fun aBranchListedTwiceIsAskedOnceAndOneThisBuildCannotEnableIsAskedNone() =
        runTest {
            val held = HeldSearch()
            val engine =
                engine(
                    held,
                    query = "mont",
                    targets =
                        listOf(
                            DataSchema.SearchTarget.Zones,
                            DataSchema.SearchTarget.Places,
                            DataSchema.SearchTarget.Zones,
                            DataSchema.SearchTarget("TRAILS"),
                        ),
                )
            runCurrent()

            assertEquals(listOf(DataSchema.SearchTarget.Zones, DataSchema.SearchTarget.Places), held.branches)
            assertEquals(
                listOf(DataSchema.SearchTarget.Zones, DataSchema.SearchTarget.Places),
                engine.state.value.sections
                    .map { it.id },
            )
            held.release()
            engine.close()
        }

    @Test
    fun aRoundAsksInItsLanguageAndUnderItsCapRankedAndConfinedAroundTheCamera() =
        runTest {
            val held = HeldSearch()
            val cap = assertNotNull(SearchLimit.of(5), "a cap of 5 is a cap")
            val engine =
                engine(
                    held,
                    query = " chamonix ",
                    targets = places,
                    camera = camera,
                    isConfined = true,
                    language = DataSchema.Language.Fr,
                    limit = cap,
                )
            runCurrent()

            val input = held.sent.first()
            assertEquals("chamonix", input.query, "the query trimmed")
            assertEquals(DataSchema.Language.Fr, input.language)
            assertEquals(5, input.limit)
            assertEquals(listOf(6.5, 0.0), input.focus)
            assertEquals(box, input.bbox)
            held.release()
            engine.close()
        }

    @Test
    fun aCameraThatNamesNoPointRanksAroundNothingAndOneOfNoZoomConfinesNothing() =
        runTest {
            val held = HeldSearch()
            val noZoom =
                engine(
                    held,
                    query = "a",
                    targets = places,
                    camera = MapViewport(listOf(197.5, 21.3), Double.NaN),
                    isConfined = true,
                )
            runCurrent()
            val noPoint =
                engine(
                    held,
                    query = "b",
                    targets = places,
                    camera = MapViewport(listOf(Double.NaN, 45.0), 12.0),
                    isConfined = true,
                )
            runCurrent()
            val none = engine(held, query = "c", targets = places, isConfined = true)
            runCurrent()

            assertEquals(listOf("a", "b", "c"), held.sent.map { it.query })
            assertEquals(listOf(-162.5, 21.3), held.sent[0].focus)
            assertNull(held.sent[0].bbox)
            assertTrue(held.sent[1].focus == null && held.sent[1].bbox == null)
            assertTrue(held.sent[2].focus == null && held.sent[2].bbox == null)
            held.release()
            for (engine in listOf(noZoom, noPoint, none)) engine.close()
        }

    @Test
    fun aRoundIsAskedOnceTheQueryHasGoneUnchangedForThePauseItsSkeletonMeanwhile() =
        runTest {
            val held = HeldSearch()
            val engine = engine(held, targets = places, pause = 150.milliseconds)

            engine.setQuery("c")
            assertEquals("c|searching|-|-|places:l", shown(engine.state.value))
            wait(100)
            engine.setQuery("ch")
            wait(100)
            engine.setQuery("cha")
            wait(149)
            assertTrue(held.sent.isEmpty())
            assertEquals("cha|searching|-|-|places:l", shown(engine.state.value))
            wait(1)

            assertEquals(listOf("cha"), held.sent.map { it.query })
            held.answer(0, listOf("Chamonix"))
            runCurrent()
            assertEquals("cha|idle|settled|-|places:s[Chamonix]", shown(engine.state.value))
            engine.close()
        }

    @Test
    fun aPauseOfZeroAsksAsTheQueryIsTypedAbandoningTheRoundInFlight() =
        runTest {
            val held = HeldSearch()
            val engine = engine(held, targets = places, pause = Duration.ZERO)

            engine.setQuery("c")
            runCurrent()
            engine.setQuery("ch")
            runCurrent()

            assertEquals(listOf("c", "ch"), held.sent.map { it.query })
            assertEquals(setOf(0), held.cancelled)
            held.answer(1, listOf("Chamonix"))
            runCurrent()
            assertEquals("ch|idle|settled|-|places:s[Chamonix]", shown(engine.state.value))
            engine.close()
        }

    /**
     * A search handed no pause waits the contract's own, `SearchPause.standard`, 150 ms; a pause below zero is no
     * `SearchPause` at all, so no engine waits one (`geo.search.typing-pause`, `SearchPauseTest`).
     */
    @Test
    fun aSearchHandedNoPauseWaitsTheStandardOne() =
        runTest {
            val held = HeldSearch()
            val engine = SearchEngine(backgroundScope, SearchConfiguration(held.fetch, targets = places))

            engine.setQuery("cha")
            wait(149)
            assertTrue(held.sent.isEmpty())
            wait(1)
            assertEquals(listOf("cha"), held.sent.map { it.query })
            engine.close()
        }

    @Test
    fun whiteSpaceTypedAroundTheSettledQueryAsksNothingNorDoesABlankQuery() =
        runTest {
            val held = HeldSearch()
            val engine = engine(held, query = "chamonix", targets = places, pause = 150.milliseconds)
            runCurrent()
            held.answer(0, listOf("Chamonix"))
            runCurrent()

            engine.setQuery("chamonix ")
            assertEquals("chamonix|idle|settled|-|places:s[Chamonix]", shown(engine.state.value))
            wait(150)
            assertEquals(1, held.sent.size)

            val blank = engine(held, targets = places, pause = 150.milliseconds)
            blank.setQuery("   ")
            assertEquals("|idle|-|-|places:s", shown(blank.state.value), "a blank query shows nothing loading")
            wait(150)
            assertEquals(1, held.sent.size)
            engine.close()
            blank.close()
        }

    @Test
    fun rowsAnsweringAnotherQueryAreNeverShownUnderTheOneTyped() =
        runTest {
            val held = HeldSearch()
            val engine = engine(held, query = "chamonix", targets = places, pause = 150.milliseconds)
            runCurrent()

            engine.setQuery("zermatt")
            held.answer(0, listOf("Chamonix"))
            runCurrent()
            assertEquals("zermatt|searching|-|-|places:l", shown(engine.state.value), "a round landing in the pause")
            wait(150)
            assertEquals(listOf("chamonix", "zermatt"), held.sent.map { it.query })
            assertTrue(held.cancelled.isEmpty(), "the round in flight is abandoned once the pause ends")
            held.answer(1, listOf("Zermatt"))
            runCurrent()
            assertEquals("zermatt|idle|settled|-|places:s[Zermatt]", shown(engine.state.value))
            engine.close()
        }

    @Test
    fun onlyTheLatestRoundWritesARoundAbandonedLandsNothingAnsweredOrFailed() =
        runTest {
            val held = HeldSearch()
            val engine = engine(held, query = "chamonix")
            runCurrent()

            engine.setQuery("zermatt")
            runCurrent()
            assertEquals(setOf(0, 1), held.cancelled)
            held.answer(0, listOf("Lac Blanc"))
            held.fail(1)
            runCurrent()
            assertEquals("zermatt|searching|-|-|itineraries:l,zones:l", shown(engine.state.value))
            held.answer(2, listOf("Gornergrat"))
            held.answer(3, emptyList())
            runCurrent()
            assertEquals("zermatt|idle|settled|-|itineraries:s[Gornergrat],zones:s", shown(engine.state.value))
            engine.close()
        }

    @Test
    fun aFailedBranchFillsItsOwnSectionAsDoesOneTheDataApiReportsFailed() =
        runTest {
            val held = HeldSearch()
            val heard = mutableListOf<Throwable>()
            val engine = engine(held, query = "chamonix", onError = { heard += it })
            runCurrent()

            held.fail(0)
            runCurrent()
            assertEquals("chamonix|searching|-|-|itineraries:e,zones:l", shown(engine.state.value))
            held.report(1)
            runCurrent()

            assertEquals("chamonix|idle|settled|-|itineraries:e,zones:e", shown(engine.state.value))
            val sections = engine.state.value.sections
            assertIs<HeldSearch.Failed>(sections[0].state.failure)
            assertEquals(
                SearchBranchException(DataSchema.SearchTarget.Zones, DataSchema.ErrorCode.BadGateway, "unreachable"),
                sections[1].state.failure,
            )
            assertEquals(1, heard.size, "the thrown failure is handed on, the report is not")
            assertIs<HeldSearch.Failed>(heard.first())
            engine.close()
        }

    @Test
    fun aChangeOfWhatARoundAsksStartsAFreshRoundAndOneOfNothingElseAsksNothing() =
        runTest {
            val held = HeldSearch()
            val engine = engine(held, query = "chamonix")
            runCurrent()
            assertEquals(2, held.sent.size)
            var configuration = engine.configuration

            fun configured(change: (SearchConfiguration) -> SearchConfiguration): Int {
                configuration = change(configuration)
                engine.configure(configuration)
                runCurrent()
                return held.sent.size
            }

            assertEquals(
                2,
                configured {
                    it.with(
                        fetch = SearchFetch { input -> held.fetch.search(input) },
                        wording = SearchWording(resolveLabel = { "Mot" }),
                        onError = {},
                    )
                },
                "a replaced transport, word resolver and handler ask nothing",
            )
            assertEquals(
                4,
                configured { it.with(wording = SearchWording(UnitPreferences("en"))) },
                "units stated where none were",
            )
            assertEquals(
                4,
                configured {
                    it.with(
                        wording =
                            SearchWording(
                                UnitPreferences(
                                    "en",
                                    pace = RestSchema.UnitPace.PerDistance,
                                    slope = RestSchema.UnitSlope.Percentage,
                                ),
                            ),
                    )
                },
                "a unit no row reads",
            )
            assertEquals(
                6,
                configured {
                    it.with(wording = SearchWording(UnitPreferences("en", RestSchema.UnitLength.Imperial)))
                },
                "the length system",
            )
            assertEquals(
                8,
                configured {
                    it.with(wording = SearchWording(UnitPreferences("fr", RestSchema.UnitLength.Imperial)))
                },
                "the units' language",
            )
            assertEquals(10, configured { it.with(language = DataSchema.Language.Fr) })
            val cap = assertNotNull(SearchLimit.of(5), "a cap of 5 is a cap")
            assertEquals(12, configured { it.with(limit = cap) })
            assertEquals(13, configured { it.with(targets = listOf(DataSchema.SearchTarget.Zones)) })
            assertTrue(held.sent.last().language == DataSchema.Language.Fr && held.sent.last().limit == 5)
            assertEquals(DataSchema.SearchTarget.Zones, held.branches.last())
            assertEquals("chamonix|searching|-|-|zones:l", shown(engine.state.value))
            assertEquals((0 until 12).toSet(), held.cancelled, "each round abandoned the one in flight")

            configuration = configuration.with(fetch = null)
            engine.configure(configuration)
            assertEquals("chamonix|idle|-|-|zones:s", shown(engine.state.value), "no transport asks nothing")
            runCurrent()
            assertTrue(12 in held.cancelled)
            engine.close()
        }

    @Test
    fun roundsAbandonedBeforeTheirCoroutineRanSendNothing() =
        runTest {
            val held = HeldSearch()
            val engine = engine(held, query = "a")
            engine.setQuery("ab")
            engine.setQuery("abc")
            runCurrent()

            assertEquals(listOf("abc", "abc"), held.sent.map { it.query })
            assertTrue(held.cancelled.isEmpty())
            held.release()
            engine.close()
        }

    @Test
    fun aRetryAsksAFreshRoundEverySectionLoadingAgain() =
        runTest {
            val held = HeldSearch()
            val engine = engine(held, query = "chamonix")
            runCurrent()
            held.fail(0)
            held.answer(1, listOf("Couloir Nord"))
            runCurrent()

            engine.retry()
            assertEquals("chamonix|searching|-|-|itineraries:l,zones:l", shown(engine.state.value))
            runCurrent()
            assertEquals(4, held.sent.size)
            held.answer(2, listOf("Lac Blanc"))
            held.answer(3, listOf("Couloir Nord"))
            runCurrent()
            assertEquals(
                "chamonix|idle|settled|-|itineraries:s[Lac Blanc],zones:s[Couloir Nord]",
                shown(engine.state.value),
            )
            engine.close()
        }

    @Test
    fun aConfinedRoundThatFoundNothingAsksOnceMoreEverywhereAndSaysItFellBack() =
        runTest {
            val held = HeldSearch()
            val engine = engine(held, query = "chamonix", camera = camera, isOpen = true, isConfined = true)
            runCurrent()

            held.answer(0, emptyList())
            runCurrent()
            assertEquals("chamonix|searching|-|-|itineraries:s,zones:l", shown(engine.state.value))
            held.answer(1, emptyList())
            runCurrent()
            assertEquals(4, held.sent.size)
            assertTrue(held.sent[2].bbox == null && held.sent[3].bbox == null, "everywhere")
            assertEquals(listOf(6.5, 0.0), held.sent[2].focus, "still ranked around the camera")
            assertEquals(
                "chamonix|searching|-|fell-back|itineraries:l,zones:l",
                shown(engine.state.value),
                "no round settled while the broadened one is out",
            )
            held.answer(2, emptyList())
            held.answer(3, emptyList())
            runCurrent()
            assertEquals("chamonix|idle|settled|fell-back|itineraries:s,zones:s", shown(engine.state.value))
            assertEquals(4, held.sent.size, "never twice")

            engine.setQuery("zermatt")
            runCurrent()
            assertEquals(box, held.sent[4].bbox, "a new query is confined again")
            assertFalse(engine.state.value.hasFallenBack)
            held.release()
            engine.close()
        }

    @Test
    fun aFailedOrReportedBranchIsNoEmptyOneAndAnUnconfinedRoundNeverFallsBack() =
        runTest {
            val failing = HeldSearch()
            val failed = engine(failing, query = "chamonix", camera = camera, isOpen = true, isConfined = true)
            val reporting = HeldSearch()
            val reported = engine(reporting, query = "chamonix", camera = camera, isOpen = true, isConfined = true)
            val unconfining = HeldSearch()
            val unconfined = engine(unconfining, query = "chamonix", camera = camera)
            val unread = HeldSearch()
            val noCamera = engine(unread, query = "chamonix", isOpen = true, isConfined = true)
            runCurrent()

            failing.fail(0)
            failing.answer(1, emptyList())
            reporting.answer(0, emptyList())
            reporting.report(1)
            unconfining.answer(0, emptyList())
            unconfining.answer(1, emptyList())
            unread.answer(0, emptyList())
            unread.answer(1, emptyList())
            runCurrent()

            for (held in listOf(failing, reporting, unconfining, unread)) assertEquals(2, held.sent.size)
            assertEquals("chamonix|idle|settled|-|itineraries:s,zones:s", shown(unconfined.state.value))
            assertTrue(unread.sent.all { it.bbox == null }, "a camera with nothing to read confines nothing")
            for (engine in listOf(failed, reported, unconfined, noCamera)) engine.close()
        }

    @Test
    fun aRestOfTheCameraAsksTheRoundAgainAfterThePauseItsRowsKeptInPlace() =
        runTest {
            val held = HeldSearch()
            val engine = engine(held, query = "chamonix", camera = camera, isOpen = true, isConfined = true)
            runCurrent()
            held.answer(0, listOf("Lac Blanc"))
            held.answer(1, emptyList())
            runCurrent()

            engine.setCamera(MapViewport(listOf(7.0, 0.0), 10.0))
            wait(349)
            assertEquals(2, held.sent.size)
            wait(1)
            assertEquals(4, held.sent.size)
            assertEquals(listOf(7.0, 0.0), held.sent[2].focus)
            assertEquals(listOf(6.82421875, -0.17578125, 7.17578125, 0.17578125), held.sent[2].bbox)
            assertEquals(
                "chamonix|searching|-|-|itineraries:s[Lac Blanc],zones:s",
                shown(engine.state.value),
                "no section loads in a refresh",
            )
            held.answer(2, emptyList())
            runCurrent()
            assertEquals(
                "chamonix|searching|-|-|itineraries:s,zones:s",
                shown(engine.state.value),
                "busy for the length of the round",
            )
            held.answer(3, emptyList())
            runCurrent()
            assertEquals("chamonix|idle|settled|-|itineraries:s,zones:s", shown(engine.state.value))
            assertEquals(4, held.sent.size, "a refresh never falls back")
            engine.close()
        }

    @Test
    fun theMovesOfOneGestureFoldIntoOneRoundAskedFromTheLast() =
        runTest {
            val held = HeldSearch()
            val engine = engine(held, query = "chamonix", camera = camera, isOpen = true, isConfined = true)
            runCurrent()
            held.answer(0, listOf("Lac Blanc"))
            held.answer(1, emptyList())
            runCurrent()

            for (longitude in listOf(6.75, 7.0, 7.25, 7.5, 7.75)) {
                engine.setCamera(MapViewport(listOf(longitude, 0.0), 10.0))
                wait(16)
            }
            assertEquals(2, held.sent.size)
            wait(334)
            assertEquals(4, held.sent.size)
            assertEquals(listOf(7.75, 0.0), held.sent[2].focus)
            held.release()
            engine.close()
        }

    @Test
    fun aRestAsksNothingWhileTheListIsClosedUnconfinedOrHoldingNoQuery() =
        runTest {
            val closedHeld = HeldSearch()
            val closed = engine(closedHeld, query = "chamonix", camera = camera, isConfined = true)
            val unconfinedHeld = HeldSearch()
            val unconfined = engine(unconfinedHeld, query = "chamonix", camera = camera, isOpen = true)
            val emptyHeld = HeldSearch()
            val empty = engine(emptyHeld, camera = camera, isOpen = true, isConfined = true)
            val closingHeld = HeldSearch()
            val closing = engine(closingHeld, query = "chamonix", camera = camera, isOpen = true, isConfined = true)
            runCurrent()

            for (engine in listOf(closed, unconfined, empty, closing)) {
                engine.setCamera(MapViewport(listOf(7.0, 0.0), 10.0))
            }
            wait(100)
            closing.setOpen(false)
            wait(400)

            assertEquals(2, closedHeld.sent.size)
            assertEquals(2, unconfinedHeld.sent.size)
            assertTrue(emptyHeld.sent.isEmpty())
            assertEquals(2, closingHeld.sent.size, "a closing drops the rest waiting")
            assertEquals(MapViewport(listOf(7.0, 0.0), 10.0), closed.camera, "the camera is held all the same")

            closed.setOpen(true)
            closed.setCamera(MapViewport(listOf(7.0, 0.0), 10.0))
            wait(350)
            assertEquals(4, closedHeld.sent.size, "opened again, a rest asks")
            for (held in listOf(closedHeld, unconfinedHeld, emptyHeld, closingHeld)) held.release()
            for (engine in listOf(closed, unconfined, empty, closing)) engine.close()
        }

    @Test
    fun aRestAfterAFallBackAsksTheVisibleAreaAgainAndNoLongerSaysItFellBack() =
        runTest {
            val held = HeldSearch()
            val engine = engine(held, query = "chamonix", camera = camera, isOpen = true, isConfined = true)
            runCurrent()
            held.answer(0, emptyList())
            held.answer(1, emptyList())
            runCurrent()
            held.answer(2, listOf("Lac Blanc"))
            held.answer(3, emptyList())
            runCurrent()
            assertTrue(engine.state.value.hasFallenBack)

            engine.setCamera(MapViewport(listOf(7.0, 0.0), 10.0))
            wait(350)
            assertEquals(6, held.sent.size)
            assertNotNull(held.sent[4].bbox, "the round asks within a box")
            assertEquals("chamonix|searching|-|-|itineraries:s[Lac Blanc],zones:s", shown(engine.state.value))
            held.release()
            engine.close()
        }

    @Test
    fun aChangeOfConfinementIsAFreshRoundEverySectionLoadingAgain() =
        runTest {
            val held = HeldSearch()
            val engine = engine(held, query = "chamonix", camera = camera)
            runCurrent()
            held.answer(0, listOf("Lac Blanc"))
            held.answer(1, emptyList())
            runCurrent()

            engine.setConfined(true)
            assertEquals("chamonix|searching|-|-|itineraries:l,zones:l", shown(engine.state.value))
            runCurrent()
            assertEquals(box, held.sent[2].bbox)
            engine.setConfined(true)
            runCurrent()
            assertEquals(4, held.sent.size, "the same confinement asks nothing")
            held.release()
            engine.close()
        }

    @Test
    fun aCollectorIsToldTheStateNowAndEachChangeAndNothingAfterClose() =
        runTest {
            val held = HeldSearch()
            val engine = engine(held, targets = places)
            val told = mutableListOf<String>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                engine.state.collect { told += shown(it) }
            }

            engine.setQuery("chamonix")
            runCurrent()
            held.answer(0, listOf("Chamonix"))
            runCurrent()
            engine.close()
            val atClose = told.size
            engine.setQuery("zermatt")
            engine.retry()
            runCurrent()

            assertEquals("|idle|-|-|places:s", told.first())
            assertEquals("chamonix|idle|settled|-|places:s[Chamonix]", told.last())
            assertTrue("chamonix|searching|-|-|places:l" in told)
            assertEquals(atClose, told.size, "nothing told after close")
            assertEquals(
                "chamonix|idle|settled|-|places:s[Chamonix]",
                shown(engine.state.value),
                "a collector after close told the last state",
            )
        }

    @Test
    fun aClosedEngineIsInertItsRoundAbandonedNothingLandingNoCommandHeard() =
        runTest {
            val held = HeldSearch()
            val engine = engine(held, query = "chamonix", camera = camera, isOpen = true, isConfined = true)
            runCurrent()

            engine.close()
            runCurrent()
            assertEquals(setOf(0, 1), held.cancelled)
            held.answer(0, listOf("Lac Blanc"))
            engine.setQuery("zermatt")
            engine.retry()
            engine.setConfined(false)
            engine.setCamera(MapViewport(listOf(7.0, 0.0), 10.0))
            wait(400)
            assertEquals(2, held.sent.size)
            assertEquals("chamonix|searching|-|-|itineraries:l,zones:l", shown(engine.state.value))
            assertEquals("chamonix", engine.query)
            held.release()
        }

    @Test
    fun aSearchWithNoTransportAsksNothingTheTypedQueryShownSettledWithNothing() =
        runTest {
            val engine =
                SearchEngine(backgroundScope, SearchConfiguration(targets = places), pause = SearchPause.standard)

            engine.setQuery("chamonix")
            assertEquals("chamonix|idle|-|-|places:s", shown(engine.state.value))
            wait(150)
            assertEquals("chamonix|idle|-|-|places:s", shown(engine.state.value))
            engine.close()
        }

    /** Every engine is closeable as a resource, so `use` stops it: a query typed after asks nothing. */
    @Test
    fun theEngineIsClosedAsAResource() =
        runTest {
            val held = HeldSearch()
            val engine = engine(held)

            engine.use { it.setQuery("chamonix") }
            engine.setQuery("zermatt")
            runCurrent()

            assertTrue(held.sent.isEmpty())
            assertEquals("chamonix", engine.query)
        }
}
