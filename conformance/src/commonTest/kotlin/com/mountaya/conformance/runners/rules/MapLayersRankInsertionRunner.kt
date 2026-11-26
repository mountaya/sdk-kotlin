package com.mountaya.conformance

import com.mountaya.client.generated.TileSchema
import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapLayer
import kotlinx.coroutines.test.TestScope

/**
 * `map.layers.rank-insertion`: the order a renderer draws once every step is played, through `addLayer` and
 * `removeLayer`: the base map's own layers, which carry no rank and which `MapView`'s renderer keeps beneath
 * every layer the map places, then the layers the engine holds, bottom first.
 */
object MapLayersRankInsertionRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val base = fixtureTexts(input.items("base"), "`base`")
        val map = MapEngine(MapFixtures.client(), geolocation = InertGeolocation())
        for (item in input.items("steps")) {
            val step = item as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
            val id = step.text("id")
            when (val name = step.text("step")) {
                "place" -> {
                    val rank = wholeOrNull(step, "rank") ?: throw AssertionError("a place states no `rank`")
                    map.addLayer(
                        MapLayer(
                            id = id,
                            type = TileSchema.TileLayerType.Line,
                            source = "placed",
                            rank = rank,
                        ),
                    )
                }

                "remove" -> {
                    map.removeLayer(id)
                }

                else -> {
                    throw AssertionError("no step is called `$name`")
                }
            }
        }
        return ConformanceOutcome.Value(
            textArray(
                base +
                    map.state.value.layers
                        .map { it.id },
            ),
        )
    }
}
