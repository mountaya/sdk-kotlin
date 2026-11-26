package com.mountaya.conformance

import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapException
import com.mountaya.elements.MapMarker
import kotlinx.coroutines.test.TestScope

/**
 * `map.marker.lifecycle`: which markers a map pins after each step, through the engine: `add` is `addMarker`,
 * `edit` `editMarker` with each member the patch states, the ones it names `unstated` left null as a Kotlin
 * caller leaves a member it does not change, and `remove` `removeMarker`. The markers are the state's, bottom
 * first.
 */
object MapMarkerLifecycleRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val map = MapEngine(MapFixtures.client(), geolocation = InertGeolocation())
        val answers = mutableListOf<FixtureValue>()
        for (item in case.fields.items("steps")) {
            val step = item as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
            var refused: FixtureValue = FixtureValue.Null
            try {
                when (val name = step.text("step")) {
                    "add" -> {
                        val marker = step.objectOrNull("marker") ?: throw AssertionError("an add states no `marker`")
                        map.addMarker(
                            MapMarker(
                                marker.text("id"),
                                fixturePosition(marker["lngLat"], "lngLat"),
                                marker.textOrNull("color"),
                                marker.textOrNull("glyph"),
                                marker.textOrNull("label"),
                            ),
                        )
                    }

                    "edit" -> {
                        val patch = step.objectOrNull("patch") ?: FixtureValue.Object(emptyList())
                        val unstated =
                            step["unstated"]
                                ?.let {
                                    fixtureTexts((it as? FixtureValue.Array)?.items.orEmpty(), "`unstated`")
                                }.orEmpty()

                        fun stated(member: String): String? = if (member in unstated) null else patch.textOrNull(member)
                        map.editMarker(
                            step.text("id"),
                            position =
                                patch["lngLat"]?.takeUnless { "lngLat" in unstated }?.let {
                                    fixturePosition(
                                        it,
                                        "lngLat",
                                    )
                                },
                            color = stated("color"),
                            glyph = stated("glyph"),
                            label = stated("label"),
                        )
                    }

                    "remove" -> {
                        map.removeMarker(step.text("id"))
                    }

                    else -> {
                        throw AssertionError("no step is called `$name`")
                    }
                }
            } catch (error: MapException) {
                refused = FixtureValue.Text(MapFixtures.kind(error))
            }
            answers +=
                MapFixtures.objectOf(
                    "markers" to
                        FixtureValue.Array(
                            map.state.value.markers.map { marker ->
                                MapFixtures.objectOf(
                                    "color" to (marker.color?.let { FixtureValue.Text(it) } ?: FixtureValue.Null),
                                    "glyph" to (marker.glyph?.let { FixtureValue.Text(it) } ?: FixtureValue.Null),
                                    "id" to FixtureValue.Text(marker.id),
                                    "label" to (marker.label?.let { FixtureValue.Text(it) } ?: FixtureValue.Null),
                                    "lngLat" to MapFixtures.numbers(marker.position),
                                )
                            },
                        ),
                    "refused" to refused,
                )
        }
        return ConformanceOutcome.Value(FixtureValue.Array(answers))
    }
}
