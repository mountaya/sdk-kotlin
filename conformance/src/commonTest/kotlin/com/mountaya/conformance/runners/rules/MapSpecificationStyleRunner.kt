package com.mountaya.conformance

import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapException
import kotlinx.coroutines.test.TestScope

/**
 * `map.specification.style`: what a map draws of a tile specification, through `setSpecification`: each layer
 * the case hid hidden first, by its id, then the specification drawn, and the state's sources and layers
 * written as MapLibre's style specification writes them. The restricted-stretch pair the map draws beside an
 * itinerary line of its own is no layer of the specification, and is left out (`map.road-access.layers` reads
 * it). A refused specification draws nothing, which the runner holds to.
 */
object MapSpecificationStyleRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val specification = MapContentFixtures.decodedSpecification(input["specification"] ?: FixtureValue.Null)
        val hidden = fixtureTexts(input.items("hidden"), "`hidden`")
        val map = MapEngine(MapFixtures.client(), geolocation = InertGeolocation())
        for (id in hidden) map.setLayerVisibility(id, false)
        try {
            map.setSpecification(specification, "specification")
        } catch (refused: MapException) {
            if (map.state.value.layers
                    .isNotEmpty() ||
                map.state.value.sources
                    .isNotEmpty()
            ) {
                throw AssertionError("a refused specification drew ${map.state.value.layers}")
            }
            return ConformanceOutcome.Value(
                MapFixtures.objectOf(
                    "layers" to FixtureValue.Array(emptyList()),
                    "refused" to FixtureValue.Text(MapFixtures.kind(refused)),
                    "sources" to FixtureValue.Object(emptyList()),
                ),
            )
        }
        val listed = specification.sources.flatMap { source -> source.layers.map { it.id } }
        val layers =
            map.state.value.layers
                .filter { it.id in listed }
        return ConformanceOutcome.Value(
            MapFixtures.objectOf(
                "layers" to FixtureValue.Array(layers.map(MapContentFixtures::styleLayer)),
                "refused" to FixtureValue.Null,
                "sources" to
                    FixtureValue.Object(
                        map.state.value.sources
                            .map { it.id to MapContentFixtures.styleSource(it) },
                    ),
            ),
        )
    }
}
