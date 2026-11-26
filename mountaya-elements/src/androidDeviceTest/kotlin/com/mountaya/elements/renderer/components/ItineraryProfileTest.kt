package com.mountaya.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.mountaya.client.generated.RestSchema
import com.mountaya.elements.Controls.Drawn
import com.mountaya.elements.Controls.image
import com.mountaya.geo.ItineraryResource
import com.mountaya.geo.ItineraryResourceEngine
import com.mountaya.geo.ProfileClass
import com.mountaya.geo.ProfileRange
import com.mountaya.geo.ResolvedItinerary
import com.mountaya.geo.StatStripCell
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.formatDuration
import com.mountaya.geo.lineBetween
import kotlinx.coroutines.cancel
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The redraws a scrub may cost of a part the cursor does not move, and the curve paths it may build or draw, over sixty
 * scrubbed positions: none. The curve is a function of the itinerary and the window alone, and a part the cursor does not
 * move reads nothing it moves (`profile-cursor` in the contract's `sdk/hot-paths.yaml`).
 */
private const val PROFILE_CURSOR_BUDGET = 0

/**
 * The redraws a scrub may cost of the profile's root over sixty scrubbed positions: none. The cursor is held where the
 * parts it moves read it, and a redraw of the root redraws the whole element (`scrub-root`).
 */
private const val SCRUB_ROOT_BUDGET = 0

/** How many positions a scrub of a budget stands on. */
private const val SCRUB_POSITIONS = 60

class ItineraryProfileTest {
    @get:Rule
    val compose = createComposeRule()

    private val english = UnitPreferences("en")
    private val reads = Itineraries.reads()
    private val drawing = ItineraryDrawing(Itineraries.resolved, null)

    @AfterTest
    fun stopReading() {
        reads.cancel()
    }

    /** Waits until [count] plots are drawn. */
    private fun waitForPlots(count: Int = 1) {
        compose.waitUntil(TIMEOUT) {
            compose.onAllNodesWithContentDescription(PLOT).fetchSemanticsNodes().size == count
        }
    }

    /** The plot of the profile drawn under [tag]. */
    private fun plot(tag: String): SemanticsNodeInteraction =
        compose.onNode(hasContentDescription(PLOT) and hasAnyAncestor(hasTestTag(tag)))

    @Test
    fun itReadsItsItineraryThroughTheCacheAndDrawsItsPlotNoneForOneWithNoAltitude() {
        val cache = Itineraries.cache(reads)
        val flat = Itineraries.cache(reads, Itineraries.flat)
        compose.setContent {
            Column {
                Drawn("filled") { ItineraryProfile("itin_test", cache, Modifier.width(390.dp).height(400.dp)) }
                // A scrolling column offers it no height, so it lays itself out at its own.
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Drawn("flat") { ItineraryProfile("itin_test", flat, Modifier.width(390.dp)) }
                }
            }
        }
        waitForPlots()
        compose.waitUntil(TIMEOUT) { compose.onAllNodesWithText(EMPTY).fetchSemanticsNodes().isNotEmpty() }

        val chart = compose.onNodeWithContentDescription(PLOT).getBoundsInRoot()
        assertTrue(chart.bottom - chart.top > 300.dp, "the plot fills the height it is offered: $chart")
        val bare = compose.onNodeWithTag("flat").getBoundsInRoot()
        assertTrue(bare.bottom - bare.top < 100.dp, "its strip and its empty line: $bare")
        compose.onAllNodesWithContentDescription("Distance 2.2 km").assertCountEquals(2)
    }

    @Test
    fun aFailureToReadTheItineraryIsHandedToOnErrorAndDrawsTheEmptyState() {
        val unreadable = Itineraries.unreadable(reads)
        val failures = mutableListOf<String?>()
        compose.setContent {
            ItineraryProfile(
                "itin_test",
                unreadable,
                Modifier.width(390.dp),
                onError = { failures += it::class.simpleName },
            )
        }

        compose.waitUntil(TIMEOUT) { compose.runOnUiThread { failures.isNotEmpty() } }
        compose.runOnIdle { assertEquals(listOf<String?>("Unreadable"), failures) }
        compose.onAllNodesWithText(EMPTY).assertCountEquals(1)
    }

    @Test
    fun theStripWritesEachFigureInTheReadersUnitsADifficultyAsTheItineraryDoes() {
        val difficulty = RestSchema.TrailDifficultyType.MountainHiking

        assertEquals("2.2 km", statStripValue(StatStripCell.Distance(2_224.0), english))
        assertEquals("400 m", statStripValue(StatStripCell.Ascent(400.0), english))
        assertEquals("300 m", statStripValue(StatStripCell.Descent(300.0), english))
        assertEquals("1,250 m", statStripValue(StatStripCell.Elevation(1_250.0), english))
        assertEquals(formatDuration(5_400.0, english), statStripValue(StatStripCell.Duration(5_400.0), english))
        assertEquals("mountain-hiking", statStripValue(StatStripCell.Difficulty(difficulty), english))
    }

    @Test
    fun aTouchOnTheChartPinsTheMapsMarkerAtItsSampleAndLightsItsLegTheStripStatingItTillItLifts() {
        val map = Controls.engine()
        val cache = Itineraries.cache(reads)
        val points = drawing.series.points
        compose.setContent {
            ItineraryProfile(
                "itin_test",
                cache,
                Modifier.width(390.dp).height(240.dp),
                map = map,
                units = english,
            )
        }
        waitForPlots()
        compose.onAllNodesWithContentDescription("Elevation 1,400 m").assertCountEquals(1)

        // A quarter of the way along, the hut's sample, low in the area, clear of every pin.
        compose.onNodeWithContentDescription(PLOT).performTouchInput { down(Offset(width * 0.25f, height * 0.95f)) }
        compose.runOnIdle {
            val marker =
                map.state.value.markers
                    .single()
            assertEquals(PROFILE_CURSOR_MARKER_ID, marker.id)
            assertEquals(points[1].lngLat, marker.position)
            assertEquals(
                lineBetween(points, drawing.rows[0].from, drawing.rows[0].to),
                map.geometry(GeometrySlot.Emphasis).geometry,
            )
        }
        compose.onAllNodesWithContentDescription("Elevation 1,200 m").assertCountEquals(1)

        compose.onNodeWithContentDescription(PLOT).performTouchInput { up() }
        compose.runOnIdle {
            assertTrue(
                map.state.value.markers
                    .isEmpty(),
            )
            assertNull(map.geometry(GeometrySlot.Emphasis).geometry)
        }
        compose.onAllNodesWithContentDescription("Elevation 1,400 m").assertCountEquals(1)
    }

    @Test
    fun anOnHoverHandlerTakesTheMapsLinksOverAndADisabledProfileIsNoAdjustableValue() {
        val map = Controls.engine()
        val cache = Itineraries.cache(reads)
        val told = mutableListOf<Int?>()
        compose.setContent {
            Column {
                Drawn("handed") {
                    ItineraryProfile(
                        "itin_test",
                        cache,
                        Modifier.width(390.dp).height(200.dp),
                        map = map,
                        onHover = { told += it?.index },
                    )
                }
                Drawn("off") {
                    ItineraryProfile(
                        "itin_test",
                        cache,
                        Modifier.width(390.dp).height(200.dp),
                        map = map,
                        enabled = false,
                    )
                }
            }
        }
        waitForPlots(2)

        plot("handed").performSemanticsAction(SemanticsActions.SetProgress) { it(3f) }
        compose.runOnIdle {
            assertEquals(listOf<Int?>(3), told)
            assertTrue(
                map.state.value.markers
                    .isEmpty(),
                "the handler takes the marker over",
            )
            assertNull(map.geometry(GeometrySlot.Emphasis).geometry)
        }
        plot("off").assert(SemanticsMatcher.keyNotDefined(SemanticsActions.SetProgress))
    }

    @Test
    fun theSectionChipsGrowWithTheReadersFontScaleSoAChipNeverClipsItsWords() {
        var scale by mutableStateOf(1f)
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = scale)) {
                Drawn("chips") { ProfileSections(emptyList(), UnitPreferences("en"), Modifier.width(300.dp)) }
            }
        }

        for ((stated, height) in listOf(1f to 36.dp, 2f to 72.dp, 0.85f to 36.dp)) {
            scale = stated
            compose.waitForIdle()
            compose.onNodeWithTag("chips").assertHeightIsEqualTo(height)
        }
    }

    /**
     * A leg's chip hands over the id `geo.profile.row-match` names where the app listens, and writes nothing to the
     * map; a profile not enabled presses nothing, and one nobody listens to draws no button.
     */
    @Test
    fun aLegsChipHandsItsIdWhereTheAppListensWritesNothingToTheMapAndPressesNothingDisabled() {
        val map = Controls.engine()
        val cache = Itineraries.cache(reads)
        val pressed = mutableListOf<String>()
        val sections = setOf(ProfilePart.Sections)
        compose.setContent {
            Column {
                Drawn("listening") {
                    ItineraryProfile(
                        "itin_test",
                        cache,
                        Modifier.width(390.dp).height(240.dp),
                        map = map,
                        parts = sections,
                        onSegmentClick = { pressed += it },
                    )
                }
                Drawn("off") {
                    ItineraryProfile(
                        "itin_test",
                        cache,
                        Modifier.width(390.dp).height(240.dp),
                        map = map,
                        parts = sections,
                        enabled = false,
                        onSegmentClick = { pressed += it },
                    )
                }
                Drawn("deaf") {
                    ItineraryProfile("itin_test", cache, Modifier.width(390.dp).height(240.dp), parts = sections)
                }
            }
        }
        waitForPlots(3)

        chip("listening").performClick()
        compose.runOnIdle {
            assertEquals(listOf(drawing.rows[0].id), pressed)
            assertTrue(
                map.state.value.markers
                    .isEmpty(),
                "a press pins no marker",
            )
            assertNull(map.geometry(GeometrySlot.Emphasis).geometry, "a press lights no leg on the map")
        }
        chip("off").assert(SemanticsMatcher.keyIsDefined(SemanticsActions.OnClick)).assertIsNotEnabled()
        chip("off").performClick()
        compose.runOnIdle { assertEquals(listOf(drawing.rows[0].id), pressed, "a profile not enabled presses nothing") }
        chip("deaf").assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
    }

    /** The chip of the first leg, `Climb`, of the profile drawn under [tag]. */
    private fun chip(tag: String): SemanticsNodeInteraction =
        compose.onNode(hasContentDescription("Climb, ", substring = true) and hasAnyAncestor(hasTestTag(tag)))

    @Test
    fun theNavigatorMovesTheWindowTheAppHoldsWhichThePlotDraws() {
        val total = drawing.series.totalDistance
        val cache = Itineraries.cache(reads)
        val window = mutableStateOf<ProfileRange?>(null)
        compose.setContent {
            ItineraryProfile(
                "itin_test",
                cache,
                Modifier.width(390.dp).height(300.dp),
                parts = setOf(ProfilePart.Navigator),
                window = window,
            )
        }
        waitForPlots()
        compose.onNodeWithContentDescription(PLOT).assertRangeInfoEquals(ProgressBarRangeInfo(0f, 0f..5f, steps = 4))

        compose
            .onNodeWithContentDescription("Start of the range shown")
            .performSemanticsAction(SemanticsActions.SetProgress) { it(total.toFloat()) }
        compose.runOnIdle { assertEquals(ProfileRange(total / 20, total), window.value) }
        // The window now starts past the first sample, so the plot's first sample is the hut's.
        compose.onNodeWithContentDescription(PLOT).assertRangeInfoEquals(ProgressBarRangeInfo(1f, 1f..5f, steps = 3))
    }

    /** What a scrub over every part of a profile cost, part by part, and where the map's cursor marker stood. */
    private class Scrubbed(
        /** How many times each part was composed or drawn during the scrub, by its name. */
        val since: Map<String, Int>,
        /** How many times each part was composed or drawn before the scrub began. */
        val before: Map<String, Int>,
        /** How many times the chart's curve paths were built and drawn during the scrub. */
        val curve: Map<String, Int>,
        /** How many positions moved the map's cursor marker. */
        val pinned: Int,
        /**
         * How many positions stood on another place than the one before, read off the itinerary's own vertices: the
         * legs meet at the col, where the climb's last sample and the descent's first stand at one place, so a step
         * between those two moves the cursor and leaves the marker where it stands.
         */
        val placesMoved: Int,
    )

    /**
     * Draws the test itinerary's profile with every part over a map, the card among them where [card] says so, reporting to
     * a probe, then scrubs its plot over [SCRUB_POSITIONS] positions, an adjustment a position, each on another sample than
     * the one before and drawn on the screen before the next, and answers what the scrub cost.
     */
    private fun scrubEveryPart(card: Boolean): Scrubbed {
        val map = Controls.engine()
        val cache = Itineraries.cache(reads)
        val probe = RenderProbe()
        val parts = ProfilePart.entries.toSet().let { if (card) it else it - ProfilePart.Tooltip }
        val bands =
            listOf(ProfileClass(0.0, 1_000.0, "#2f9e44", "Easy"), ProfileClass(1_000.0, 2_224.0, "#e03131", "Hard"))
        compose.setContent {
            CompositionLocalProvider(LocalMountayaRenderProbe provides probe) {
                Drawn("profile") {
                    ItineraryProfile(
                        "itin_test",
                        cache,
                        Modifier.width(390.dp).height(480.dp),
                        map = map,
                        units = english,
                        bands = bands,
                        parts = parts,
                    )
                }
            }
        }
        waitForPlots()
        compose.image("profile")
        val before = compose.runOnIdle { probe.counts }
        val paths = compose.runOnIdle { assertNotNull(probe.plotPaths, "no chart was composed") }
        val built = compose.runOnIdle { paths.builds to paths.draws }

        val bounce = listOf(0, 1, 2, 3, 4, 5, 4, 3, 2, 1)
        // The place each sample stands at: the series is the climb's vertices then the descent's, none thinned.
        val places = (Itineraries.climb + Itineraries.descent).map { it.take(2) }
        val stoodAt = (0 until SCRUB_POSITIONS).map { places[bounce[it % bounce.size]] }
        val pinned =
            (0 until SCRUB_POSITIONS).map { position ->
                compose.onNodeWithContentDescription(PLOT).performSemanticsAction(SemanticsActions.SetProgress) {
                    it(bounce[position % bounce.size].toFloat())
                }
                compose.image("profile")
                compose.runOnIdle {
                    map.state.value.markers
                        .firstOrNull { it.id == PROFILE_CURSOR_MARKER_ID }
                        ?.position
                }
            }

        val after = compose.runOnIdle { probe.counts }
        val curve =
            compose.runOnIdle {
                mapOf(
                    "builds" to paths.builds - built.first,
                    "draws" to paths.draws - built.second,
                )
            }
        return Scrubbed(
            since = after.mapValues { (part, count) -> count - (before[part] ?: 0) },
            before = before,
            curve = curve,
            pinned = (listOf<List<Double>?>(null) + pinned).zipWithNext().count { (was, now) -> was != now },
            placesMoved = (listOf<List<Double>?>(null) + stoodAt).zipWithNext().count { (was, now) -> was != now },
        )
    }

    /** The parts a scrub leaves as they were, each placed from the itinerary, the chain or the window alone. */
    private fun unmoved(card: Boolean): List<String> =
        listOf(
            "ProfileChart",
            "ProfileChart content",
            "ProfileCurve",
            "ProfilePin hut",
            "ProfilePin col",
            "ProfilePin spring",
            "AmenityStackBadges",
            "ProfileDistanceAxis",
            "ProfileStretchStrip",
            "ProfileSections",
            "ProfileNavigator",
            "ProfileNavigator curve drawn",
        ) + if (card) listOf("ProfileStatStrip") else emptyList()

    /**
     * The parts a scrub moves, each reading the cursor, with the positions of [scrubbed] each is drawn again at, at least:
     * the dot on the curve, the plot's reading and the card at every position, and the strip at every position on another
     * place. The strip states the figures of the sample the cursor stands on, which the climb's last sample and the
     * descent's first share, and a strip handed the figures it states already is not drawn again.
     */
    private fun moved(
        card: Boolean,
        scrubbed: Scrubbed,
    ): Map<String, Int> =
        mapOf("ProfileCursor drawn" to SCRUB_POSITIONS, "ProfilePlotReading" to SCRUB_POSITIONS) +
            if (card) {
                mapOf("ProfileReadoutCard" to SCRUB_POSITIONS)
            } else {
                mapOf("ProfileStatStrip" to scrubbed.placesMoved)
            }

    /** Whether each part [scrubbed] moves was drawn again at the positions it states something new at, at least. */
    private fun followed(
        card: Boolean,
        scrubbed: Scrubbed,
    ): Boolean = moved(card, scrubbed).all { (part, least) -> (scrubbed.since[part] ?: 0) >= least }

    @Test
    fun hotPathBudgetProfileCursorSixtyPositionsScrubbedOverEveryPartAndTheCardRedrawOnlyWhatReadsTheCursor() {
        val scrubbed = scrubEveryPart(card = true)

        // Every part drew before the scrub, so a count of none below is a part the scrub left alone.
        assertTrue(unmoved(card = true).all { (scrubbed.before[it] ?: 0) >= 1 }, "drawn before: ${scrubbed.before}")
        // The work happened: the cursor stood on a sample of its own at every position, and what reads it followed it, the
        // map's marker at every position that stood on another place.
        assertEquals(scrubbed.placesMoved, scrubbed.pinned, "the map's marker moved at every position on another place")
        assertTrue(followed(card = true, scrubbed), "${scrubbed.since}")
        // No curve path was built or drawn, and no part the cursor does not move was drawn again.
        assertTrue(scrubbed.curve.values.all { it <= PROFILE_CURSOR_BUDGET }, "the curve: ${scrubbed.curve}")
        val redrawn = unmoved(card = true).associateWith { scrubbed.since[it] ?: 0 }
        assertTrue(redrawn.values.all { it <= PROFILE_CURSOR_BUDGET }, "redrawn: $redrawn")
    }

    @Test
    fun hotPathBudgetProfileCursorSixtyPositionsScrubbedOverEveryPartButTheCardRedrawOnlyWhatReadsTheCursor() {
        val scrubbed = scrubEveryPart(card = false)

        assertTrue(unmoved(card = false).all { (scrubbed.before[it] ?: 0) >= 1 }, "drawn before: ${scrubbed.before}")
        assertEquals(scrubbed.placesMoved, scrubbed.pinned, "the map's marker moved at every position on another place")
        assertTrue(followed(card = false, scrubbed), "${scrubbed.since}")
        assertTrue(scrubbed.curve.values.all { it <= PROFILE_CURSOR_BUDGET }, "the curve: ${scrubbed.curve}")
        val redrawn = unmoved(card = false).associateWith { scrubbed.since[it] ?: 0 }
        assertTrue(redrawn.values.all { it <= PROFILE_CURSOR_BUDGET }, "redrawn: $redrawn")
    }

    @Test
    fun hotPathBudgetScrubRootSixtyPositionsScrubbedNeverRedrawTheProfilesRoot() {
        val scrubbed = scrubEveryPart(card = false)
        // The root is the profile and the layout it lays its parts out in, which derive everything a scrub does not move.
        val root = listOf("ItineraryProfile", "ItineraryProfile content")

        assertTrue(root.all { (scrubbed.before[it] ?: 0) >= 1 }, "drawn before: ${scrubbed.before}")
        // The work happened: the cursor moved at every position, and its readers followed it, the strip, the dot and the
        // marker on the map.
        assertEquals(scrubbed.placesMoved, scrubbed.pinned, "the map's marker moved at every position on another place")
        assertTrue(followed(card = false, scrubbed), "${scrubbed.since}")
        val redrawn = root.associateWith { scrubbed.since[it] ?: 0 }
        assertTrue(redrawn.values.sum() <= SCRUB_ROOT_BUDGET, "the profile's root redrawn: $redrawn")
    }

    /**
     * Handed the app's engine, the profile reads its state, the itinerary the app chose through `configure`, and never
     * configures or closes it: the engine still answers the question the app asks after the profile left.
     */
    @Test
    fun aProfileOverTheAppsEngineReadsItsItineraryAndNeverClosesIt() {
        val cache = Itineraries.cache(reads)
        val flat = Itineraries.cache(reads, Itineraries.flat)
        val engine = compose.runOnUiThread { ItineraryResourceEngine(cache) }
        var isPresent by mutableStateOf(true)
        compose.setContent { if (isPresent) ItineraryProfile(engine, Modifier.width(390.dp).height(400.dp)) }
        compose.runOnUiThread { engine.configure(cache, "itin_test") }
        waitForPlots()

        compose.runOnUiThread { isPresent = false }
        compose.waitForIdle()
        compose.runOnUiThread { engine.configure(flat, "itin_test") }
        compose.waitUntil(TIMEOUT) {
            compose.runOnUiThread {
                (engine.state.value as? ItineraryResource.Resolved)?.itinerary == ResolvedItinerary(Itineraries.flat)
            }
        }
        compose.runOnUiThread { engine.close() }
    }

    /**
     * A profile's own strings stand over the scope's words for it alone, its empty line among them, and a profile
     * beside it handed none reads the scope's.
     */
    @Test
    fun aProfilesOwnStringsStandOverTheScopesWordsForItAlone() {
        val flat = Itineraries.cache(reads, Itineraries.flat)
        compose.setContent {
            Column {
                ItineraryProfile(
                    "itin_test",
                    flat,
                    Modifier.width(390.dp),
                    strings = mapOf("profile.empty" to "No altitude to draw."),
                )
                ItineraryProfile("itin_test", flat, Modifier.width(390.dp))
            }
        }
        compose.waitUntil(TIMEOUT) {
            compose.onAllNodesWithText("No altitude to draw.").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onAllNodesWithText("No altitude to draw.").assertCountEquals(1)
        compose.onAllNodesWithText(EMPTY).assertCountEquals(1)
    }

    private companion object {
        /** How long a test waits for a profile, whose itinerary is read on the main thread. */
        const val TIMEOUT = 5_000L

        /** What the plot is read as. */
        const val PLOT = "Elevation along the itinerary"

        /** What a profile with no altitude states. */
        const val EMPTY = "This itinerary carries no elevation data."
    }
}
