package com.mountaya.conformance

import com.mountaya.geo.LayersSelection
import com.mountaya.geo.LayersSelectionEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent

/**
 * `geo.layers.selection-instant`: plays the case's `steps` over one selection and the named maps of its `maps`, as
 * `geo.layers.selection-outcome`'s runner plays them, every command and catalogue read held until a step settles it,
 * numbered across maps, and `instant` pins the instant `at` through `setInstant`, a pin that names no instant handed as
 * none; it answers every command a map was given, a pin by its instant, and after each step the instant the selection
 * holds, its overlay and whether a refusal stands.
 */
@OptIn(ExperimentalCoroutinesApi::class)
object GeoLayersSelectionInstantRunner : ConformanceRunner {
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

                    "instant" -> {
                        engine.setInstant(TimeFixtures.instant(step.textOrNull("at")))
                    }

                    "overlay" -> {
                        engine.setOverlay(step.textOrNull("slug"))
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
                snapshots += shown(engine.state.value)
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

    /** What the selection holds as the fixture writes it: its instant, its overlay and whether a refusal stands. */
    private fun shown(selection: LayersSelection): FixtureValue =
        FixtureValue.Object(
            listOf(
                "instant" to TimeFixtures.written(selection.instant),
                "overlay" to (selection.overlay?.let { FixtureValue.Text(it) } ?: FixtureValue.Null),
                "refused" to FixtureValue.Bool(selection.refusal != null),
            ),
        )
}
