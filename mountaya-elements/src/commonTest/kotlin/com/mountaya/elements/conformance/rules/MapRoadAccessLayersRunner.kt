package com.mountaya.elements

import com.mountaya.client.generated.TileSchema
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import com.mountaya.geo.LayerRank
import com.mountaya.geo.RoadAccessKind
import com.mountaya.geo.roadAccessColor
import com.mountaya.geo.roadAccessGlyph
import kotlinx.coroutines.test.TestScope

/**
 * `map.road-access.layers`: the layers an itinerary source's restricted stretches are drawn on, through the
 * internal rule the engine applies, each with its rank and the layer as MapLibre's style specification writes
 * it. The constants are held to the rule's own: the kinds most severe first, each kind's colour and glyph, the
 * stripe image's head, the stripes' opacity, the icons' size, the width of a line stating none, the source layer
 * and the two bands. A plain map draws the same layers beside the itinerary it is handed, with the host's word
 * on whether the stretches are shown, which the runner holds the engine to.
 */
object MapRoadAccessLayersRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        MapRunnerFixtures.holdConstants(
            case,
            mapOf(
                "iconSize" to FixtureValue.Number(ROAD_ACCESS_ICON_SIZE),
                "kindStyles" to
                    FixtureValue.Object(
                        RoadAccessKind.entries.map { kind ->
                            kind.id to
                                FixtureValue.Object(
                                    listOf(
                                        "color" to FixtureValue.Text(roadAccessColor(kind).hex),
                                        "glyph" to FixtureValue.Text(roadAccessGlyph(kind)),
                                    ),
                                )
                        },
                    ),
                "kinds" to MapRunnerFixtures.texts(RoadAccessKind.entries.map { it.id }),
                "ranks" to
                    FixtureValue.Object(
                        listOf(
                            "icons" to FixtureValue.Number(LayerRank.ItineraryPoint.rank.toDouble()),
                            "stripes" to FixtureValue.Number(LayerRank.ItineraryRestriction.rank.toDouble()),
                        ),
                    ),
                "sourceLayer" to FixtureValue.Text(ITINERARY_RESTRICTION_SOURCE_LAYER),
                "stripeImagePrefix" to FixtureValue.Text(ROAD_ACCESS_STRIPE_IMAGE_PREFIX),
                "stripeOpacity" to FixtureValue.Number(ROAD_ACCESS_STRIPE_OPACITY),
                "unstatedLineWidth" to FixtureValue.Number(UNSTATED_LINE_WIDTH),
            ),
        )
        val input = case.fields
        val source =
            MapRunnerFixtures.json.decodeFromJsonElement(
                TileSchema.TileSource.serializer(),
                MapRunnerFixtures.json(input["source"] ?: FixtureValue.Null),
            )
        val shown = input.bool("shown")
        val layers = roadAccessLayersOf(source, shown)
        val map = MapEngine(MapRunnerFixtures.client(), geolocation = InertGeolocation())
        map.setAccessRestrictionsVisibility(shown)
        map.setSpecification(TileSchema.TileSpecification(listOf(source)), "itinerary")
        val drawn =
            map.state.value.layers
                .filter { it.sourceLayer == ITINERARY_RESTRICTION_SOURCE_LAYER }
        if (drawn != layers) throw AssertionError("the map drew other stretches: ${drawn.map { it.id }}")
        return ConformanceOutcome.Value(
            FixtureValue.Array(
                layers.map { layer ->
                    FixtureValue.Object(
                        listOf(
                            "layer" to MapRunnerFixtures.styleLayer(layer),
                            "rank" to FixtureValue.Number(layer.rank.toDouble()),
                        ),
                    )
                },
            ),
        )
    }
}
