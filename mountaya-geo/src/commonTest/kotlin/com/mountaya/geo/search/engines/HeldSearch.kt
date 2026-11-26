package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

/**
 * HeldSearch is a search's transport a test drives: it records every request in the order the engine sends it, holds
 * each open until the test answers, fails or reports it, and records each request the engine abandoned while it was
 * still open. The engine runs in the test's scope, so the requests are numbered in the order they leave.
 */
internal class HeldSearch {
    /** What a request the test fails throws. */
    class Failed : Exception("failed")

    /** What a request still open as a test ends throws. */
    class Released : Exception("released")

    /** Every request's input, in the order sent. */
    val sent = mutableListOf<DataSchema.SearchInput>()

    /** Every request the engine abandoned while no answer had settled it. */
    val cancelled = mutableSetOf<Int>()
    private val answers = mutableListOf<CompletableDeferred<DataSchema.SearchResult>?>()
    private val done = mutableSetOf<Int>()

    /** The transport as a search takes it. */
    val fetch: SearchFetch = SearchFetch { input -> request(input) }

    /** The branch each request asked, in the order sent. */
    val branches: List<DataSchema.SearchTarget> get() = sent.map(::branch)

    private suspend fun request(input: DataSchema.SearchInput): DataSchema.SearchResult {
        val index = sent.size
        sent += input
        val answer = CompletableDeferred<DataSchema.SearchResult>()
        answers += answer
        try {
            return answer.await()
        } catch (withdrawn: CancellationException) {
            if (index !in done && !currentCoroutineContext().isActive) cancelled += index
            throw withdrawn
        }
    }

    /** Answers the request [index] with one result per name of [names] for its branch. */
    fun answer(
        index: Int,
        names: List<String>,
    ) {
        settle(index)?.complete(result(branch(sent[index]), names))
    }

    /** Answers the request [index] with the Data API reporting its branch failed. */
    fun report(
        index: Int,
        message: String? = "unreachable",
    ) {
        val branch = branch(sent[index])
        settle(index)?.complete(
            DataSchema.SearchResult {
                errors =
                    listOf(
                        DataSchema.SearchTargetError {
                            target = branch
                            code = DataSchema.ErrorCode.BadGateway
                            this.message = message
                        },
                    )
            },
        )
    }

    /** Fails the request [index]. */
    fun fail(index: Int) {
        settle(index)?.completeExceptionally(Failed())
    }

    /** Fails every request still open, so no coroutine waits for ever once a test ends. */
    fun release() {
        for (index in answers.indices) {
            answers[index]?.completeExceptionally(Released())
            answers[index] = null
        }
    }

    private fun settle(index: Int): CompletableDeferred<DataSchema.SearchResult>? {
        val answer = answers.getOrNull(index) ?: return null
        answers[index] = null
        done += index
        return answer
    }

    companion object {
        /** The one branch [input] enables. */
        fun branch(input: DataSchema.SearchInput): DataSchema.SearchTarget {
            val targets = input.targets
            val flags =
                listOf(
                    targets.places to DataSchema.SearchTarget.Places,
                    targets.pois to DataSchema.SearchTarget.Pois,
                    targets.ways to DataSchema.SearchTarget.Ways,
                    targets.itineraries to DataSchema.SearchTarget.Itineraries,
                    targets.collections to DataSchema.SearchTarget.Collections,
                    targets.zones to DataSchema.SearchTarget.Zones,
                )
            return flags.firstOrNull { it.first == true }?.second ?: DataSchema.SearchTarget("NONE")
        }

        /** The Data API's answer for [branch]: one result per name, each titled by its name. */
        fun result(
            branch: DataSchema.SearchTarget,
            names: List<String>,
        ): DataSchema.SearchResult =
            DataSchema.SearchResult {
                when (branch) {
                    DataSchema.SearchTarget.Places -> {
                        places =
                            DataSchema.GeocodeResult {
                                places =
                                    names.mapIndexed { index, name ->
                                        SearchEntity.place(name = name, point = listOf(6.87, 45.92 + index / 100.0))
                                    }
                            }
                    }

                    DataSchema.SearchTarget.Pois -> {
                        pois = DataSchema.PoisResult { pois = names.map { SearchEntity.poi(name = it) } }
                    }

                    DataSchema.SearchTarget.Ways -> {
                        ways = DataSchema.WaysResult { ways = names.map { SearchEntity.way(name = it) } }
                    }

                    DataSchema.SearchTarget.Itineraries -> {
                        itineraries =
                            DataSchema.ItinerariesResult {
                                itineraries =
                                    names.mapIndexed { index, name ->
                                        DataSchema.ItinerarySearchResult {
                                            id = "itinerary-$index"
                                            this.name = name
                                        }
                                    }
                            }
                    }

                    DataSchema.SearchTarget.Collections -> {
                        collections =
                            DataSchema.CollectionsResult {
                                collections =
                                    names.mapIndexed { index, name ->
                                        DataSchema.CollectionSearchResult {
                                            id = "collection-$index"
                                            this.name = name
                                        }
                                    }
                            }
                    }

                    DataSchema.SearchTarget.Zones -> {
                        zones =
                            DataSchema.ZonesResult {
                                zones =
                                    names.mapIndexed { index, name ->
                                        DataSchema.ZoneSearchResult {
                                            id = "zone-$index"
                                            this.name = name
                                        }
                                    }
                            }
                    }

                    else -> {}
                }
            }
    }
}

/**
 * A snapshot read as `query|searching|settled|fellBack|section:state[rows],...`, each state's first letter, so a test
 * states what a list shows in one line.
 */
internal fun shown(snapshot: SearchSnapshot): String {
    val sections =
        snapshot.sections.joinToString(",") { section ->
            val state =
                when (section.state) {
                    SearchSectionState.Loading -> "l"
                    SearchSectionState.Settled -> "s"
                    is SearchSectionState.Failed -> "e"
                }
            val rows = if (section.rows.isEmpty()) "" else section.rows.joinToString(",", "[", "]") { it.title }
            "${section.id.value.lowercase()}:$state$rows"
        }
    return listOf(
        snapshot.query,
        if (snapshot.isSearching) "searching" else "idle",
        if (snapshot.isSettled) "settled" else "-",
        if (snapshot.hasFallenBack) "fell-back" else "-",
        sections,
    ).joinToString("|")
}
