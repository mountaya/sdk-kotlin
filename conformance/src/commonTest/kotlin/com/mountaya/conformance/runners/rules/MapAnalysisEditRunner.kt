package com.mountaya.conformance

import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent

/**
 * `map.analysis.edit`: how an edit of a drawn result asks again, over a client that answers every query at
 * once: isochrones with a band per value of the query's `range`, and suggestions with three candidates. `add`
 * draws from `input`, which it asks for, or from `result`, handed over; `edit` is the kind's edit with a patch
 * applying the members the case's `patch` states to the input held, the members it names `unstated` left as
 * held, `{}` being no patch at all, and the draw options it states. `queries` is each query the client was
 * asked, read off the full document each sends after its persisted digest is refused. Every input and every
 * query is read as the case writes it.
 */
@OptIn(ExperimentalCoroutinesApi::class)
object MapAnalysisEditRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val exchange = MapResultFixtures.Queries(holding = false)
        val map = MapEngine(MapFixtures.client(exchange = exchange), geolocation = InertGeolocation())
        val ids = mutableListOf<Pair<String, String>>()
        val refused = mutableListOf<FixtureValue>()
        for (item in case.fields.items("steps")) {
            val step = item as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
            try {
                when (val name = step.text("step")) {
                    "add" -> {
                        val added = step.text("name")
                        val kind = step.text("kind")
                        val input = step["input"]
                        ids += added to
                            if (input != null) {
                                MapResultFixtures.ask(kind, input, map)
                            } else {
                                MapResultFixtures.add(kind, step["result"] ?: FixtureValue.Null, map)
                            }
                    }

                    "edit" -> {
                        val edited = step.text("name")
                        val id = ids.lastOrNull { it.first == edited }?.second ?: "analysis:none:0"
                        val unstated =
                            step["unstated"]
                                ?.let {
                                    fixtureTexts((it as? FixtureValue.Array)?.items.orEmpty(), "`unstated`")
                                }.orEmpty()
                        val stated =
                            step
                                .objectOrNull("patch")
                                ?.entries
                                .orEmpty()
                                .filter { it.first !in unstated }
                        MapResultFixtures.edit(step.text("kind"), id, stated, step.objectOrNull("options"), map)
                    }

                    else -> {
                        throw AssertionError("no step is called `$name`")
                    }
                }
                refused += FixtureValue.Null
            } catch (error: MapException) {
                refused += FixtureValue.Text(MapFixtures.kind(error))
            }
            scope.runCurrent()
        }
        return ConformanceOutcome.Value(
            MapFixtures.objectOf(
                "drawn" to MapResultFixtures.marks(ids, map),
                "queries" to FixtureValue.Array(exchange.queries.map { it.written }),
                "refused" to FixtureValue.Array(refused),
            ),
        )
    }
}
