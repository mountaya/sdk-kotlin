package com.mountaya.geo

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * `geo.layers.hovered-itinerary`: the leg and the place the case's `features`, topmost first, name. Each
 * feature is handed over as the properties it carries, none where it carries none, and its own `id` is
 * never handed over, since the rule never reads one. The fixture's property names are held to the ones the
 * rule reads. The rule is what an element beside a map lights by, which a consumer only observes, so it is
 * reached as an internal.
 */
object GeoLayersHoveredItineraryRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdConstantsExactly(
            case,
            mapOf(
                "segmentProperty" to FixtureValue.Text(SEGMENT_ID_PROPERTY),
                "waypointProperty" to FixtureValue.Text(WAYPOINT_ID_PROPERTY),
            ),
        )
        val properties =
            case.fields.items("features").map { feature ->
                val written = feature as? FixtureValue.Object ?: throw AssertionError("a feature is not an object")
                when (val held = written["properties"]) {
                    null, is FixtureValue.Null -> {
                        emptyMap()
                    }

                    is FixtureValue.Object -> {
                        JsonObject(
                            held.entries.associate { (name, value) ->
                                name to json(value)
                            },
                        )
                    }

                    else -> {
                        throw AssertionError("a feature's `properties` is not an object")
                    }
                }
            }
        val hovered = hoveredItinerary(properties)
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "segmentId" to (hovered.segmentId?.let { FixtureValue.Text(it) } ?: FixtureValue.Null),
                    "waypointId" to (hovered.waypointId?.let { FixtureValue.Text(it) } ?: FixtureValue.Null),
                ),
            ),
        )
    }

    /** [value] as the JSON a feature's properties hold it as. */
    private fun json(value: FixtureValue): JsonElement =
        when (value) {
            is FixtureValue.Null -> JsonNull
            is FixtureValue.Bool -> JsonPrimitive(value.value)
            is FixtureValue.Number -> JsonPrimitive(value.value)
            is FixtureValue.Text -> JsonPrimitive(value.value)
            is FixtureValue.Array -> JsonArray(value.items.map(::json))
            is FixtureValue.Object -> JsonObject(value.entries.associate { (name, item) -> name to json(item) })
        }
}
