package com.mountaya.conformance

import com.mountaya.client.generated.TileSchema
import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapException
import kotlinx.coroutines.test.TestScope

/**
 * `map.basemap.terrain`: which base map a map shows after each step, over a catalogue of the case's terrains:
 * `terrain` is `setTerrain` and `variant` `setTerrainVariant`. `style` is the document the state asks its
 * renderer for, `terrain` and `variant` the state's own.
 */
object MapBasemapTerrainRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val terrains =
            input.items("terrains").map { item ->
                val terrain = item as? FixtureValue.Object ?: throw AssertionError("a terrain is not an object")
                MapContentFixtures.terrain(terrain.text("slug"), terrain.text("light"), terrain.text("dark"))
            }
        val body = MapContentFixtures.catalogueBody(terrains = terrains)
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
                    "terrain" -> map.setTerrain(step.textOrNull("slug"))
                    "variant" -> map.setTerrainVariant(TileSchema.TerrainVariant(step.text("variant")))
                    else -> throw AssertionError("no step is called `$name`")
                }
            } catch (error: MapException) {
                refused = FixtureValue.Text(MapFixtures.kind(error))
            }
            val shown = map.state.value
            answers +=
                MapFixtures.objectOf(
                    "refused" to refused,
                    "style" to (shown.style?.let { FixtureValue.Text(it.toString()) } ?: FixtureValue.Null),
                    "terrain" to (shown.terrain?.let { FixtureValue.Text(it) } ?: FixtureValue.Null),
                    "variant" to FixtureValue.Text(shown.terrainVariant.value),
                )
        }
        return ConformanceOutcome.Value(FixtureValue.Array(answers))
    }
}
