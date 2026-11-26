package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SearchListStateTest {
    private fun section(
        id: DataSchema.SearchTarget,
        state: SearchSectionState,
        titles: List<String> = emptyList(),
    ): SearchSection =
        SearchSection(
            id,
            state,
            titles.map { title ->
                SearchRow(
                    "${id.value}:$title",
                    title,
                    emptyList(),
                    null,
                    emptyList(),
                    SearchRowPayload.Itinerary(title, null, null),
                )
            },
        )

    private fun snapshot(
        sections: List<SearchSection>,
        searching: Boolean = false,
        settled: Boolean = true,
        fallback: Boolean = false,
    ): SearchSnapshot = SearchSnapshot("chamonix", searching, settled, fallback, sections)

    private val failed = SearchSectionState.Failed(SearchBranchException(DataSchema.SearchTarget.Zones))

    @Test
    fun aFailureUnderTheFilterIsAnnouncedAndOneOutsideItIsNot() {
        val round =
            snapshot(
                listOf(
                    section(DataSchema.SearchTarget.Itineraries, SearchSectionState.Settled),
                    section(DataSchema.SearchTarget.Places, SearchSectionState.Settled),
                    section(DataSchema.SearchTarget.Zones, failed),
                ),
            )
        val all = searchListState(round, SearchFilter.All)
        assertEquals(listOf(DataSchema.SearchTarget.Zones), all.failed.map { it.id })
        assertEquals<SearchListStatus?>(SearchListStatus.Error, all.status)
        assertFalse(all.isNothingFound)
        val places = searchListState(round, SearchFilter.Places)
        assertTrue(places.failed.isEmpty())
        assertTrue(places.isNothingFound)
        assertEquals<SearchListStatus?>(SearchListStatus.EmptyTitle, places.status)
    }

    @Test
    fun aSettledRoundCountsTheRowsItsFilterShowsInTheOrderTheyRender() {
        val round =
            snapshot(
                listOf(
                    section(
                        DataSchema.SearchTarget.Itineraries,
                        SearchSectionState.Settled,
                        listOf("Tour du Mont Blanc", "Haute Route"),
                    ),
                    section(DataSchema.SearchTarget.Places, SearchSectionState.Settled, listOf("Chamonix")),
                    section(DataSchema.SearchTarget.Collections, SearchSectionState.Settled, listOf("Alpine Classics")),
                ),
            )
        assertEquals(
            listOf("Tour du Mont Blanc", "Haute Route", "Chamonix", "Alpine Classics"),
            searchListState(round, SearchFilter.All).rows.map { it.title },
        )
        val itineraries = searchListState(round, SearchFilter.Itineraries)
        assertEquals(listOf("Tour du Mont Blanc", "Haute Route", "Alpine Classics"), itineraries.rows.map { it.title })
        assertEquals<SearchListStatus?>(SearchListStatus.ResultCount(3), itineraries.status)
    }

    @Test
    fun aRoundInFlightAnnouncesNothingButAFailureAndShowsTheRowsItKeeps() {
        val loading =
            snapshot(
                listOf(
                    section(DataSchema.SearchTarget.Itineraries, SearchSectionState.Loading),
                    section(DataSchema.SearchTarget.Zones, SearchSectionState.Loading),
                ),
                searching = true,
                settled = false,
            )
        assertNull(searchListState(loading, SearchFilter.All).status)
        assertFalse(searchListState(loading, SearchFilter.All).isNothingFound)
        val kept =
            snapshot(
                listOf(
                    section(DataSchema.SearchTarget.Itineraries, SearchSectionState.Settled, listOf("Lac Blanc")),
                    section(DataSchema.SearchTarget.Zones, SearchSectionState.Settled),
                ),
                searching = true,
                settled = false,
            )
        assertEquals(listOf("Lac Blanc"), searchListState(kept, SearchFilter.All).rows.map { it.title })
        assertNull(searchListState(kept, SearchFilter.All).status)
        val failing =
            snapshot(
                listOf(
                    section(DataSchema.SearchTarget.Itineraries, SearchSectionState.Loading),
                    section(DataSchema.SearchTarget.Zones, failed),
                ),
                searching = true,
                settled = false,
            )
        assertEquals<SearchListStatus?>(SearchListStatus.Error, searchListState(failing, SearchFilter.All).status)
    }

    @Test
    fun nothingIsFoundOnlyOnceARoundSettledAndAFallBackIsNotedEitherWay() {
        val asked =
            snapshot(
                listOf(
                    section(DataSchema.SearchTarget.Itineraries, SearchSectionState.Settled),
                    section(DataSchema.SearchTarget.Zones, SearchSectionState.Settled),
                ),
                settled = false,
            )
        assertFalse(searchListState(asked, SearchFilter.All).isNothingFound, "a search that asked nothing")
        assertNull(searchListState(asked, SearchFilter.All).status)
        val fellBack =
            snapshot(
                listOf(
                    section(DataSchema.SearchTarget.Itineraries, SearchSectionState.Settled),
                    section(DataSchema.SearchTarget.Zones, SearchSectionState.Settled),
                ),
                fallback = true,
            )
        assertTrue(searchListState(fellBack, SearchFilter.All).hasFallbackNote)
        assertTrue(searchListState(fellBack, SearchFilter.All).isNothingFound)
        val found =
            snapshot(
                listOf(
                    section(DataSchema.SearchTarget.Itineraries, SearchSectionState.Settled, listOf("Aiguillette")),
                    section(DataSchema.SearchTarget.Zones, SearchSectionState.Settled),
                ),
                fallback = true,
            )
        assertTrue(searchListState(found, SearchFilter.All).hasFallbackNote)
        assertEquals<SearchListStatus?>(
            SearchListStatus.ResultCount(1),
            searchListState(found, SearchFilter.All).status,
        )
        val none =
            snapshot(
                listOf(
                    section(
                        DataSchema.SearchTarget.Itineraries,
                        SearchSectionState.Settled,
                        listOf("Tour du Mont Blanc"),
                    ),
                    section(DataSchema.SearchTarget.Places, SearchSectionState.Settled, listOf("Chamonix")),
                ),
            )
        assertTrue(searchListState(none, SearchFilter.Zones).isNothingFound, "a filter showing none")
    }

    @Test
    fun aSectionIsShownByItsBranchsCodePoints() {
        val round = snapshot(listOf(section(DataSchema.SearchTarget("ZONÉS"), SearchSectionState.Settled, listOf("x"))))
        assertTrue(searchListState(round, SearchFilter.All).rows.isEmpty())
    }
}
