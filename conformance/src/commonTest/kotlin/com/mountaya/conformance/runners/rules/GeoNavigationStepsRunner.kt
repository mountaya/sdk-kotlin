package com.mountaya.conformance

import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.readDirectionsSteps
import com.mountaya.geo.readItineraryDirectionsSteps
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * `geo.navigation.steps`: the case's `steps`, as the `surface` it names sends them, read into the one
 * vocabulary over a geometry of `coordinateCount` vertices. Each surface's steps are written as its
 * JSON, the Data API's `DirectionsStep` in camelCase and REST's `ItineraryDirectionsStep` in snake
 * case, and decoded by the wire's own serializer, so a maneuver neither spells reaches the adapter as
 * written. The answer is whether the list was refused and the steps read.
 */
object GeoNavigationStepsRunner : ConformanceRunner {
    private val json = Json

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val coordinateCount =
            wholeOrNull(input, "coordinateCount") ?: throw AssertionError("the case has no `coordinateCount`")
        val written = input["steps"]?.takeUnless { it is FixtureValue.Null }?.let(::fixtureJson)
        val reading =
            when (val surface = input.text("surface")) {
                "data" -> {
                    val steps =
                        written?.let {
                            json.decodeFromJsonElement(ListSerializer(DataSchema.DirectionsStep.serializer()), it)
                        }
                    readDirectionsSteps(steps, coordinateCount)
                }

                "rest" -> {
                    val steps =
                        written?.let {
                            json.decodeFromJsonElement(
                                ListSerializer(RestSchema.ItineraryDirectionsStep.serializer()),
                                it,
                            )
                        }
                    readItineraryDirectionsSteps(steps, coordinateCount)
                }

                else -> {
                    throw AssertionError("`$surface` is no surface")
                }
            }
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "refused" to FixtureValue.Bool(reading.refused),
                    "steps" to FixtureValue.Array(reading.steps.map(::navigationStepValue)),
                ),
            ),
        )
    }
}
