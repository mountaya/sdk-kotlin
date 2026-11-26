package com.mountaya.elements

import com.mountaya.client.generated.DataSchema
import com.mountaya.geo.SearchFetch
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import java.util.Collections
import kotlin.time.Duration

/**
 * Searches is what the search control's tests put behind a search in place of time and the network: a scheduler
 * whose time a test moves, the pauses waited out on it, and a transport that holds every request until the test
 * answers it. Every coroutine runs where the test moves the scheduler, the main thread, as the control is driven.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class Searches {
    /** The time the search's pauses are waited out on, which only the test moves. */
    val scheduler = TestCoroutineScheduler()

    /** The scope a search engine runs in, on the scheduler. */
    val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(scheduler))

    /** What a request the test fails throws. */
    class Failed : Exception("failed")

    /** Every request's input, in the order sent. */
    val sent: MutableList<DataSchema.SearchInput> = Collections.synchronizedList(mutableListOf())
    private val answers: MutableList<CompletableDeferred<DataSchema.SearchResult>> =
        Collections.synchronizedList(
            mutableListOf(),
        )

    /** The transport as a search takes it. */
    val fetch: SearchFetch =
        SearchFetch { input ->
            val answer = CompletableDeferred<DataSchema.SearchResult>()
            sent += input
            answers += answer
            answer.await()
        }

    /** Moves the scheduler's time on by [by] and runs every coroutine due, the main thread's turn of [onMain]. */
    fun advance(
        by: Duration,
        onMain: (() -> Unit) -> Unit,
    ) {
        onMain {
            scheduler.advanceTimeBy(by)
            scheduler.runCurrent()
        }
    }

    /** Runs every coroutine due now, on the main thread through [onMain]. */
    fun settle(onMain: (() -> Unit) -> Unit) {
        onMain { scheduler.runCurrent() }
    }

    /** Answers the request [index] with one result per name of [names] for the branch it asks. */
    fun answer(
        index: Int,
        names: List<String>,
    ) {
        val branch = branch(sent[index])
        answers[index].complete(
            DataSchema.SearchResult {
                when (branch) {
                    DataSchema.SearchTarget.Itineraries -> {
                        itineraries =
                            DataSchema.ItinerariesResult {
                                itineraries =
                                    names.mapIndexed { at, name ->
                                        DataSchema.ItinerarySearchResult {
                                            id = "itinerary-$at"
                                            this.name = name
                                        }
                                    }
                            }
                    }

                    DataSchema.SearchTarget.Zones -> {
                        zones =
                            DataSchema.ZonesResult {
                                zones =
                                    names.mapIndexed { at, name ->
                                        DataSchema.ZoneSearchResult {
                                            id = "zone-$at"
                                            this.name = name
                                            point = listOf(6.95, 45.95)
                                        }
                                    }
                            }
                    }

                    else -> {}
                }
            },
        )
    }

    /** Fails every request still open, so nothing waits once a test ends. */
    fun release() {
        for (answer in answers.toList()) answer.completeExceptionally(Failed())
    }

    /** The one branch [input] enables. */
    private fun branch(input: DataSchema.SearchInput): DataSchema.SearchTarget =
        with(input.targets) {
            when {
                itineraries == true -> DataSchema.SearchTarget.Itineraries
                zones == true -> DataSchema.SearchTarget.Zones
                places == true -> DataSchema.SearchTarget.Places
                pois == true -> DataSchema.SearchTarget.Pois
                ways == true -> DataSchema.SearchTarget.Ways
                else -> DataSchema.SearchTarget.Collections
            }
        }
}
