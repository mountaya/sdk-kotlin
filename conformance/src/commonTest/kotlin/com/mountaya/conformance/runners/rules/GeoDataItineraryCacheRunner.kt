package com.mountaya.conformance

import com.mountaya.geo.ItineraryResourceCache
import com.mountaya.geo.ItineraryResourceEngine
import kotlinx.coroutines.test.TestScope

/**
 * `geo.data.itinerary-cache`: plays the case's `steps`, each element that watches an engine of its own held
 * for the whole case, and answers each read sent and what each element watching shows once every step is
 * played. An element that reads asks once and goes: its engine is closed as soon as it is made. The capacity
 * the fixture declares is held to the cache's own.
 */
object GeoDataItineraryCacheRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(case, mapOf("capacity" to ItineraryResourceCache.capacity.toDouble()))
        val reads = ItineraryReads(scope)
        val watching = mutableListOf<Pair<String, ItineraryResourceEngine>>()
        try {
            for (item in case.fields.items("steps")) {
                val step = item as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
                when (val name = step.text("step")) {
                    "watch" -> {
                        val element = step.text("element")
                        if (watching.any { it.first == element }) {
                            throw AssertionError(
                                "the element `$element` watches twice",
                            )
                        }
                        watching +=
                            element to
                            ItineraryResourceEngine(
                                reads.cache(step.text("source")),
                                step.textOrNull("itinerary"),
                                ItineraryReads.surface(step),
                            )
                    }

                    "read" -> {
                        ItineraryResourceEngine(
                            reads.cache(step.text("source")),
                            step.textOrNull("itinerary"),
                            ItineraryReads.surface(step),
                        ).close()
                    }

                    "answer" -> {
                        reads.answer(stepRead(step))
                    }

                    "fail" -> {
                        reads.fail(stepRead(step))
                    }

                    "invalidate" -> {
                        reads.cache(step.text("source")).invalidate(step.textOrNull("itinerary"))
                    }

                    else -> {
                        throw AssertionError("`$name` is no step")
                    }
                }
                reads.settle()
            }
            val shows = watching.map { (element, engine) -> element to ItineraryReads.shown(engine.state.value) }
            return ConformanceOutcome.Value(
                FixtureValue.Object(listOf("requests" to reads.requests, "shows" to FixtureValue.Object(shows))),
            )
        } finally {
            for ((_, engine) in watching) engine.close()
            reads.drain()
            reads.settle()
        }
    }
}
