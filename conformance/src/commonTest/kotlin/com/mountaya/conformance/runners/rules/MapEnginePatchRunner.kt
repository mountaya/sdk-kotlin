package com.mountaya.conformance

import com.mountaya.client.generated.TileSchema
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapException
import com.mountaya.elements.MapLayer
import com.mountaya.geo.LayerRank
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * `map.engine.patch`: what a map holds once a patch is merged onto it, through the public engine. The
 * map draws a layer whose paint is the case's `held`, the patch is `MapEngine.editLayer`'s paint, each
 * member the case lists as `unstated` mapped to Kotlin's `null`, which JSON cannot write, and the
 * answer is the layer's paint as the engine's state then holds it. A case whose `drawn` is false
 * patches a layer the map does not draw: the engine's `MapException` of kind `LayerNotFound` is the
 * contract's `not-found`, held to leave the map as it was. A case that states `spelled` patches with each
 * of its members too, read from its JSON text by `Json.parseToJsonElement`, which keeps a number's text as
 * written, and is answered with what the map `holds` and whether it `changed`, its state compared before and
 * after as the map compares it.
 */
object MapEnginePatchRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val held = input.objectOrNull("held") ?: throw AssertionError("the case has no `held`")
        val stated = input.objectOrNull("patch") ?: throw AssertionError("the case has no `patch`")
        val unstated = input["unstated"]?.let { fixtureTexts((it as? FixtureValue.Array)?.items.orEmpty(), "unstated") }
        // Each member `spelled` writes as JSON text, read by the reader the map's own values are read with, which keeps
        // a number's text as written.
        val spelled = input.objectOrNull("spelled")
        val patch: Map<String, JsonElement?> =
            buildMap {
                for (name in unstated.orEmpty()) put(name, null)
                for ((name, value) in stated.entries) put(name, fixtureJson(value))
                for ((name, value) in spelled?.entries.orEmpty()) {
                    val text =
                        (value as? FixtureValue.Text)?.value ?: throw AssertionError("`$name` is spelled as no text")
                    put(name, Json.parseToJsonElement(text))
                }
            }

        val map = MapEngine(Scripted.client(scope, Scripted.Exchange(scope, emptyList())))
        map.addLayer(
            MapLayer(
                id = "held",
                type = TileSchema.TileLayerType.Line,
                source = "held",
                rank = LayerRank.ItineraryLine.rank,
                paint = held.entries.associate { (name, value) -> name to fixtureJson(value) },
            ),
        )
        val drawn = (input["drawn"] as? FixtureValue.Bool)?.value ?: true
        if (!drawn) {
            val before = map.state.value
            val refused =
                try {
                    map.editLayer("not-drawn", paint = patch)
                    null
                } catch (refusal: MapException) {
                    refusal.kind
                }
            val after = map.state.value
            map.close()
            if (refused != MapException.Kind.LayerNotFound) {
                throw AssertionError("a patch of a layer the map does not draw answered $refused")
            }
            if (after != before) throw AssertionError("a refused patch changed what the map holds")
            return ConformanceOutcome.Refused("not-found")
        }
        val before = map.state.value
        map.editLayer("held", paint = patch)
        val after = map.state.value
        val layer = after.layers.singleOrNull() ?: throw AssertionError("the map holds no single layer")
        map.close()
        val holds = fixtureValueOf(JsonObject(layer.paint))
        if (spelled == null) return ConformanceOutcome.Value(holds)
        // Whether the merge changed what the map holds, as the map compares it, which is what its state tells.
        return ConformanceOutcome.Value(
            FixtureValue.Object(listOf("changed" to FixtureValue.Bool(after != before), "holds" to holds)),
        )
    }
}
