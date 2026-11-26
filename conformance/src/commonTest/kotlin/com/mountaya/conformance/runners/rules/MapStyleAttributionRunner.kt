package com.mountaya.conformance

import com.mountaya.client.generated.TileSchema
import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapLayer
import com.mountaya.elements.MapSource
import com.mountaya.elements.ScriptedMapSurface
import com.mountaya.geo.GeoJsonFeatureCollection
import com.mountaya.geo.LayerRank
import kotlinx.coroutines.test.TestScope

/**
 * `map.style.attribution`: what a map credits, through the engine's `credits`: each of the case's sources added
 * to the map with the credit it states, null and absent alike none, and drawn where the case says a layer draws
 * it, by a layer of the map's own drawing from it at every zoom. A source the case names by its `tileJson` is the
 * base map's, which the scripted surface reports as `MapView`'s renderer reads MapLibre Native's, its credit the
 * TileJSON's where the style states none. Mountaya's credit is held to the one a map drawing nothing credits, and
 * the credits are answered as a set, the order a renderer lists them in being its own.
 */
object MapStyleAttributionRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val bare = MapEngine(MapFixtures.client(), geolocation = InertGeolocation())
        val alone = bare.credits
        val mountaya = alone.singleOrNull() ?: throw AssertionError("a map drawing nothing credits $alone")
        holdDeclaredConstants(case, rules = emptyMap(), values = mapOf("mountayaCredit" to FixtureValue.Text(mountaya)))

        val map = MapEngine(MapFixtures.client(), geolocation = InertGeolocation())
        val surface = ScriptedMapSurface(map)
        val base = mutableListOf<ScriptedMapSurface.BaseSource>()
        for (written in case.fields.items("sources")) {
            val source = written as? FixtureValue.Object ?: throw AssertionError("a source is not an object")
            val id = source.text("id")
            val tileJson = source.objectOrNull("tileJson")
            if (tileJson != null) {
                // A source the style names by its TileJSON's address is the base map's, its credit the style's own
                // or, where it states none, its TileJSON's, as MapLibre Native reads it.
                val credit = source.textOrNull("attribution") ?: tileJson.textOrNull("attribution")
                base += ScriptedMapSurface.BaseSource(credit, source.bool("drawn"))
                continue
            }
            map.addSource(
                MapSource(
                    id,
                    MapSource.Kind.GeoJson(GeoJsonFeatureCollection(emptyList())),
                    attribution = source.textOrNull("attribution"),
                ),
            )
            if (source.bool("drawn")) {
                map.addLayer(
                    MapLayer(
                        "$id-drawn",
                        TileSchema.TileLayerType.Line,
                        id,
                        rank = LayerRank.Overlay.rank,
                    ),
                )
            }
        }
        surface.baseSources = base
        // The order a renderer lists its credits in is its own, so they are compared as a set.
        return ConformanceOutcome.Members(map.credits.map { FixtureValue.Text(it) })
    }
}
