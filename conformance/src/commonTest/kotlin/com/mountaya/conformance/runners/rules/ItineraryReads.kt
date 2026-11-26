package com.mountaya.conformance

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.ItineraryResource
import com.mountaya.geo.ItineraryResourceCache
import com.mountaya.geo.ItinerarySource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlin.time.Instant

/**
 * ItineraryReads stands for the clients an itinerary is read through, `a` and `b`, each with the one cache
 * an app makes for its client, its reads in the case's background scope. Every read is held open until a
 * step answers or fails it, by the order it was sent in, which is how a step names it, and an answer is an
 * itinerary whose one segment names the read. A step settles on the test's scheduler, no clock read.
 */
internal class ItineraryReads(
    private val scope: TestScope,
    /**
     * What every answer holds where a case states it: the members `segment` writes, merged into the one segment,
     * and the `waypoints`, each holding the `timing` it writes.
     */
    private val answered: FixtureValue.Object? = null,
) {
    /** One read a client was asked for. */
    private class Read(
        val itinerary: String,
        val surface: RestSchema.ReadSurface?,
        val client: String,
        val answer: CompletableDeferred<RestSchema.ItineraryWithDetails>,
    )

    /** What a read a step fails throws. */
    private class Failed(
        read: Int,
    ) : Exception("read $read failed")

    private val sent = mutableListOf<Read>()
    private val settled = mutableSetOf<Int>()
    private val caches = mutableMapOf<String, ItineraryResourceCache>()

    /** The cache of the client a step names, made the first time it is named. */
    fun cache(client: String): ItineraryResourceCache {
        if (client != "a" && client != "b") throw AssertionError("`$client` is no client")
        return caches.getOrPut(client) {
            ItineraryResourceCache(
                scope.backgroundScope,
                ItinerarySource { itineraryId, surface ->
                    val answer = CompletableDeferred<RestSchema.ItineraryWithDetails>()
                    sent += Read(itineraryId, surface, client, answer)
                    answer.await()
                },
            )
        }
    }

    /** Answers the read a step names with an itinerary whose one segment names the read. */
    fun answer(index: Int) {
        val read = open(index)
        read.answer.complete(
            RestSchema.ItineraryWithDetails(
                activity_id = "hiking",
                ascent = null,
                count = RestSchema.ItineraryCount(groups = 0, segments = 1, waypoints = 0),
                created_at = Instant.fromEpochMilliseconds(0),
                created_by = RestSchema.RelationToUser(id = "user"),
                description = null,
                distance = 1_000.0,
                id = read.itinerary,
                is_private = false,
                is_verified = false,
                name = read.itinerary,
                organization_id = null,
                etag = "etag",
                revision = 1,
                status = RestSchema.ItineraryStatus.Draft,
                updated_at = Instant.fromEpochMilliseconds(0),
                groups = emptyList(),
                has_unpublished_changes = false,
                published_at = null,
                segments = listOf(fixtureSegment(answeredSegment(index))),
                waypoints = answered?.let { it.items("waypoints").map(::timedWaypoint) }.orEmpty(),
            ),
        )
    }

    /** The segment read [index] answers, as a fixture writes one: named by the read, a line of two positions. */
    private fun answeredSegment(index: Int): FixtureValue.Object {
        fun position(vararg values: Double) = FixtureValue.Array(values.map { FixtureValue.Number(it) })
        val line =
            FixtureValue.Object(
                listOf(
                    "coordinates" to FixtureValue.Array(listOf(position(6.0, 45.0), position(6.01, 45.0))),
                    "type" to FixtureValue.Text("LineString"),
                ),
            )
        val stated = answered?.objectOrNull("segment")?.entries.orEmpty()
        return FixtureValue.Object(
            listOf(
                "geojson" to line,
                "id" to FixtureValue.Text("read-$index"),
                "position" to FixtureValue.Number(0.0),
            ) +
                stated,
        )
    }

    /** Fails the read a step names. */
    fun fail(index: Int) {
        open(index).answer.completeExceptionally(Failed(index))
    }

    /** Fails every read still open, so no coroutine is left waiting once a case ends. */
    fun drain() {
        for (index in sent.indices) {
            if (settled.add(index)) sent[index].answer.completeExceptionally(Failed(index))
        }
    }

    /** Every read sent, with its `itinerary`, `source` and `surface`. */
    val requests: FixtureValue
        get() =
            FixtureValue.Array(
                sent.map {
                    FixtureValue.Object(
                        listOf(
                            "itinerary" to FixtureValue.Text(it.itinerary),
                            "source" to FixtureValue.Text(it.client),
                            "surface" to FixtureValue.Text(it.surface?.value ?: "draft"),
                        ),
                    )
                },
            )

    /** Lets every read answered so far land and every engine it moves be told, on the test's scheduler. */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun settle() = scope.testScheduler.runCurrent()

    /** The read a step names, which must have been sent and not yet settled. */
    private fun open(index: Int): Read {
        if (index !in sent.indices) throw AssertionError("read $index was never sent")
        if (!settled.add(index)) throw AssertionError("read $index is settled twice")
        return sent[index]
    }

    companion object {
        /** The surface a step names, the draft being the one a question names no surface for. */
        fun surface(step: FixtureValue.Object): RestSchema.ReadSurface? =
            when (val surface = step.text("surface")) {
                "draft" -> null
                "published" -> RestSchema.ReadSurface.Published
                else -> throw AssertionError("`$surface` is no surface")
            }

        /**
         * What an element shows as the fixtures read it: `idle`, `resolving` or `failed` with nothing shown,
         * or `resolved` with the read whose answer it shows, every projection of it held, and, where
         * [characteristics] asks, the summary it shows, as `geo.profile.characteristics` writes it, or null.
         */
        fun shown(
            state: ItineraryResource,
            characteristics: Boolean = false,
        ): FixtureValue {
            fun snapshot(
                state: String,
                shows: Int?,
                summary: FixtureValue = FixtureValue.Null,
            ) = FixtureValue.Object(
                listOfNotNull(
                    ("characteristics" to summary).takeIf { characteristics },
                    "shows" to (shows?.let { FixtureValue.Number(it.toDouble()) } ?: FixtureValue.Null),
                    "state" to FixtureValue.Text(state),
                ),
            )
            return when (state) {
                ItineraryResource.Idle -> {
                    snapshot("idle", null)
                }

                ItineraryResource.Resolving -> {
                    snapshot("resolving", null)
                }

                is ItineraryResource.Failed -> {
                    snapshot("failed", null)
                }

                is ItineraryResource.Resolved -> {
                    // Every answer of a script carries a geometry and one leg named by its read.
                    val id =
                        state.itinerary.legs
                            .firstOrNull()
                            ?.id
                    val read = id?.takeIf { it.startsWith("read-") }?.removePrefix("read-")?.toIntOrNull()
                    if (state.itinerary.geometry == null || read == null) {
                        throw AssertionError("a resolved element shows no answer a read names")
                    }
                    snapshot("resolved", read, characteristicsValue(state.itinerary.characteristics))
                }
            }
        }
    }
}
