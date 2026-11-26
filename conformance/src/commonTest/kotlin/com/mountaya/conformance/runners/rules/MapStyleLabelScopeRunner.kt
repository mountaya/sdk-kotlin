package com.mountaya.conformance

import com.mountaya.client.generated.RestSchema
import com.mountaya.client.generated.TileSchema
import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapLayer
import com.mountaya.elements.ScriptedMapSurface
import com.mountaya.geo.LayerRank
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.JsonPrimitive

/**
 * `map.style.label-scope`: which labels a map's label language relabels, through the engine's
 * `setLabelLanguage` on the scripted surface, which draws the case's `base` labels and records each label the
 * engine rewrites: the map's own labels placed as symbol layers of a specification's source, then the language
 * set; where the case switches base map, the surface's labels become the ones the switch brings in, a style
 * loads, and what it rewrites then is the answer after the switch.
 */
object MapStyleLabelScopeRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val language = RestSchema.Language(input.text("language"))

        fun labels(key: String): List<ScriptedMapSurface.Label>? =
            if (input[key] == null) {
                null
            } else {
                input.items(key).map { written ->
                    val layer = written as? FixtureValue.Object ?: throw AssertionError("a label is not an object")
                    ScriptedMapSurface.Label(layer.text("id"), JsonPrimitive(layer.text("textField")))
                }
            }
        val base = labels("base").orEmpty()
        val placed = labels("placed").orEmpty()
        val switchTo = labels("switchTo")

        val map =
            MapEngine(
                MapFixtures.client(),
                labelLanguage = RestSchema.Language.En,
                geolocation = InertGeolocation(),
            )
        val surface = ScriptedMapSurface(map)
        surface.baseLabels = base
        for (label in placed) {
            map.addLayer(
                MapLayer(
                    id = label.id,
                    type = TileSchema.TileLayerType.Symbol,
                    source = "placed",
                    sourceLayer = "itinerary_points",
                    rank = LayerRank.ItineraryPoint.rank,
                    layout = mapOf("text-field" to label.textField),
                ),
            )
        }
        map.setLabelLanguage(language)
        val relabelled = surface.relabelled
        var afterSwitch: FixtureValue = FixtureValue.Null
        if (switchTo != null) {
            surface.baseLabels = switchTo
            surface.loadStyle()
            afterSwitch = textArray(surface.relabelled.drop(relabelled.size))
        }
        return ConformanceOutcome.Value(
            MapFixtures.objectOf("afterSwitch" to afterSwitch, "relabelled" to textArray(relabelled)),
        )
    }
}
