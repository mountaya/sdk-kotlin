package com.mountaya.conformance

import com.mountaya.client.ExchangeRequest
import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent

/**
 * `map.slots.latest-request`: which request each slot draws, played over an exchange that holds every read
 * until a step answers it: `request` is `setItinerary` or `setCollection`, `toggle` `setItineraries` or
 * `setZones`, `clear` the slot's clear, `answer` a specification of one source drawing the line `line-` then the
 * read's name, and `withdraw` the cancellation of the coroutine that asked, which is how a Kotlin caller
 * withdraws a call. Every call runs in a coroutine of its own on the test's scheduler, and each step settles
 * before the next. A read is named by its path: the id it asks for, or the content a toggle reads.
 */
@OptIn(ExperimentalCoroutinesApi::class)
object MapSlotsLatestRequestRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val held = MapContentFixtures.Held()
        val map = MapEngine(MapFixtures.client(exchange = held), geolocation = InertGeolocation())
        val calls = mutableMapOf<String, Job>()
        val contentOf = mutableListOf<Pair<String, String>>()

        fun call(work: suspend () -> Unit): Job =
            scope.launch {
                try {
                    work()
                } catch (failed: Exception) {
                    // What a call came to is read off what the map draws.
                }
            }
        for (item in case.fields.items("steps")) {
            val step = item as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
            when (val name = step.text("step")) {
                "request" -> {
                    val content = step.text("content")
                    val request = step.text("request")
                    contentOf += request to content
                    calls[request] =
                        when (content) {
                            "itinerary" -> call { map.setItinerary(request) }
                            "collection" -> call { map.setCollection(request) }
                            else -> throw AssertionError("no content `$content` is asked for by id")
                        }
                }

                "toggle" -> {
                    val content = step.text("content")
                    val on = step.bool("on")
                    contentOf += content to content
                    when (content) {
                        "itineraries" -> call { map.setItineraries(on) }
                        "zones" -> call { map.setZones(on) }
                        else -> throw AssertionError("no content `$content` is switched")
                    }
                }

                "clear" -> {
                    when (val content = step.text("content")) {
                        "itinerary" -> map.clearItinerary()
                        "collection" -> map.clearCollection()
                        "itineraries" -> call { map.setItineraries(false) }
                        "zones" -> call { map.setZones(false) }
                        else -> throw AssertionError("no content `$content` is cleared")
                    }
                }

                "answer" -> {
                    val request = step.text("request")
                    // A read withdrawn before its answer is no longer held, and the answer reaches nobody.
                    val read =
                        held.reads.indices.firstOrNull {
                            readName(held.reads[it].request) == request &&
                                held.isHeld(it)
                        }
                    if (read != null) {
                        val line =
                            MapFixtures.objectOf(
                                "id" to FixtureValue.Text("line-$request"),
                                "source_layer" to FixtureValue.Text("itinerary_lines"),
                                "type" to FixtureValue.Text("line"),
                            )
                        held.answer(
                            read,
                            MapContentFixtures.specificationBody(
                                MapContentFixtures.specification("source-$request", listOf(line)),
                            ),
                        )
                    }
                }

                "withdraw" -> {
                    calls[step.text("request")]?.cancel()
                }

                else -> {
                    throw AssertionError("no step is called `$name`")
                }
            }
            scope.runCurrent()
        }
        val drawn = mutableMapOf<String, String>()
        for ((request, content) in contentOf) {
            if (map.state.value.layers
                    .none { it.id == "line-$request" }
            ) {
                continue
            }
            val already = drawn[content]
            if (already != null &&
                already != request
            ) {
                throw AssertionError("the $content draws both $already and $request")
            }
            drawn[content] = request
        }
        val reads = held.reads.map { FixtureValue.Text(readName(it.request)) }
        held.release()
        scope.runCurrent()
        map.close()
        return ConformanceOutcome.Value(
            MapFixtures.objectOf(
                "drawn" to
                    FixtureValue.Object(
                        listOf("collection", "itineraries", "itinerary", "zones").map { content ->
                            content to (drawn[content]?.let { FixtureValue.Text(it) } ?: FixtureValue.Null)
                        },
                    ),
                "reads" to FixtureValue.Array(reads),
            ),
        )
    }

    /** A read named by its path: the id a specification is asked for by, or the content a toggle reads, `itineraries` or `zones`. */
    private fun readName(request: ExchangeRequest): String {
        val path =
            request.url
                .toString()
                .substringBefore('?')
                .substringAfter("://")
                .substringAfter('/')
        val parts = path.split('/').filter { it.isNotEmpty() }
        val version = parts.indexOf("v1")
        if (version < 0) return path
        val rest = parts.drop(version + 1)
        return if (rest.size == 3) rest[1] else rest.firstOrNull() ?: path
    }
}
