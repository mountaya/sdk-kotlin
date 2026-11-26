package com.mountaya.conformance

import com.mountaya.client.Url
import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.ScriptedMapSurface
import kotlinx.coroutines.test.TestScope

/**
 * `map.layers.visibility`: a layer's visibility after each step, through the engine on the scripted surface:
 * `draw` is `setSpecification` of one vector source whose layers are symbols of `poi_food`, `clear` is
 * `removeSpecification`, `hide` and `show` are `setLayerVisibility`, and `switch` is `setStyle` to a document
 * of its own, which the surface then loads. `shown` is `isLayerVisible`; `drawn` is how the state the renderer
 * draws holds the layer, `none` or `visible`, or null where it holds none.
 */
object MapLayersVisibilityRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val watched = fixtureTexts(input.items("watched"), "`watched`")
        val map = MapEngine(MapFixtures.client(), geolocation = InertGeolocation())
        val surface = ScriptedMapSurface(map)
        var switches = 0
        val answers = mutableListOf<FixtureValue>()
        for (item in input.items("steps")) {
            val step = item as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
            when (val name = step.text("step")) {
                "draw" -> {
                    val content = step.text("content")
                    val layers =
                        fixtureTexts(step.items("layers"), "`layers`").map { id ->
                            MapFixtures.objectOf(
                                "id" to FixtureValue.Text(id),
                                "source_layer" to FixtureValue.Text("poi_food"),
                                "type" to FixtureValue.Text("symbol"),
                            )
                        }
                    map.setSpecification(MapContentFixtures.specification(content, layers), content)
                }

                "clear" -> {
                    map.removeSpecification(step.text("content"))
                }

                "hide" -> {
                    map.setLayerVisibility(step.text("layer"), false)
                }

                "show" -> {
                    map.setLayerVisibility(step.text("layer"), true)
                }

                "switch" -> {
                    switches += 1
                    map.setStyle(Url("https://tiles.test/base-$switches.json"))
                    surface.loadStyle()
                }

                else -> {
                    throw AssertionError("no step is called `$name`")
                }
            }
            answers +=
                FixtureValue.Object(
                    watched.map { id ->
                        val layer =
                            map.state.value.layers
                                .firstOrNull { it.id == id }
                        id to
                            MapFixtures.objectOf(
                                "drawn" to
                                    (
                                        layer?.let { FixtureValue.Text(if (it.isHidden) "none" else "visible") }
                                            ?: FixtureValue.Null
                                    ),
                                "shown" to FixtureValue.Bool(map.isLayerVisible(id)),
                            )
                    },
                )
        }
        return ConformanceOutcome.Value(FixtureValue.Array(answers))
    }
}
