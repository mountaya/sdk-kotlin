package com.mountaya.geo

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * Every read and command runs in the test's background scope and suspends on a deferred the test completes, so
 * `runCurrent` lets each one land, in the order sent, without a clock.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LayersSelectionEngineTest {
    private val catalogue =
        TileFixture.layers(
            listOf(
                TileFixture.category(
                    "conditions",
                    listOf(TileFixture.overlay("slopes"), TileFixture.overlay("snow-depth")),
                ),
                TileFixture.category(
                    "enrichment",
                    listOf(TileFixture.overlay("hillshade"), TileFixture.overlay("pois")),
                ),
            ),
            listOf(TileFixture.terrain("outdoors"), TileFixture.terrain("satellite")),
        )

    /** A selection as `terrain|overlay|enrichments|refused`. */
    private fun shown(selection: LayersSelection): String =
        listOf(
            selection.terrain ?: "-",
            selection.overlay ?: "-",
            selection.enrichments.joinToString("+"),
            if (selection.refusal == null) "kept" else "refused",
        ).joinToString("|")

    @Test
    fun aClassicOverlayReplacesTheOneBeforeASecondPressClearsItAndNullClearsIt() =
        runTest {
            val map = HeldLayersMap(catalogue, "outdoors", answersAtOnce = true)
            val engine = LayersSelectionEngine(backgroundScope, map)
            runCurrent()
            map.land(0)
            runCurrent()

            engine.setOverlay("slopes")
            engine.setOverlay("snow-depth")
            assertEquals("outdoors|snow-depth||kept", shown(engine.state.value))
            engine.setOverlay("snow-depth")
            assertEquals("outdoors|-||kept", shown(engine.state.value))
            engine.setOverlay("slopes")
            engine.setOverlay(null)
            runCurrent()

            assertEquals("outdoors|-||kept", shown(engine.state.value))
            assertEquals(
                listOf("overlay:slopes", "overlay:snow-depth", "overlay:-", "overlay:slopes", "overlay:-"),
                map.calls,
            )
            engine.close()
        }

    @Test
    fun enrichmentsStackInTheOrderTurnedOnOneTurnedOnAgainGoingLast() =
        runTest {
            val map = HeldLayersMap(catalogue, "outdoors", answersAtOnce = true)
            val engine = LayersSelectionEngine(backgroundScope, map)

            engine.toggleEnrichment("hillshade")
            engine.toggleEnrichment("pois")
            engine.toggleEnrichment("hillshade")
            assertEquals(listOf("pois"), engine.state.value.enrichments)
            engine.toggleEnrichment("hillshade")
            runCurrent()

            assertEquals(listOf("pois", "hillshade"), engine.state.value.enrichments)
            assertEquals(
                listOf(
                    "add-enrichment:hillshade",
                    "add-enrichment:pois",
                    "remove-enrichment:hillshade",
                    "add-enrichment:hillshade",
                ),
                map.calls,
            )
            map.release()
            engine.close()
        }

    @Test
    fun theTerrainTheMapReportsIsStatedOnceTheCatalogueLandsUnlessOneWasChosen() =
        runTest {
            val shows = HeldLayersMap(catalogue, "outdoors", answersAtOnce = true)
            val reporting = LayersSelectionEngine(backgroundScope, shows)
            runCurrent()
            assertNull(reporting.state.value.terrain)
            shows.terrain = "satellite"
            shows.land(0)
            runCurrent()
            assertEquals("satellite", reporting.state.value.terrain)
            assertEquals(catalogue, reporting.state.value.catalogue)

            val chosen = HeldLayersMap(catalogue, "outdoors", answersAtOnce = true)
            val choosing = LayersSelectionEngine(backgroundScope, chosen)
            choosing.setTerrain("satellite")
            runCurrent()
            chosen.land(0)
            runCurrent()
            assertEquals("satellite", choosing.state.value.terrain)
            assertEquals(listOf("terrain:satellite"), chosen.calls)

            val none = HeldLayersMap(catalogue, null, answersAtOnce = true)
            val reportingNone = LayersSelectionEngine(backgroundScope, none)
            runCurrent()
            none.land(0)
            runCurrent()
            assertNull(reportingNone.state.value.terrain)
            assertEquals(catalogue, reportingNone.state.value.catalogue)
            assertTrue(none.calls.isEmpty())
            for (engine in listOf(reporting, choosing, reportingNone)) engine.close()
        }

    @Test
    fun aTerrainAlreadySelectedIsAskedOfTheMapAgain() =
        runTest {
            val map = HeldLayersMap(catalogue, "outdoors")
            val engine = LayersSelectionEngine(backgroundScope, map)
            engine.setTerrain("satellite")
            runCurrent()
            map.refuse(0)
            runCurrent()
            assertEquals("satellite|-||refused", shown(engine.state.value))

            engine.setTerrain("satellite")
            runCurrent()
            assertEquals("satellite|-||refused", shown(engine.state.value))
            map.accept(1)
            runCurrent()

            assertEquals("satellite|-||kept", shown(engine.state.value))
            assertEquals(listOf("terrain:satellite", "terrain:satellite"), map.calls)
            map.release()
            engine.close()
        }

    @Test
    fun theOutcomeOfTheCommandSettledLastStands() =
        runTest {
            val map = HeldLayersMap(catalogue, "outdoors")
            val engine = LayersSelectionEngine(backgroundScope, map)
            runCurrent()
            map.land(0)
            runCurrent()
            engine.setOverlay("slopes")
            engine.toggleEnrichment("hillshade")
            runCurrent()

            map.accept(1)
            runCurrent()
            assertNull(engine.state.value.refusal)
            map.refuse(0)
            runCurrent()
            assertEquals("outdoors|slopes|hillshade|refused", shown(engine.state.value))
            engine.setOverlay("snow-depth")
            assertEquals("outdoors|snow-depth|hillshade|refused", shown(engine.state.value))
            runCurrent()
            map.accept(2)
            runCurrent()

            assertEquals("outdoors|snow-depth|hillshade|kept", shown(engine.state.value))
            map.release()
            engine.close()
        }

    @Test
    fun aMapHandedInPlaceOfAnotherStartsAnEmptySelectionOwingItNothing() =
        runTest {
            val a = HeldLayersMap(catalogue, "outdoors")
            val b = HeldLayersMap(catalogue, "satellite")
            val engine = LayersSelectionEngine(backgroundScope, a)
            engine.setOverlay("slopes")
            runCurrent()

            engine.configure(b)
            assertEquals("-|-||kept", shown(engine.state.value))
            runCurrent()
            a.refuse(0)
            a.land(0)
            runCurrent()
            assertEquals("-|-||kept", shown(engine.state.value))
            assertNull(engine.state.value.catalogue)
            b.land(0)
            runCurrent()
            assertEquals("satellite|-||kept", shown(engine.state.value))

            // The same map brought back starts afresh too, and owes its earlier selection nothing.
            engine.toggleEnrichment("pois")
            runCurrent()
            engine.configure(a)
            runCurrent()
            b.refuse(0)
            runCurrent()

            assertEquals("-|-||kept", shown(engine.state.value))
            assertEquals(2, a.reads)
            a.release()
            b.release()
            engine.close()
        }

    @Test
    fun theMapItDrivesHandedAgainChangesNothingUnlessItsCatalogueReadFailed() =
        runTest {
            val map = HeldLayersMap(catalogue, "outdoors")
            val engine = LayersSelectionEngine(backgroundScope, map)
            engine.setOverlay("slopes")
            runCurrent()
            engine.configure(map)
            runCurrent()
            assertEquals("slopes", engine.state.value.overlay)
            assertEquals(1, map.reads)

            map.failRead(0)
            runCurrent()
            assertNotNull(engine.state.value.refusal, "the refused command is reported")
            engine.configure(map)
            runCurrent()
            assertEquals(2, map.reads)
            map.land(1)
            runCurrent()

            assertEquals(catalogue, engine.state.value.catalogue)
            assertNull(engine.state.value.refusal)
            assertEquals("slopes", engine.state.value.overlay)
            engine.configure(map)
            runCurrent()
            assertEquals(2, map.reads)
            map.release()
            engine.close()
        }

    @Test
    fun withNoMapACommandIsHeldAndDrivesNothing() =
        runTest {
            val map = HeldLayersMap(catalogue, "outdoors")
            val engine = LayersSelectionEngine(backgroundScope)
            engine.setOverlay("slopes")
            engine.toggleEnrichment("pois")
            engine.setTerrain("satellite")
            runCurrent()
            assertEquals("satellite|slopes|pois|kept", shown(engine.state.value))

            engine.configure(map)
            runCurrent()

            assertEquals("-|-||kept", shown(engine.state.value))
            assertTrue(map.calls.isEmpty())
            assertEquals(1, map.reads)
            map.release()
            engine.close()
        }

    /**
     * A state flow is the stream: a collector reads the selection as it is now, then each change. It never ends, so a
     * closed engine is told by nothing landing after it.
     */
    @Test
    fun theStateReportsTheSelectionNowAndEachChangeAndNothingLandsAfterClose() =
        runTest {
            val map = HeldLayersMap(catalogue, "outdoors", answersAtOnce = true)
            val engine = LayersSelectionEngine(backgroundScope, map)
            val seen = mutableListOf<String>()
            backgroundScope.launch(
                UnconfinedTestDispatcher(testScheduler),
            ) { engine.state.collect { seen += shown(it) } }
            runCurrent()
            map.land(0)
            runCurrent()
            engine.setOverlay("slopes")
            runCurrent()
            engine.close()
            engine.setOverlay("snow-depth")
            runCurrent()

            assertEquals(listOf("-|-||kept", "outdoors|-||kept", "outdoors|slopes||kept"), seen)
            assertEquals("outdoors|slopes||kept", shown(engine.state.value))
        }

    @Test
    fun aClosedEngineIsInertNothingLandsAndNoCommandDrivesTheMap() =
        runTest {
            val map = HeldLayersMap(catalogue, "outdoors")
            val engine = LayersSelectionEngine(backgroundScope, map)
            engine.setOverlay("slopes")
            runCurrent()

            engine.close()
            map.land(0)
            map.refuse(0)
            engine.setOverlay("snow-depth")
            engine.configure(HeldLayersMap(catalogue, null))
            runCurrent()

            assertEquals("-|slopes||kept", shown(engine.state.value))
            assertNull(engine.state.value.catalogue)
            assertEquals(listOf("overlay:slopes"), map.calls)
            map.release()
        }

    /**
     * A command that takes something off suspends as one that draws does, so a map of the app's own may wait on
     * its renderer: the selection waits for it, and reports its refusal and the end of it as it reports a draw's.
     */
    @Test
    fun aCommandThatTakesSomethingOffIsWaitedForAndItsRefusalReported() =
        runTest {
            val map = HeldLayersMap(catalogue, "outdoors", holdsTakeOffs = true)
            val engine = LayersSelectionEngine(backgroundScope, map)
            runCurrent()
            map.land(0)
            runCurrent()
            engine.setOverlay("slopes")
            engine.setOverlay("slopes")
            engine.toggleEnrichment("hillshade")
            engine.toggleEnrichment("hillshade")
            runCurrent()
            assertEquals(
                listOf("overlay:slopes", "overlay:-", "add-enrichment:hillshade", "remove-enrichment:hillshade"),
                map.calls,
            )

            map.accept(0)
            map.refuse(1)
            runCurrent()
            assertEquals("outdoors|-||refused", shown(engine.state.value), "the overlay the map would not take off")
            map.accept(2)
            map.accept(3)
            runCurrent()
            assertEquals("outdoors|-||kept", shown(engine.state.value))
            map.release()
            engine.close()
        }

    /**
     * A pin is a command as the others are: held as asked, driven at once, asked again for the instant held, its refusal
     * kept and reported, and standing through every other command.
     */
    @Test
    fun aPinIsHeldDrivenAtOnceAskedAgainAndStandsThroughTheOtherCommands() =
        runTest {
            val map = HeldLayersMap(catalogue, "outdoors")
            val engine = LayersSelectionEngine(backgroundScope, map)
            runCurrent()
            map.land(0)
            runCurrent()
            val pinned = Instant.parse("2026-03-15T12:00:00Z")

            engine.setInstant(pinned)
            assertEquals(pinned, engine.state.value.instant)
            runCurrent()
            map.refuse(0)
            runCurrent()
            assertNotNull(engine.state.value.refusal, "a refused pin is reported")
            assertEquals(pinned, engine.state.value.instant, "and kept")
            engine.setInstant(Instant.parse("2026-03-15T13:30:00+01:30"))
            engine.setOverlay("slopes")
            engine.setOverlay("slopes")
            runCurrent()
            map.accept(1)
            map.accept(2)
            runCurrent()
            assertEquals(pinned, engine.state.value.instant, "the pin stands through the overlay commands")
            engine.setInstant(null)
            runCurrent()
            map.accept(4)
            runCurrent()

            assertNull(engine.state.value.instant)
            assertEquals(
                listOf(
                    "instant:2026-03-15T12:00:00Z",
                    "instant:2026-03-15T12:00:00Z",
                    "overlay:slopes",
                    "overlay:-",
                    "instant:-",
                ),
                map.calls,
            )
            assertNull(engine.state.value.refusal)
            map.release()
            engine.close()
        }

    /**
     * A replaced map, or none, starts with none pinned and is told nothing, and with no map a pin is held and drives
     * nothing.
     */
    @Test
    fun aReplacedMapStartsWithNonePinnedAndWithNoMapAPinDrivesNothing() =
        runTest {
            val a = HeldLayersMap(catalogue, "outdoors", answersAtOnce = true)
            val b = HeldLayersMap(catalogue, "satellite", answersAtOnce = true)
            val engine = LayersSelectionEngine(backgroundScope, a)
            engine.setInstant(Instant.parse("2026-03-15T12:00:00Z"))
            runCurrent()

            engine.configure(b)
            assertNull(engine.state.value.instant)
            engine.configure(null)
            engine.setInstant(Instant.parse("2026-03-16T06:00:00Z"))
            runCurrent()

            assertEquals(Instant.parse("2026-03-16T06:00:00Z"), engine.state.value.instant)
            assertEquals(listOf("instant:2026-03-15T12:00:00Z"), a.calls)
            assertTrue(b.calls.isEmpty())
            a.release()
            b.release()
            engine.close()
        }

    /** Every engine is closeable as a resource, so `use` stops it. */
    @Test
    fun theEngineIsClosedAsAResource() =
        runTest {
            val map = HeldLayersMap(catalogue, "outdoors", answersAtOnce = true)
            val engine = LayersSelectionEngine(backgroundScope, map)

            engine.use { it.setOverlay("slopes") }
            engine.setOverlay("snow-depth")
            runCurrent()

            assertEquals(listOf("overlay:slopes"), map.calls)
            map.release()
        }
}
