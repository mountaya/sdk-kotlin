package com.example.consumer

import com.mountaya.client.Mountaya
import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import com.mountaya.elements.MapEngine
import com.mountaya.geo.HexColor
import com.mountaya.geo.MapViewport
import com.mountaya.geo.POINT_ZOOM
import com.mountaya.geo.SearchBranchException
import com.mountaya.geo.SearchConfiguration
import com.mountaya.geo.SearchEngine
import com.mountaya.geo.SearchFetch
import com.mountaya.geo.SearchFilter
import com.mountaya.geo.SearchHighlight
import com.mountaya.geo.SearchLimit
import com.mountaya.geo.SearchListState
import com.mountaya.geo.SearchListStatus
import com.mountaya.geo.SearchPause
import com.mountaya.geo.SearchPick
import com.mountaya.geo.SearchPickCamera
import com.mountaya.geo.SearchRowPayload
import com.mountaya.geo.SearchSection
import com.mountaya.geo.SearchSectionState
import com.mountaya.geo.SearchSnapshot
import com.mountaya.geo.SearchWording
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.dataLanguageFor
import com.mountaya.geo.describePlace
import com.mountaya.geo.describePoi
import com.mountaya.geo.describeWay
import com.mountaya.geo.pickTarget
import com.mountaya.geo.searchListState
import com.mountaya.geo.searchSectionOrder
import com.mountaya.geo.searchTypingPause
import com.mountaya.geo.searchViewportRefreshPause
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Search reads the search as an app's shared code does, on every target: the engine over the app's client, what a
 * list of the app's own shows of its state, what a pick does to a map and the highlight it draws there, and a row's
 * second line. None of it names a composable.
 */
object Search {
    /**
     * Returns the README's search for huts: the engine asks the Data API through the client, and the list reads its
     * settled state.
     */
    suspend fun searchHuts(
        mountaya: Mountaya,
        scope: CoroutineScope,
    ): List<String> {
        val search =
            SearchEngine(
                scope,
                SearchConfiguration(
                    fetch = SearchFetch { input -> mountaya.data.search(input) },
                    language = dataLanguageFor("fr-CH"),
                    targets = listOf(DataSchema.SearchTarget.Pois, DataSchema.SearchTarget.Places),
                ),
            )
        search.setQuery("refuge")
        val settled = search.state.first { it.isSettled }
        search.close()
        return searchListState(settled, SearchFilter.Places).rows.map { row ->
            "${row.title}: ${if (pickTarget(row.payload).mark == null) "unmarked" else "marked"}"
        }
    }

    /** Returns the engine's every command and value, over a map's camera, under a cap and a pause of the app's own. */
    fun engine(
        scope: CoroutineScope,
        map: MapEngine,
    ): Int {
        val cap: SearchLimit? = SearchLimit.of(20)
        val pause: SearchPause = SearchPause.of(200.milliseconds) ?: SearchPause.standard
        val configuration =
            SearchConfiguration(
                fetch = SearchFetch { DataSchema.SearchResult() },
                language = DataSchema.Language.En,
                limit = cap,
                targets = searchSectionOrder,
                wording = SearchWording(UnitPreferences("en")),
                onError = { failure -> seen(failure.message) },
            )
        val camera = map.state.value.camera
        val search =
            SearchEngine(
                scope,
                configuration,
                query = "",
                camera = MapViewport(camera.center, camera.zoom),
                isOpen = false,
                isConfined = true,
                pause = pause,
                refreshPause = searchViewportRefreshPause,
            )
        search.setQuery("chamonix")
        search.setOpen(true)
        search.setConfined(false)
        search.setCamera(null)
        search.configure(configuration)
        search.retry()
        val snapshot: SearchSnapshot = search.state.value
        val read =
            seen(
                search.query,
                search.isOpen,
                search.isConfined,
                search.camera,
                search.configuration.fetch,
                search.configuration.wording,
                search.configuration.onError,
                cap?.value,
                snapshot.isSearching,
                snapshot.hasFallenBack,
                Duration.ZERO,
                pause.duration,
                searchTypingPause,
            )
        search.close()
        return read + snapshot.sections.sumOf { section(it) }
    }

    /** A section as a list of the app's own reads it: its branch, its state and its rows. */
    private fun section(section: SearchSection): Int =
        when (val state = section.state) {
            SearchSectionState.Loading -> seen(section.id, state.isLoading)
            SearchSectionState.Settled -> seen(section.id, state.isSettled, section.rows.size)
            is SearchSectionState.Failed -> seen((state.failure as? SearchBranchException)?.code, state.failure.message)
        }

    /**
     * Returns what a list shows under a filter, its status line by kind, and a pick drawn on a map in an accent of the
     * app's own.
     */
    fun picked(
        snapshot: SearchSnapshot,
        map: MapEngine,
    ): List<String> {
        val list: SearchListState = searchListState(snapshot, SearchFilter.All)
        val status =
            when (val announced = list.status) {
                SearchListStatus.Error -> announced.key
                SearchListStatus.EmptyTitle -> announced.key
                is SearchListStatus.ResultCount -> "${announced.key} ${announced.count}"
                null -> ""
            }
        val pick: SearchPick = pickTarget(SearchRowPayload.Place(listOf(6.86, 45.83), null))
        val camera =
            when (val move = pick.camera) {
                is SearchPickCamera.Fit -> "fit ${move.bounds}"
                is SearchPickCamera.Fly -> "fly ${move.center} ${move.zoom}"
                null -> "stay"
            }
        val mark =
            when (val highlight = pick.mark) {
                is SearchHighlight.Place -> "place ${highlight.point}"
                is SearchHighlight.Poi -> "poi ${highlight.category}"
                is SearchHighlight.Way -> "way ${highlight.anchor} ${highlight.bounds} ${highlight.coordinates}"
                null -> "none"
            }
        map.setSearchHighlight(pick.mark, HexColor("#e9501e"))
        map.setSearchHighlight(SearchHighlight.Way(listOf(6.86, 45.83), listOf(6.8, 45.8, 6.9, 45.9), null))
        map.setSearchHighlight(null)
        val failure =
            SearchBranchException(DataSchema.SearchTarget.Zones, DataSchema.ErrorCode.BadGateway, "unreachable")
        return listOf(
            status,
            camera,
            mark,
            "$POINT_ZOOM",
            "${list.rows.size} ${list.failed.size} ${list.isNothingFound} ${list.hasFallbackNote}",
            "${failure.target} ${failure.code} ${failure.message}",
        )
    }

    /** Returns a result's second line, worded for the reader and in their units, or in English and metric. */
    fun described(
        place: DataSchema.Place,
        poi: DataSchema.POI,
        way: DataSchema.Way,
    ): List<String> {
        val imperial = SearchWording(UnitPreferences("en", RestSchema.UnitLength.Imperial))
        return listOf(describePlace(place), describePoi(poi, imperial), describeWay(way))
    }
}
