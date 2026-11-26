package com.mountaya.conformance

import com.mountaya.geo.ItineraryResourceEngine
import kotlinx.coroutines.test.TestScope

/**
 * `geo.data.itinerary-resource`: plays the case's `steps` over one element watching, an engine held for the
 * whole case, idle on client `a`'s cache until its first question, and answers each read it sent and what it
 * shows after each step. Every read runs in the case's background scope on the test's scheduler, so each
 * step is settled by running what it queued, never by a clock. Where the case states what every read is
 * `answered` with, each answer holds it and each snapshot carries the summary the element shows.
 */
object GeoDataItineraryResourceRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val answered = case.fields.objectOrNull("answered")
        val reads = ItineraryReads(scope, answered)
        val engine = ItineraryResourceEngine(reads.cache("a"))
        val snapshots = mutableListOf<FixtureValue>()
        try {
            for (item in case.fields.items("steps")) {
                val step = item as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
                when (val name = step.text("step")) {
                    "configure" -> {
                        engine.configure(
                            reads.cache(step.text("source")),
                            step.textOrNull("itinerary"),
                            ItineraryReads.surface(step),
                        )
                    }

                    "answer" -> {
                        reads.answer(stepRead(step))
                    }

                    "fail" -> {
                        reads.fail(stepRead(step))
                    }

                    else -> {
                        throw AssertionError("`$name` is no step")
                    }
                }
                reads.settle()
                snapshots += ItineraryReads.shown(engine.state.value, characteristics = answered != null)
            }
        } finally {
            engine.close()
            reads.drain()
            reads.settle()
        }
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "requests" to reads.requests,
                    "snapshots" to FixtureValue.Array(snapshots),
                ),
            ),
        )
    }
}

/** The read a step names, by the order it was sent in. */
internal fun stepRead(step: FixtureValue.Object): Int =
    wholeOrNull(step, "request") ?: throw AssertionError("the step names no `request`")
