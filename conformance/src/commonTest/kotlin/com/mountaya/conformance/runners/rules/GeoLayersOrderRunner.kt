package com.mountaya.conformance

import com.mountaya.geo.LayerRank
import com.mountaya.geo.poiSourceLayers
import com.mountaya.geo.tileLayerRank
import com.mountaya.geo.tileSourceLayerRanks
import kotlinx.coroutines.test.TestScope

/**
 * `geo.layers.order`: the rank the case's tile `layer` draws at. The rank table, the twelve bands,
 * each by the kebab-case name of its [LayerRank] member, bottom to top, and the points of interest's
 * source layers are held to the ones Geo publishes: `poiSourceLayerNames` as the list in its order, and
 * `poiSourceLayers` as the set of its members, whose order says nothing.
 */
object GeoLayersOrderRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            rules = emptyMap(),
            sets = mapOf("poiSourceLayers" to poiSourceLayers.toSet()),
            values =
                mapOf(
                    "layerRankBands" to
                        FixtureValue.Object(
                            LayerRank.entries.map { band ->
                                kebab(band.name) to
                                    FixtureValue.Number(band.rank.toDouble())
                            },
                        ),
                    "poiSourceLayerNames" to textArray(poiSourceLayers),
                    "tileSourceLayerRanks" to
                        FixtureValue.Object(
                            tileSourceLayerRanks.map { (layer, rank) ->
                                layer to
                                    FixtureValue.Number(rank.toDouble())
                            },
                        ),
                ),
        )
        val layer = case.fields.objectOrNull("layer") ?: throw AssertionError("the case has no `layer`")
        return ConformanceOutcome.Value(FixtureValue.Number(tileLayerRank(fixtureTileLayer(layer)).toDouble()))
    }

    /** [name], an UpperCamelCase member's, in kebab-case, the way the fixture names a band: `ItineraryLine` is `itinerary-line`. */
    private fun kebab(name: String): String = name.replace(Regex("(?<=[a-z0-9])(?=[A-Z])"), "-").lowercase()
}
