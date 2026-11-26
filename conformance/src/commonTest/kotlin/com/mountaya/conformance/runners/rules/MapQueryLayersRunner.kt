package com.mountaya.conformance

import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.QueriedFeature
import com.mountaya.elements.ScriptedMapSurface
import com.mountaya.geo.LayerRank
import kotlinx.coroutines.test.TestScope

/**
 * `map.query.layers`: which layers each point query asks the renderer about, through the engine's
 * `featuresAt`, `itinerariesAt`, `poisAt` and `zonesAt` on the scripted surface, which records the layers each
 * query asked: the case's layers placed at their ranks, the live style holding its `live` ids. A query that
 * asks nothing is null. The bands are held to `LayerRank`'s.
 */
object MapQueryLayersRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            rules = emptyMap(),
            values =
                mapOf(
                    "bands" to
                        MapFixtures.objectOf(
                            "itineraryLine" to FixtureValue.Number(LayerRank.ItineraryLine.rank.toDouble()),
                            "itineraryPoint" to FixtureValue.Number(LayerRank.ItineraryPoint.rank.toDouble()),
                            "poi" to FixtureValue.Number(LayerRank.Poi.rank.toDouble()),
                            "zone" to FixtureValue.Number(LayerRank.Zone.rank.toDouble()),
                        ),
                ),
        )
        val input = case.fields
        val live = fixtureTexts(input.items("live"), "`live`")
        val map = MapEngine(MapFixtures.client(), geolocation = InertGeolocation())
        val surface = ScriptedMapSurface(map)
        MapFixtures.place(input, map)
        surface.liveLayers = live

        fun asked(query: (List<Double>) -> List<QueriedFeature>): FixtureValue {
            val before = surface.queries.size
            if (query(listOf(0.0, 0.0)).isNotEmpty()) throw AssertionError("a query found a feature nothing drew")
            return if (surface.queries.size > before) textArray(surface.queries.last()) else FixtureValue.Null
        }
        return ConformanceOutcome.Value(
            MapFixtures.objectOf(
                "features" to asked(map::featuresAt),
                "itineraries" to asked(map::itinerariesAt),
                "pois" to asked(map::poisAt),
                "zones" to asked(map::zonesAt),
            ),
        )
    }
}
