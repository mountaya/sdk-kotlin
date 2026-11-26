package com.mountaya.conformance

import com.mountaya.client.generated.DataSchema
import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.geo.FEATURE_INDEX_PROPERTY
import kotlinx.coroutines.test.TestScope

/**
 * `map.analysis.slots`: which Data API results a map draws after each step, through the engine: `add` is the
 * kind's add of a result handed over as it is, `pois` is `setPointsOfInterest` of one, `remove` is
 * `removeAnalysis` of the id a step names or of the one its name was added under, a name nothing was added under
 * being an id nothing is drawn under, and `clear` is the kind's clear. Each result is read off the GeoJSON source
 * the state draws it from, `mountaya-` then its id, as the index of each feature it draws, under its step's name, or
 * under the id it is drawn under where its step states none.
 */
object MapAnalysisSlotsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val map = MapEngine(MapFixtures.client(), geolocation = InertGeolocation())
        val ids = mutableListOf<Pair<String, String>>()
        val answers = mutableListOf<FixtureValue>()
        for (item in case.fields.items("steps")) {
            val step = item as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
            when (val name = step.text("step")) {
                "add" -> {
                    val id = MapResultFixtures.add(step.text("kind"), step["result"] ?: FixtureValue.Null, map)
                    ids += (step.textOrNull("name") ?: id) to id
                }

                "pois" -> {
                    map.setPointsOfInterest(
                        MapResultFixtures.decoded(
                            step["result"] ?: FixtureValue.Null,
                            DataSchema.PoisResult.serializer(),
                        ),
                    )
                    ids.removeAll { it.second == MapEngine.poisAnalysisId }
                    ids += (step.textOrNull("name") ?: MapEngine.poisAnalysisId) to MapEngine.poisAnalysisId
                }

                "remove" -> {
                    val id =
                        step.textOrNull("id")
                            ?: ids.lastOrNull { it.first == step.text("name") }?.second
                            ?: "analysis:none:0"
                    map.removeAnalysis(id)
                }

                "clear" -> {
                    MapResultFixtures.clear(step.text("kind"), map)
                }

                else -> {
                    throw AssertionError("no step is called `$name`")
                }
            }
            answers +=
                MapResultFixtures.drawn(ids, map) { features ->
                    FixtureValue.Array(
                        features.map { feature ->
                            feature.properties[FEATURE_INDEX_PROPERTY]?.let(::fixtureValueOf)
                                ?: FixtureValue.Null
                        },
                    )
                }
        }
        return ConformanceOutcome.Value(FixtureValue.Array(answers))
    }
}
