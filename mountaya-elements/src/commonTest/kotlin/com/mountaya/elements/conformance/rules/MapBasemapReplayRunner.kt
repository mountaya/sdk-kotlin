package com.mountaya.elements

import com.mountaya.client.Url
import com.mountaya.client.generated.TileSchema
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import com.mountaya.geo.GeoJsonFeatureCollection
import kotlinx.coroutines.test.TestScope

/**
 * `map.basemap.replay`: what a base-map switch draws, through the engine on the scripted surface, the map built
 * on the case's `base` and the surface loading it: `draw` is `setSpecification` of one vector source holding
 * the step's layers, `clear` is `removeSpecification`, `hide` is `setLayerVisibility`, `addByHand` is `addLayer` of
 * the caller's own layer over a source of the caller's own, added by `addSource`, and `switch` asks for
 * each base map of `to` in order, `setStyle` to a document of its own, then has the surface load the latest or
 * fail it as `arrives` says. The state is what `MapView`'s renderer draws again on every style it loads: `base`
 * is the base map its style is, `drawn` its layers bottom first, and `hidden` those drawn hidden. The
 * renderer's own replay onto MapLibre's map is proven on the emulator.
 */
object MapBasemapReplayRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        if (case.fixture.objectOrNull("constants") !=
            null
        ) {
            throw AssertionError("the fixture declares constants no rule here states")
        }
        val input = case.fields
        val bases = input.objectOrNull("bases")?.keys.orEmpty()

        fun url(name: String) = Url("https://tiles.test/$name.json")
        val map =
            MapEngine(MapRunnerFixtures.client(), style = url(input.text("base")), geolocation = InertGeolocation())
        val surface = ScriptedMapSurface(map)
        surface.loadStyle()
        for (item in input.items("steps")) {
            val step = item as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
            when (val name = step.text("step")) {
                "draw" -> {
                    val content = step.text("content")
                    map.setSpecification(MapRunnerFixtures.specification(content, step.items("layers")), content)
                }

                "clear" -> {
                    map.removeSpecification(step.text("content"))
                }

                "hide" -> {
                    map.setLayerVisibility(step.text("layer"), false)
                }

                "addByHand" -> {
                    // A layer of the caller's own, drawing from a source of the caller's own, added as a caller adds
                    // one by hand.
                    val layer = step.objectOrNull("layer") ?: throw AssertionError("the step adds no `layer`")
                    val source = step.text("source")
                    map.addSource(MapSource(source, MapSource.Kind.GeoJson(GeoJsonFeatureCollection(emptyList()))))
                    map.addLayer(
                        MapLayer(
                            layer.text("id"),
                            TileSchema.TileLayerType(layer.text("type")),
                            source,
                            rank = step.number("rank").toInt(),
                        ),
                    )
                }

                "switch" -> {
                    for (base in MapRunnerFixtures.textsOf(step.items("to"))) map.setStyle(url(base))
                    if (step.bool("arrives")) surface.loadStyle() else surface.failStyle()
                }

                else -> {
                    throw AssertionError("no step is called `$name`")
                }
            }
        }
        val shown =
            bases.firstOrNull { url(it) == map.state.value.style }
                ?: throw AssertionError("the map shows no base map the case names")
        val drawn = map.state.value.layers
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "base" to FixtureValue.Text(shown),
                    "drawn" to MapRunnerFixtures.texts(drawn.map { it.id }),
                    "hidden" to MapRunnerFixtures.texts(drawn.filter { it.isHidden }.map { it.id }),
                ),
            ),
        )
    }
}
