package com.mountaya.elements

import com.mountaya.client.generated.DataSchema
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import com.mountaya.geo.FEATURE_KIND_PROPERTY
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * `map.analysis.requests`: how the queries of a drawn result are cancelled and answered, played over an exchange
 * that holds every query sending its full document until a step answers it, and fails one as cancelled the
 * moment its coroutine is cancelled. `add` is `addIsochrones` of `input`, `edit` `editIsochrones` with the case's
 * patch, `answer` lets the query `query` answer, `remove` is `removeAnalysis` and `clear` the kind's clear. Every
 * call runs in a coroutine of its own on the test's scheduler, and each step settles before the next. A call is
 * `cancelled` where it threw the `CancellationException` the engine cancels a query with.
 */
@OptIn(ExperimentalCoroutinesApi::class)
object MapAnalysisRequestsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        if (case.fixture.objectOrNull("constants") !=
            null
        ) {
            throw AssertionError("the fixture declares constants no rule here states")
        }
        val exchange = MapRunnerFixtures.IsochroneQueries()
        val map = MapEngine(MapRunnerFixtures.client(exchange), geolocation = InertGeolocation())
        val calls = mutableListOf<String>()
        val ids = mutableListOf<Pair<String, String>>()

        fun track(call: suspend () -> Unit) {
            val index = calls.size
            calls += "waiting"
            scope.launch {
                calls[index] =
                    try {
                        call()
                        "answered"
                    } catch (cancelled: CancellationException) {
                        "cancelled"
                    } catch (failed: Exception) {
                        "failed"
                    }
            }
        }
        for (item in case.fields.items("steps")) {
            val step = item as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
            when (val name = step.text("step")) {
                "add" -> {
                    val added = step.text("name")
                    val input = decoded(step["input"] ?: FixtureValue.Null)
                    track { ids += added to map.addIsochrones(input).id }
                }

                "edit" -> {
                    val edited = step.text("name")
                    val stated = step.objectOrNull("patch")?.entries.orEmpty()
                    val id = ids.lastOrNull { it.first == edited }?.second ?: "analysis:none:0"
                    track { map.editIsochrones(id, patched(stated)) }
                }

                "answer" -> {
                    val query =
                        step.numberOrNull("query")?.toInt() ?: throw AssertionError("an answer names no `query`")
                    exchange.answer(query)
                }

                "remove" -> {
                    val removed = step.text("name")
                    map.removeAnalysis(ids.lastOrNull { it.first == removed }?.second ?: "analysis:none:0")
                }

                "clear" -> {
                    when (val kind = step.text("kind")) {
                        "directions" -> map.clearDirections()
                        "suggestions" -> map.clearSuggestions()
                        "isochrones" -> map.clearIsochrones()
                        "places" -> map.clearPlaces()
                        "matrix" -> map.clearMatrix()
                        "pois" -> map.clearPointsOfInterest()
                        else -> throw AssertionError("no kind `$kind` is cleared")
                    }
                }

                else -> {
                    throw AssertionError("no step is called `$name`")
                }
            }
            scope.runCurrent()
        }
        val outcomes = calls.toList()
        val sent =
            exchange.queries.map { query ->
                FixtureValue.Object(
                    listOf(
                        "input" to MapRunnerFixtures.value(query.input),
                        "kind" to FixtureValue.Text("isochrones"),
                    ),
                )
            }
        val cancelled = exchange.queries.indices.filter { exchange.queries[it].cancelled }
        val drawn = marks(ids, map)
        exchange.release()
        scope.runCurrent()
        map.close()
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "calls" to MapRunnerFixtures.texts(outcomes),
                    "cancelled" to FixtureValue.Array(cancelled.map { FixtureValue.Number(it.toDouble()) }),
                    "drawn" to drawn,
                    "sent" to FixtureValue.Array(sent),
                ),
            ),
        )
    }

    private fun decoded(written: FixtureValue): DataSchema.IsochronesInput =
        MapRunnerFixtures.json.decodeFromJsonElement(
            DataSchema.IsochronesInput.serializer(),
            MapRunnerFixtures.json(written),
        )

    /** A patch writing each of [stated] onto the input held, through the input's own encoding, or null where it states none. */
    private fun patched(
        stated: List<Pair<String, FixtureValue>>,
    ): ((DataSchema.IsochronesInput) -> DataSchema.IsochronesInput)? {
        if (stated.isEmpty()) return null
        return { input ->
            val serializer = DataSchema.IsochronesInput.serializer()
            val members = (MapRunnerFixtures.json.encodeToJsonElement(serializer, input) as JsonObject).toMutableMap()
            for ((key, value) in stated) members[key] = MapRunnerFixtures.json(value)
            MapRunnerFixtures.json.decodeFromJsonElement(serializer, JsonObject(members))
        }
    }

    /** What each named result draws, an isochrone band's value, by the name it was added under. */
    private fun marks(
        ids: List<Pair<String, String>>,
        map: MapEngine,
    ): FixtureValue =
        FixtureValue.Object(
            map.state.value.sources.map { source ->
                val name =
                    ids.lastOrNull { "mountaya-${it.second}" == source.id }?.first
                        ?: throw AssertionError("the map draws `${source.id}`, which no step drew")
                val features =
                    (source.kind as? MapSource.Kind.GeoJson)?.features?.features
                        ?: throw AssertionError("`${source.id}` is no GeoJSON")
                name to
                    FixtureValue.Array(
                        features.map { feature ->
                            val kind = feature.properties[FEATURE_KIND_PROPERTY]
                            val mark =
                                if (kind ==
                                    JsonPrimitive("suggestion")
                                ) {
                                    feature.properties["chosen"]
                                } else {
                                    feature.properties["value"]
                                }
                            mark?.let(MapRunnerFixtures::value) ?: FixtureValue.Null
                        },
                    )
            },
        )
}
