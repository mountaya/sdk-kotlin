package com.mountaya.conformance

import com.mountaya.client.generated.TileSchema
import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapLayer
import com.mountaya.elements.ScriptedMapSurface
import com.mountaya.geo.LayerRank
import kotlinx.coroutines.test.TestScope

/**
 * `map.query.feature-shape`: what a point query answers for the features a renderer found, through the
 * engine's `featuresAt` on the scripted surface: each feature the case's renderer found is built through
 * `QueriedFeature`'s published constructor, the shape's rule, every layer that drew one placed by the map,
 * and the query answers them as found.
 */
object MapQueryFeatureShapeRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val rendered = MapFixtures.features(case.fields, "rendered")
        val map = MapEngine(MapFixtures.client(), geolocation = InertGeolocation())
        val surface = ScriptedMapSurface(map)
        for (layer in rendered.map { it.layer }.distinct()) {
            map.addLayer(
                MapLayer(layer, TileSchema.TileLayerType.Line, "placed", rank = LayerRank.Overlay.rank),
            )
        }
        surface.rendered = rendered
        return ConformanceOutcome.Value(FixtureValue.Array(map.featuresAt(listOf(0.0, 0.0)).map(MapFixtures::answer)))
    }
}
