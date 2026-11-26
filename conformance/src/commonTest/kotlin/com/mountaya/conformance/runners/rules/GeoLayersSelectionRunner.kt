package com.mountaya.conformance

import com.mountaya.geo.LayersSelectionEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent

/**
 * `geo.layers.selection`: plays the case's `steps` over one selection driving a map that answers every command at
 * once, the catalogue it reads as it starts held until the `catalogue` step lands it, and answers every command the
 * map was given and what the selection holds after each step. The selection runs in the case's background scope, so
 * each step is settled by running what it queued, never by a clock.
 */
@OptIn(ExperimentalCoroutinesApi::class)
object GeoLayersSelectionRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val written = input.objectOrNull("catalogue") ?: throw AssertionError("the case names no `catalogue`")
        val maps = GeoControlFixtures.SelectionMaps(GeoControlFixtures.catalogueBySlug(written), answersAtOnce = true)
        val map = maps.map("map", input.textOrNull("shows"))
        val engine = LayersSelectionEngine(scope.backgroundScope, map)
        val snapshots = mutableListOf<FixtureValue>()
        try {
            scope.runCurrent()
            for (item in input.items("steps")) {
                val step = item as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
                when (val name = step.text("step")) {
                    "catalogue" -> maps.land(0)
                    "shows" -> map.terrain = step.textOrNull("terrain")
                    "terrain" -> engine.setTerrain(step.text("slug"))
                    "overlay" -> engine.setOverlay(step.textOrNull("slug"))
                    "enrichment" -> engine.toggleEnrichment(step.text("slug"))
                    else -> throw AssertionError("`$name` is no step")
                }
                scope.runCurrent()
                snapshots += GeoControlFixtures.selectionShown(engine.state.value, refusal = false)
            }
        } finally {
            engine.close()
            maps.release()
            scope.runCurrent()
        }
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "calls" to maps.written(naming = false),
                    "snapshots" to FixtureValue.Array(snapshots),
                ),
            ),
        )
    }
}
