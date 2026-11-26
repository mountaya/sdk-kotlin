package com.mountaya.conformance

import com.mountaya.geo.LayersSelectionEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent

/**
 * `geo.layers.selection-outcome`: plays the case's `steps` over one selection and the named maps of its `maps`, every
 * command and catalogue read held until a step settles it, numbered across maps, a read landed by `catalogue` and
 * failed by `fail`, and answers every command a map was
 * given and what the selection holds after each step, whether a refusal stands among it. Clearing the overlay and
 * removing an enrichment are commands like any other, held open until a step settles them, a refused one keeping
 * what was asked.
 */
@OptIn(ExperimentalCoroutinesApi::class)
object GeoLayersSelectionOutcomeRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val written = input.objectOrNull("catalogue") ?: throw AssertionError("the case names no `catalogue`")
        val named = input.objectOrNull("maps") ?: throw AssertionError("the case names no `maps`")
        val maps = GeoControlFixtures.SelectionMaps(GeoControlFixtures.catalogueBySlug(written), answersAtOnce = false)
        val engine = LayersSelectionEngine(scope.backgroundScope)
        val snapshots = mutableListOf<FixtureValue>()
        try {
            for (item in input.items("steps")) {
                val step = item as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
                when (val name = step.text("step")) {
                    "map" -> {
                        val map =
                            step.textOrNull("map")?.let { mapName ->
                                val shown =
                                    named.objectOrNull(mapName)
                                        ?: throw AssertionError("the case names no map `$mapName`")
                                maps.map(mapName, shown.textOrNull("shows"))
                            }
                        engine.configure(map)
                    }

                    "catalogue" -> {
                        maps.land(wholeOrNull(step, "read") ?: throw AssertionError("the step names no `read`"))
                    }

                    "fail" -> {
                        maps.fail(wholeOrNull(step, "read") ?: throw AssertionError("the step names no `read`"))
                    }

                    "terrain" -> {
                        engine.setTerrain(step.text("slug"))
                    }

                    "overlay" -> {
                        engine.setOverlay(step.textOrNull("slug"))
                    }

                    "enrichment" -> {
                        engine.toggleEnrichment(step.text("slug"))
                    }

                    "accept", "refuse" -> {
                        maps.settle(
                            wholeOrNull(step, "call") ?: throw AssertionError("the step names no `call`"),
                            name == "refuse",
                        )
                    }

                    else -> {
                        throw AssertionError("`$name` is no step")
                    }
                }
                scope.runCurrent()
                snapshots += GeoControlFixtures.selectionShown(engine.state.value, refusal = true)
            }
        } finally {
            engine.close()
            maps.release()
            scope.runCurrent()
        }
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "calls" to maps.written(naming = true),
                    "snapshots" to FixtureValue.Array(snapshots),
                ),
            ),
        )
    }
}
