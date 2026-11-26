package com.mountaya.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToKeyAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mountaya.client.InternalMountayaApi
import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import com.mountaya.elements.Controls.Drawn
import com.mountaya.elements.Controls.color
import com.mountaya.elements.Controls.image
import com.mountaya.elements.Controls.pixel
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.ElementsScopeStatement
import com.mountaya.geo.HexColor
import com.mountaya.geo.SearchBranchException
import com.mountaya.geo.SearchConfiguration
import com.mountaya.geo.SearchEngine
import com.mountaya.geo.SearchFetch
import com.mountaya.geo.SearchFilter
import com.mountaya.geo.SearchHighlight
import com.mountaya.geo.SearchListState
import com.mountaya.geo.SearchListStatus
import com.mountaya.geo.SearchPause
import com.mountaya.geo.SearchRow
import com.mountaya.geo.SearchRowPayload
import com.mountaya.geo.SearchRowSecondary
import com.mountaya.geo.SearchSection
import com.mountaya.geo.SearchSectionState
import com.mountaya.geo.ThemeScheme
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.generated.ElementTokens
import com.mountaya.geo.highlightRanges
import com.mountaya.geo.scopeOf
import com.mountaya.geo.searchListState
import com.mountaya.geo.themeTokenValue
import kotlinx.coroutines.awaitCancellation
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Rule
import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class SearchControlTest {
    @get:Rule
    val compose = createComposeRule()

    /** A row of [title], the runs [query] matches emphasized, handing back [payload]. */
    private fun row(
        title: String,
        query: String = "",
        payload: SearchRowPayload,
        secondary: SearchRowSecondary? = null,
    ): SearchRow = SearchRow("row:$title", title, highlightRanges(title, query), secondary, emptyList(), payload)

    /** Runs [command] on the main thread, where the control and its engine are driven. */
    private val onMain: (() -> Unit) -> Unit = { command -> compose.runOnUiThread(command) }

    /** Reads [read] on the main thread. */
    private fun <T> read(read: () -> T): T = compose.runOnUiThread(read)

    @Test
    fun typingWaitsOutThePauseThenAsksARequestPerBranchTheSkeletonShownMeanwhile() {
        val searches = Searches()
        val engine =
            SearchEngine(
                searches.scope,
                SearchConfiguration(
                    targets = listOf(DataSchema.SearchTarget.Itineraries, DataSchema.SearchTarget.Zones),
                ),
                pause = SearchPause.standard,
            )
        val typed = mutableStateOf("")
        compose.setContent {
            SearchControlOver(
                map = null,
                engine = engine,
                configures = true,
                query = typed,
                targets = listOf(DataSchema.SearchTarget.Itineraries, DataSchema.SearchTarget.Zones),
                fetch = searches.fetch,
            )
        }
        compose.waitUntil(TIMEOUT) { read { engine.configuration.fetch != null } }

        // The app writes the text it holds, which the box shows.
        compose.runOnUiThread { typed.value = "cha" }
        compose.waitUntil(TIMEOUT) { read { engine.query == "cha" } }
        compose.onNode(hasSetTextAction()).assertTextEquals("cha")
        assertTrue(read { engine.state.value.isSearching }, "the skeleton of the round owed")
        assertTrue(
            read {
                engine.state.value.sections
                    .all { it.state.isLoading }
            },
        )
        searches.advance(149.milliseconds, onMain)
        assertTrue(searches.sent.isEmpty())
        searches.advance(1.milliseconds, onMain)
        assertEquals(listOf("cha", "cha"), searches.sent.map { it.query })
        assertFalse(read { engine.isConfined }, "a control with no map confines nothing")

        searches.answer(0, listOf("Chamonix", "Champex"))
        searches.answer(1, emptyList())
        searches.settle(onMain)
        assertTrue(read { engine.state.value.isSettled })
        val list = searchListState(read { engine.state.value }, SearchFilter.All)
        assertEquals(listOf("Chamonix", "Champex"), list.rows.map { it.title })
        assertEquals("2 results", searchStatusText(list.status, ElementsScope.standard))
        compose.runOnUiThread { engine.close() }
    }

    @Test
    fun theListShowsUnderTheBoxWhileItHoldsFocusOverAQuery() {
        val searches = Searches()
        val engine =
            SearchEngine(
                searches.scope,
                SearchConfiguration(targets = listOf(DataSchema.SearchTarget.Itineraries)),
                query = "mont",
            )
        val typed = mutableStateOf("mont")
        compose.setContent {
            Drawn("control", dark = true) {
                SearchControlOver(
                    map = null,
                    engine = engine,
                    configures = true,
                    query = typed,
                    targets = listOf(DataSchema.SearchTarget.Itineraries),
                    fetch = searches.fetch,
                )
            }
        }
        compose.waitUntil(TIMEOUT) { read { engine.configuration.fetch != null } }
        searches.settle(onMain)
        assertEquals(1, searches.sent.size, "a query the box starts with owes no pause")
        searches.answer(0, listOf("Tour du Mont Blanc"))
        searches.settle(onMain)
        assertFalse(read { engine.isOpen })

        compose.onNode(hasSetTextAction()).requestFocus()
        compose.waitUntil(TIMEOUT) { read { engine.isOpen } }
        compose.onNode(hasSetTextAction()).performImeAction()
        compose.waitUntil(TIMEOUT) { read { !engine.isOpen } }
        compose.runOnUiThread { engine.close() }
    }

    /**
     * The list stays shown while the focus moves from the box to its filter chips, anywhere in the control, as Geo's
     * `isSearchListShown` reads it, and goes once the focus leaves the control.
     */
    @Test
    fun theListStaysShownWhileTheFocusMovesToItsFilterChips() {
        val searches = Searches()
        val engine =
            SearchEngine(
                searches.scope,
                SearchConfiguration(targets = listOf(DataSchema.SearchTarget.Itineraries)),
                query = "mont",
            )
        val typed = mutableStateOf("mont")
        compose.setContent {
            SearchControlOver(
                map = null,
                engine = engine,
                configures = true,
                query = typed,
                targets = listOf(DataSchema.SearchTarget.Itineraries),
                fetch = searches.fetch,
            )
        }
        compose.waitUntil(TIMEOUT) { read { engine.configuration.fetch != null } }
        searches.settle(onMain)
        searches.answer(0, listOf("Tour du Mont Blanc"))
        searches.settle(onMain)

        compose.onNode(hasSetTextAction()).requestFocus()
        compose.waitUntil(TIMEOUT) { read { engine.isOpen } }
        compose.onNodeWithText(searchFilterLabel(SearchFilter.Zones, ElementsScope.standard)).requestFocus()
        compose.waitForIdle()
        assertTrue(read { engine.isOpen }, "a filter chip holding the focus keeps the list shown")

        compose.onNode(hasSetTextAction()).requestFocus()
        compose.onNode(hasSetTextAction()).performImeAction()
        compose.waitUntil(TIMEOUT) { read { !engine.isOpen } }
        compose.runOnUiThread { engine.close() }
    }

    @Test
    fun theListIsDrawnOnThePanelsGroundUnderEachSchemeItsNotesInTheScopesWords() {
        val empty = SearchListState(emptyList(), emptyList(), true, true, SearchListStatus.EmptyTitle)
        val found = SearchListState(emptyList(), emptyList(), false, false, null)
        val units = UnitPreferences("en")
        compose.setContent {
            Column {
                for ((tag, dark, list) in listOf(
                    Triple("light", false, empty),
                    Triple("dark", true, empty),
                    Triple("found", false, found),
                )) {
                    Drawn(tag, dark) {
                        SearchList(list, false, SearchFilter.All, {}, null, {}, units, {}, {}, Modifier.width(300.dp))
                    }
                }
            }
        }
        compose.waitForIdle()

        for ((tag, scheme) in listOf("light" to ThemeScheme.Light, "dark" to ThemeScheme.Dark)) {
            val ground = compose.pixel(compose.image(tag), 10.0, 10.0)
            val panel = themeTokenValue(ElementTokens.standard.panelBg, scheme)
            assertTrue(ground.isNear(color(panel.hex)), "$tag: $ground")
        }
        val emptyHeight = compose.onNodeWithTag("light").getBoundsInRoot().let { it.bottom - it.top }
        val foundHeight = compose.onNodeWithTag("found").getBoundsInRoot().let { it.bottom - it.top }
        assertTrue(emptyHeight > foundHeight + 40.dp, "the empty state and the note drawn: $emptyHeight, $foundHeight")
    }

    @Test
    fun aFailedBranchReadsTheElementsWordBesideItsRetryAndNeverTheApisMessage() {
        val failure = SearchBranchException(DataSchema.SearchTarget.Pois, message = "Upstream index unavailable")
        val failed = SearchSection(DataSchema.SearchTarget.Pois, SearchSectionState.Failed(failure))
        val list = SearchListState(emptyList(), listOf(failed), false, false, null)
        var retried = 0
        compose.setContent {
            SearchList(
                list,
                false,
                SearchFilter.All,
                {},
                null,
                {},
                UnitPreferences("en"),
                { retried += 1 },
                {},
                Modifier.width(300.dp),
            )
        }

        compose.onNodeWithText("The search could not be completed.").assertExists()
        compose.onNodeWithText("Upstream index unavailable").assertDoesNotExist()
        compose.onNodeWithText("Try again").performClick()
        compose.runOnIdle { assertEquals(1, retried) }
    }

    @Test
    fun aLongListComposesTheRowsItShowsWithinItsHeightAndScrollsToTheRest() {
        val rows =
            List(60) { row("Refuge $it", payload = SearchRowPayload.Place(listOf(6.0 + it / 100.0, 45.9), null)) }
        val list = SearchListState(rows, emptyList(), false, false, null)
        val picked = mutableListOf<String>()
        compose.setContent {
            Drawn("list") {
                SearchList(
                    list,
                    false,
                    SearchFilter.All,
                    {},
                    null,
                    {},
                    UnitPreferences("en"),
                    {},
                    { picked += it.id },
                    Modifier.width(300.dp),
                )
            }
        }
        compose.waitForIdle()

        val composed = rows.count { compose.onAllNodesWithText(it.title).fetchSemanticsNodes().isNotEmpty() }
        assertTrue(composed in 1 until rows.size, "$composed of ${rows.size} rows composed")
        val height = compose.onNodeWithTag("list").getBoundsInRoot().let { it.bottom - it.top }
        assertTrue(height <= 320.dp + 200.dp, "the rows scroll within their height: $height")

        compose.onNode(hasScrollToKeyAction()).performScrollToKey("row:Refuge 59")
        compose.onNodeWithText("Refuge 59").performClick()
        compose.runOnIdle { assertEquals(listOf("row:Refuge 59"), picked) }
    }

    @Test
    fun theStatusLineTheFiltersAndARowsTitleReadTheScopesWords() {
        val standard = ElementsScope.standard
        assertEquals("", searchStatusText(null, standard))
        assertEquals("The search could not be completed.", searchStatusText(SearchListStatus.Error, standard))
        assertEquals("Nothing found", searchStatusText(SearchListStatus.EmptyTitle, standard))
        assertEquals("1 result", searchStatusText(SearchListStatus.ResultCount(1), standard))
        assertEquals("3 results", searchStatusText(SearchListStatus.ResultCount(3), standard))
        val french = scopeOf(standard, ElementsScopeStatement(language = RestSchema.Language("fr")))
        assertEquals("1 résultat", searchStatusText(SearchListStatus.ResultCount(1), french))
        assertEquals("0 résultat", searchStatusText(SearchListStatus.ResultCount(0), french))
        assertEquals("Aucun résultat", searchStatusText(SearchListStatus.EmptyTitle, french))
        assertEquals(
            listOf("All", "Itineraries", "Places", "Zones"),
            SearchFilter.entries.map {
                searchFilterLabel(it, standard)
            },
        )
        assertEquals(
            listOf("Tout", "Itinéraires", "Lieux", "Zones"),
            SearchFilter.entries.map { searchFilterLabel(it, french) },
        )

        val title =
            searchRowTitle(
                row("Mont Blanc", query = "blanc", payload = SearchRowPayload.Place(listOf(6.86, 45.83), null)),
            )
        assertEquals("Mont Blanc", title.text)
        val bold = title.spanStyles.filter { it.item.fontWeight == FontWeight.Bold }
        assertEquals(listOf(5 until 10), bold.map { it.start until it.end })
    }

    @Test
    fun aRowIsDrawnTheTouchTargetHighItsTitleAndItsSecondLineInTheTokens() {
        val row =
            row(
                "Tour du Mont Blanc",
                query = "mont",
                payload = SearchRowPayload.Itinerary("tmb", null, null),
                secondary = SearchRowSecondary.Metric(170_000.0, 10_000),
            )
        compose.setContent { Drawn("row") { SearchResultRow(row, UnitPreferences("en"), {}, Modifier.width(300.dp)) } }
        compose.waitForIdle()

        val bounds = compose.onNodeWithTag("row").getBoundsInRoot()
        assertTrue(bounds.bottom - bounds.top >= 48.dp, "${bounds.bottom - bounds.top}")
        val image = compose.image("row")
        var inked = 0
        var x = 2.0
        while (x < 120.0) {
            var y = 2.0
            while (y < (bounds.bottom - bounds.top).value - 2.0) {
                val pixel = compose.pixel(image, x, y)
                if (pixel.alpha > 200 && pixel.red < 120) inked += 1
                y += 2.0
            }
            x += 2.0
        }
        assertTrue(inked > 20, "the title and its line are drawn: $inked")
    }

    @Test
    fun aPickMovesTheCameraOntoTheRowAndMarksItInTheAccentACollectionUnmarks() {
        val map = Controls.engine()
        val surface = ScriptedMapSurface(map)
        val accent = HexColor("#e9501e")

        searchPick(row("Lac Blanc", payload = SearchRowPayload.Place(listOf(6.9, 45.9), null)), map, accent, null)
        assertEquals(listOf(6.9, 45.9), map.state.value.camera.center)
        assertEquals(13.0, map.state.value.camera.zoom)
        assertEquals(
            JsonPrimitive("poi-icon-point-e9501e"),
            map.state.value.layers
                .last()
                .layout["icon-image"],
        )

        searchPick(
            row("Tour", payload = SearchRowPayload.Itinerary("t", listOf(6.8, 45.8, 6.9, 45.9), null)),
            map,
            accent,
            null,
        )
        assertEquals(listOf(6.8, 45.8, 6.9, 45.9), surface.frames.last().bounds, "an extent is framed")
        assertTrue(
            map.state.value.layers
                .none { it.id.startsWith("mountaya-search-highlight") },
            "an itinerary with no start marks nothing, taking the mark before down",
        )

        searchPick(row("Couloir", payload = SearchRowPayload.Zone("z", listOf(6.95, 45.95), null)), map, accent, null)
        assertEquals(
            "mountaya-search-highlight-place",
            map.state.value.layers
                .last()
                .id,
        )
        searchPick(
            row("Classics", payload = SearchRowPayload.Collection("c", "Classics", null, null)),
            map,
            accent,
            null,
        )
        assertTrue(
            map.state.value.layers
                .none { it.id.startsWith("mountaya-search-highlight") },
        )

        val handed = mutableListOf<String>()
        searchPick(
            row(
                "Lac Blanc",
                payload = SearchRowPayload.Place(listOf(7.0, 46.0), null),
            ),
            map,
            accent,
            onSelect = {
                handed +=
                    it.id
            },
        )
        assertEquals(listOf("row:Lac Blanc"), handed)
        assertNotEquals(listOf(7.0, 46.0), map.state.value.camera.center, "a handler stands in for the map")
    }

    @Test
    fun theControlTakesItsMapsSearchHighlightDownAsItLeavesOrIsHandedAnotherMap() {
        val first = Controls.engine()
        val second = Controls.engine()
        for (map in listOf(first, second)) map.setSearchHighlight(SearchHighlight.Place(listOf(6.86, 45.83)))

        fun highlighted(map: MapEngine): Boolean =
            map.state.value.sources
                .any { it.id == "mountaya-search-highlight" }
        val searches = Searches()
        val engine = SearchEngine(searches.scope)
        var shown by mutableStateOf<MapEngine?>(first)
        var isPresent by mutableStateOf(true)
        compose.setContent {
            if (isPresent) {
                SearchControlOver(
                    map = shown,
                    engine = engine,
                    configures = true,
                    fetch = SearchFetch { DataSchema.SearchResult() },
                )
            }
        }
        compose.waitUntil(TIMEOUT) { read { engine.configuration.fetch != null } }
        assertTrue(highlighted(first))

        compose.runOnUiThread { shown = second }
        compose.waitUntil(TIMEOUT) { !highlighted(first) }
        assertTrue(highlighted(second))

        compose.runOnUiThread { isPresent = false }
        compose.waitUntil(TIMEOUT) { !highlighted(second) }
        compose.runOnUiThread { engine.close() }
    }

    @Test
    fun aRowPressedInTheListMarksItOnTheMapAndLeavesTheBoxItsCountReadOutAsItChanges() {
        val searches = Searches()
        val map = Controls.engine()
        val zones = listOf(DataSchema.SearchTarget.Zones)
        val engine = SearchEngine(searches.scope, SearchConfiguration(targets = zones), query = "couloir")
        val typed = mutableStateOf("couloir")
        // The scope the control's own engine starts with, confined over a map, held here as the control holds its own.
        val confined = mutableStateOf(true)
        compose.setContent {
            SearchControlOver(
                map = map,
                engine = engine,
                configures = true,
                query = typed,
                isConfined = confined,
                targets = zones,
                fetch = searches.fetch,
            )
        }
        compose.waitUntil(TIMEOUT) { read { engine.configuration.fetch != null } }
        searches.settle(onMain)
        assertTrue(searches.sent.single().bbox != null, "a control over a map confined to its visible area")
        searches.answer(0, listOf("Couloir Nord"))
        searches.settle(onMain)

        compose.onNode(hasSetTextAction()).requestFocus()
        compose.waitUntil(TIMEOUT) { compose.onAllNodesWithText("Couloir Nord").fetchSemanticsNodes().isNotEmpty() }
        compose
            .onNode(hasContentDescription("1 result"))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        compose.onNodeWithText("Couloir Nord").performClick()

        compose.waitUntil(TIMEOUT) {
            map.state.value.layers
                .any { it.id == "mountaya-search-highlight-place" }
        }
        assertEquals(listOf(6.95, 45.95), map.state.value.camera.center, "a zone's point flown to")
        assertEquals(13.0, map.state.value.camera.zoom)
        compose.waitUntil(TIMEOUT) { read { !engine.isOpen } }
        assertTrue(compose.onAllNodesWithText("Couloir Nord").fetchSemanticsNodes().isEmpty(), "the list dismissed")
        compose.runOnUiThread { engine.close() }
    }

    /**
     * Handed the app's engine, the box renders and drives it, the text typed reaching it, and never configures it, the
     * engine's own configuration holding whatever the box is handed, nor closes it as it leaves.
     */
    @Test
    fun anAppsEngineIsRenderedAndDrivenButNeverConfiguredNorClosed() {
        val searches = Searches()
        val zones = listOf(DataSchema.SearchTarget.Zones)
        val configuration = SearchConfiguration(fetch = searches.fetch, targets = zones)
        val engine = SearchEngine(searches.scope, configuration)
        var isPresent by mutableStateOf(true)
        compose.setContent {
            if (isPresent) {
                SearchControl(
                    map = null,
                    engine = engine,
                    targets = listOf(DataSchema.SearchTarget.Itineraries),
                    fetch = SearchFetch { DataSchema.SearchResult() },
                )
            }
        }
        compose.onNode(hasSetTextAction()).requestFocus()
        compose.onNode(hasSetTextAction()).performTextReplacement("couloir")
        compose.waitUntil(TIMEOUT) { read { engine.query == "couloir" } }
        assertSame(configuration, read { engine.configuration }, "the engine's own configuration holds")
        searches.advance(150.milliseconds, onMain)
        assertEquals(listOf("couloir"), searches.sent.map { it.query }, "the engine's own branch and transport")

        compose.runOnUiThread { isPresent = false }
        compose.waitForIdle()
        compose.runOnUiThread { engine.setQuery("refuge") }
        assertEquals("refuge", read { engine.query }, "an engine the box closed would take no query")
        searches.release()
        compose.runOnUiThread { engine.close() }
    }

    /**
     * Where the app hoists no text, the box shows the engine's own, as typed, and follows it as the engine tells a new
     * state, so a query the app hands the engine directly reaches the box.
     */
    @Test
    fun withNoTextHoistedTheBoxShowsTheEnginesOwnTextAndFollowsIt() {
        val searches = Searches()
        val zones = SearchConfiguration(targets = listOf(DataSchema.SearchTarget.Zones))
        val engine = SearchEngine(searches.scope, zones, query = "col")
        compose.setContent { SearchControl(map = null, engine = engine) }
        compose.onNode(hasSetTextAction()).assertTextEquals("col")

        compose.runOnUiThread { engine.setQuery("couloir") }
        compose.waitUntil(TIMEOUT) {
            compose.onAllNodes(hasSetTextAction() and hasText("couloir")).fetchSemanticsNodes().isNotEmpty()
        }
        compose.runOnUiThread { engine.close() }
    }

    /**
     * A text the app hoists is kept in step with the engine both ways: a value the app writes reaches the engine, and
     * an edit of the field writes the value and the engine at once.
     */
    @Test
    fun aHoistedTextIsKeptInStepWithTheAppsEngineBothWays() {
        val searches = Searches()
        val engine = SearchEngine(searches.scope, SearchConfiguration(targets = listOf(DataSchema.SearchTarget.Zones)))
        val typed = mutableStateOf("")
        compose.setContent { SearchControl(map = null, engine = engine, query = typed) }

        compose.runOnUiThread { typed.value = "cha" }
        compose.waitUntil(TIMEOUT) { read { engine.query == "cha" } }
        compose.onNode(hasSetTextAction()).requestFocus()
        compose.onNode(hasSetTextAction()).performTextReplacement("refuge")
        compose.runOnIdle {
            assertEquals("refuge", typed.value)
            assertEquals("refuge", engine.query)
        }
        compose.runOnUiThread { engine.close() }
    }

    /**
     * Handed no engine, the box holds one of its own over its map: it starts with an empty text, confined to the
     * visible area of the map where a `SearchEngine` starts unconfined, asks through the transport it is handed, and is
     * closed as the box leaves, the round in flight abandoned.
     */
    @Test
    fun withNoEngineTheBoxsOwnStartsConfinedOverItsMapAndIsClosedAsItLeaves() {
        val map = Controls.engine()
        val sent = Collections.synchronizedList(mutableListOf<DataSchema.SearchInput>())
        val abandoned = AtomicBoolean(false)
        val fetch =
            SearchFetch { input ->
                sent += input
                try {
                    awaitCancellation()
                } finally {
                    abandoned.set(true)
                }
            }
        var isPresent by mutableStateOf(true)
        compose.setContent {
            if (isPresent) SearchControl(map, targets = listOf(DataSchema.SearchTarget.Zones), fetch = fetch)
        }
        compose.onNode(hasSetTextAction()).assertTextEquals("")

        compose.onNode(hasSetTextAction()).requestFocus()
        compose.onNode(hasSetTextAction()).performTextReplacement("couloir")
        compose.waitUntil(TIMEOUT) { sent.isNotEmpty() }
        assertTrue(sent.single().bbox != null, "its own engine starts confined to the visible area of its map")
        assertFalse(abandoned.get())

        compose.runOnUiThread { isPresent = false }
        compose.waitUntil(TIMEOUT) { abandoned.get() }
    }

    /**
     * The box's own strings stand over the scope's words for it alone: its label, and its count, a template whose
     * `{count}` takes the count in the scope's locale where they name `search.resultCount`.
     */
    @OptIn(InternalMountayaApi::class)
    @Test
    fun theBoxsOwnStringsStandOverTheScopesWordsItsCountATemplate() {
        val own =
            ElementsScope.standard.withStrings(
                mapOf("search.resultCount" to "{count} huts", "search.emptyTitle" to "No hut"),
            )
        assertEquals("3 huts", searchStatusText(SearchListStatus.ResultCount(3), own))
        assertEquals("1,234 huts", searchStatusText(SearchListStatus.ResultCount(1_234), own))
        assertEquals("No hut", searchStatusText(SearchListStatus.EmptyTitle, own))
        assertEquals("3 results", searchStatusText(SearchListStatus.ResultCount(3), ElementsScope.standard))

        compose.setContent {
            Column {
                SearchControl(map = null, strings = mapOf("search.label" to "Hut search"))
                SearchControl(map = null)
            }
        }
        compose.onNodeWithContentDescription("Hut search").assertExists()
        compose.onAllNodes(hasSetTextAction()).assertCountEquals(2)
    }

    private companion object {
        /** How long a test waits for the control, which is composed and driven on the main thread. */
        const val TIMEOUT = 5_000L
    }
}
