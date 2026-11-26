package com.mountaya.conformance

import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapException
import kotlinx.coroutines.test.TestScope

/**
 * `map.overlays.composition`: which overlays a map draws after each step, over a catalogue of the case's
 * classic and enrichment overlays, each drawing one fill layer named after its slug: `overlay` is `setOverlay`,
 * or `clearOverlay` for null, `add` is `addEnrichment`, and `remove` is `removeEnrichment`. `overlay` is the
 * classic overlay whose layer the state holds, and `enrichments` the engine's `enrichments`.
 */
object MapOverlaysCompositionRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val overlays = input.objectOrNull("overlays") ?: throw AssertionError("the case states no `overlays`")
        val classic = fixtureTexts(overlays.items("classic"), "`classic`")
        val enrichment = fixtureTexts(overlays.items("enrichment"), "`enrichment`")
        val body = MapContentFixtures.catalogueBody(classic = classic, enrichment = enrichment)
        val map =
            MapEngine(
                MapFixtures.client(exchange = MapContentFixtures.Serving { 200 to body }),
                geolocation = InertGeolocation(),
            )
        val answers = mutableListOf<FixtureValue>()
        for (item in input.items("steps")) {
            val step = item as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
            var refused: FixtureValue = FixtureValue.Null
            try {
                when (val name = step.text("step")) {
                    "overlay" -> step.textOrNull("slug")?.let { map.setOverlay(it) } ?: map.clearOverlay()
                    "add" -> map.addEnrichment(step.text("slug"))
                    "remove" -> map.removeEnrichment(step.text("slug"))
                    else -> throw AssertionError("no step is called `$name`")
                }
            } catch (error: MapException) {
                refused = FixtureValue.Text(MapFixtures.kind(error))
            }
            val drawn =
                classic.filter { slug ->
                    map.state.value.layers
                        .any { it.id == "$slug-layer" }
                }
            if (drawn.size > 1) throw AssertionError("two classic overlays are drawn: $drawn")
            answers +=
                MapFixtures.objectOf(
                    "enrichments" to textArray(map.enrichments),
                    "overlay" to (drawn.firstOrNull()?.let { FixtureValue.Text(it) } ?: FixtureValue.Null),
                    "refused" to refused,
                )
        }
        return ConformanceOutcome.Value(FixtureValue.Array(answers))
    }
}
