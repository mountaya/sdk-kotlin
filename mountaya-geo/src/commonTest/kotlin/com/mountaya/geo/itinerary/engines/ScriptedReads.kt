package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.CompletableDeferred

/**
 * ScriptedReads stands for the clients an itinerary is read through. Each read a source is asked for is
 * recorded in the order it was sent and held open until the test answers or fails it.
 *
 * A source suspends on a deferred the test completes, so every step of an engine over it runs on the
 * test's scheduler in order, and the scheduler's `runCurrent` lets a step land.
 */
internal class ScriptedReads {
    /** One read a source was asked for. */
    class Read(
        val itineraryId: String,
        val surface: RestSchema.ReadSurface?,
        val client: String,
        val answer: CompletableDeferred<RestSchema.ItineraryWithDetails>,
    )

    /** The failure a read is failed with unless a test names another. */
    class Failed(
        val index: Int,
    ) : Exception("read $index failed")

    val sent = mutableListOf<Read>()
    private val settled = mutableSetOf<Int>()

    /** The source of the client [client], which records each read it is asked for and holds it open. */
    fun source(client: String = "a"): ItinerarySource =
        ItinerarySource { itineraryId, surface ->
            val answer = CompletableDeferred<RestSchema.ItineraryWithDetails>()
            sent += Read(itineraryId, surface, client, answer)
            answer.await()
        }

    /** Every read sent, as `client:itinerary:surface`, the draft spelled `draft`. */
    val asked: List<String> get() = sent.map { "${it.client}:${it.itineraryId}:${it.surface?.value ?: "draft"}" }

    /**
     * answer answers read [index] with [itinerary], by default one whose one segment, `read-<index>`,
     * names the read and is [distance] meters long.
     */
    fun answer(
        index: Int,
        itinerary: RestSchema.ItineraryWithDetails? = null,
        distance: Double = 1_000.0,
    ) {
        if (index >= sent.size || !settled.add(index)) return
        val read = sent[index]
        read.answer.complete(itinerary ?: answered(index, read.itineraryId, distance))
    }

    /** fail fails read [index] with [error], by default [Failed] naming the read. */
    fun fail(
        index: Int,
        error: Throwable? = null,
    ) {
        if (index >= sent.size || !settled.add(index)) return
        sent[index].answer.completeExceptionally(error ?: Failed(index))
    }

    /** drain fails every read still open, so no coroutine is left waiting when a test ends. */
    fun drain() {
        for (index in sent.indices) fail(index)
    }

    /** The itinerary read [index] answers: one segment named by the read. */
    private fun answered(
        index: Int,
        itineraryId: String,
        distance: Double,
    ) = restItinerary(
        itineraryId,
        segments =
            listOf(
                restSegment(
                    "read-$index",
                    position = 0,
                    geojson = lineString(listOf(6.0, 45.0), listOf(6.01, 45.0)),
                    distance = distance,
                ),
            ),
    )
}

/**
 * The read whose answer [state] shows, read off its first leg's id, or null where it shows none.
 */
internal fun shownRead(state: ItineraryResource): Int? {
    if (state !is ItineraryResource.Resolved) return null
    val leg = state.itinerary.legs.firstOrNull() ?: return null
    return if (leg.id.startsWith("read-")) leg.id.removePrefix("read-").toIntOrNull() else null
}

/** What [state] shows, as the contract's fixtures name it: `idle`, `resolving`, `failed`, or the read whose answer it shows. */
internal fun named(state: ItineraryResource): String =
    when (state) {
        ItineraryResource.Idle -> "idle"
        ItineraryResource.Resolving -> "resolving"
        is ItineraryResource.Failed -> "failed"
        is ItineraryResource.Resolved -> shownRead(state)?.let { "read-$it" } ?: "resolved"
    }
