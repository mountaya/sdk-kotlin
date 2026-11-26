package com.mountaya.conformance

import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import kotlinx.coroutines.test.TestScope

/**
 * `map.layers.keys`: which concepts a map draws, through `setSpecification`, each content a specification of
 * one vector source holding its layers, drawn under its content's name, then `setLayerVisibility` for each
 * layer hidden, and the engine's `drawnLayerKeys`, `layerIds` and `isKeyVisible`.
 */
object MapLayersKeysRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val band = wholeOrNull(input, "band") ?: throw AssertionError("the case states no `band`")
        val key = input.text("key")
        val map = MapEngine(MapFixtures.client(), geolocation = InertGeolocation())
        for (item in input.items("drawn")) {
            val entry = item as? FixtureValue.Object ?: throw AssertionError("a content is not an object")
            val content = entry.text("content")
            map.setSpecification(MapContentFixtures.specification(content, entry.items("layers")), content)
        }
        for (id in fixtureTexts(input.items("hidden"), "`hidden`")) map.setLayerVisibility(id, false)
        return ConformanceOutcome.Value(
            MapFixtures.objectOf(
                "keys" to textArray(map.drawnLayerKeys()),
                "keysInBand" to textArray(map.drawnLayerKeys(band)),
                "layerIds" to textArray(map.layerIds(key)),
                "shown" to FixtureValue.Bool(map.isKeyVisible(key)),
            ),
        )
    }
}
