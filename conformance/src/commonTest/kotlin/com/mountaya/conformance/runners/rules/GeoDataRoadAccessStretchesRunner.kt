package com.mountaya.conformance

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.roadAccessFeatures
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * `geo.data.road-access-stretches`: the pieces the case's `info` cuts from its `geometry`, each as
 * `{geometry, kind}`: the feature's line as RFC 7946 writes it, and its `kind` property. The geometry is
 * built as the fixture writes it, and `info` is read by REST's own decoder, as a consumer receives a
 * segment's `road_access_info`.
 */
object GeoDataRoadAccessStretchesRunner : ConformanceRunner {
    private val json = Json

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val geometry = input["geometry"]?.takeUnless { it is FixtureValue.Null }?.let(::fixtureGeometry)
        val info =
            input["info"]?.takeUnless { it is FixtureValue.Null }?.let {
                json.decodeFromJsonElement(ListSerializer(RestSchema.RoadAccessInfo.serializer()), fixtureJson(it))
            }
        val pieces =
            roadAccessFeatures(geometry, info).map { feature ->
                val line = feature.geometry ?: throw AssertionError("a piece holds no line")
                val kind = feature.properties["kind"] ?: throw AssertionError("a piece names no kind")
                FixtureValue.Object(listOf("geometry" to writtenGeometry(line), "kind" to fixtureValueOf(kind)))
            }
        return ConformanceOutcome.Value(FixtureValue.Array(pieces))
    }
}
