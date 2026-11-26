package com.mountaya.conformance

import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * `map.query.elevation-at`: the elevation a map answers at the case's `lngLat`, through the engine's
 * `elevationAt` over a client whose exchange answers the Data API's terrain samples with the case's `answer`,
 * and what the map asked with: the locations of the query's input, and its selection read back off the
 * document the query sent once the persisted-query protocol asked for it, each field it names selected whole.
 * No surface is drawn, so no drawn terrain is there to read.
 */
object MapQueryElevationAtRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val answer = input["answer"] ?: throw AssertionError("the input states no `answer`")
        val exchange = MapFixtures.Recording(MapFixtures.Recording.Answer.Data("terrainSamples", answer))
        val map = MapEngine(MapFixtures.client(exchange = exchange), geolocation = InertGeolocation())
        val elevation = map.elevationAt(fixturePosition(input["lngLat"], "lngLat"))

        val body = exchange.sent.lastOrNull()?.body ?: throw AssertionError("the map asked the Data API nothing")
        val read =
            Json.parseToJsonElement(body.decodeToString()) as? JsonObject
                ?: throw AssertionError("the query sent no object")
        val document =
            (read["query"] as? JsonPrimitive)?.takeIf { it.isString }?.content
                ?: throw AssertionError("the query sent no document")
        val locations =
            ((read["variables"] as? JsonObject)?.get("input") as? JsonObject)?.get("locations")
                ?: throw AssertionError("the query sent no locations")
        return ConformanceOutcome.Value(
            MapFixtures.objectOf(
                "asked" to
                    MapFixtures.objectOf(
                        "locations" to fixtureValueOf(locations),
                        "select" to MapFixtures.selection(document, "terrainSamples"),
                    ),
                "elevation" to (elevation?.let { FixtureValue.Number(it) } ?: FixtureValue.Null),
            ),
        )
    }
}
