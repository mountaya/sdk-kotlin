package com.mountaya.conformance

import com.mountaya.geo.ItineraryStroke
import com.mountaya.geo.itineraryStrokeStyle
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.JsonElement

/**
 * `geo.style.itinerary-stroke`: the layer the case's stroke is drawn as, its `kind` and `color` read as
 * the `ItineraryStroke` they name, under the paint its `paint` patch resolves to, the shipped paint where
 * it states none. The rule answers a line layer's style, its layout and its paint, so the layer is of type
 * `line`, and its `id`, `source`, `sourceLayer` and `filter` are the case's own, written where the case
 * states them as MapLibre's style specification names them. A casing given a colour fails the case, since
 * a casing takes none of its caller's.
 */
object GeoStyleItineraryStrokeRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val stroke =
            when (val kind = input.text("kind")) {
                "line" -> {
                    ItineraryStroke.Line(strokeColor(input))
                }

                "dashed" -> {
                    ItineraryStroke.Dashed(strokeColor(input))
                }

                "casing" -> {
                    if (input["color"] != null) throw AssertionError("a casing takes no colour of its caller's")
                    ItineraryStroke.Casing
                }

                else -> {
                    throw AssertionError("`$kind` is no stroke")
                }
            }
        val style = itineraryStrokeStyle(stroke, fixtureMapPaint(input["paint"]))
        val layer = mutableListOf<Pair<String, FixtureValue>>()
        input["filter"]?.let { layer += "filter" to it }
        layer += "id" to FixtureValue.Text(input.text("id"))
        layer += "layout" to writtenProperties(style.layout)
        layer += "paint" to writtenProperties(style.paint)
        layer += "source" to FixtureValue.Text(input.text("source"))
        input.textOrNull("sourceLayer")?.let { layer += "source-layer" to FixtureValue.Text(it) }
        layer += "type" to FixtureValue.Text("line")
        return ConformanceOutcome.Value(FixtureValue.Object(layer))
    }

    /** The colour a stroke's case states, a literal or an expression, as JSON. */
    private fun strokeColor(input: FixtureValue.Object): JsonElement =
        fixtureJson(input["color"] ?: throw AssertionError("a line or a dashed stroke states no `color`"))

    private fun writtenProperties(properties: Map<String, JsonElement>): FixtureValue =
        FixtureValue.Object(properties.entries.sortedBy { it.key }.map { it.key to fixtureValueOf(it.value) })
}
