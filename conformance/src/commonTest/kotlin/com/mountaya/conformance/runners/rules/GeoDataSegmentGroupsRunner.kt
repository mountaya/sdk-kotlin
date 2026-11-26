package com.mountaya.conformance

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.applySegmentGroups
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlin.time.Instant

/**
 * `geo.data.segment-groups`: the case's chain of `segments` projected with its `groups` collapsed.
 *
 * Each segment and group is built as the REST API's, from the members the fixture writes and a
 * placeholder for every other the type requires. The fixture's segments are not quite the wire's: a
 * mode of `ROUTED`, which the open mode keeps as written, a creator written as its id where the wire
 * writes a user, and no colour, name or revision (CONTRACT-GAPS). So the projected chain is written
 * back as the wire writes it, a creator as its id: a segment that passes as itself, the same instance,
 * with the members its case wrote, and a collapsed one with every member of the wire's segment.
 */
object GeoDataSegmentGroupsRunner : ConformanceRunner {
    private val json = Json

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val written =
            input.items("segments").map { item ->
                val fields = item as? FixtureValue.Object ?: throw AssertionError("a segment is not an object")
                fixtureSegment(fields) to fields.keys
            }
        val groups =
            input.items("groups").map { item ->
                val group = item as? FixtureValue.Object ?: throw AssertionError("a group is not an object")
                RestSchema.ItinerarySegmentGroup(
                    color = group.text("color"),
                    created_at = Instant.parse(group.text("created_at")),
                    created_by = RestSchema.RelationToUser(id = group.text("created_by")),
                    id = group.text("id"),
                    itinerary_id = group.text("itinerary_id"),
                    name = group.text("name"),
                    revision = wholeOrNull(group, "revision") ?: throw AssertionError("a group has no `revision`"),
                    speed = group.numberOrNull("speed"),
                )
            }
        val projected = applySegmentGroups(written.map { it.first }, groups)
        return ConformanceOutcome.Value(
            FixtureValue.Array(
                projected.map { segment ->
                    val wire = json.encodeToJsonElement(RestSchema.ItinerarySegment.serializer(), segment) as JsonObject
                    val keys = written.firstOrNull { it.first === segment }?.second ?: wire.keys.toList()
                    FixtureValue.Object(
                        keys.map { key ->
                            val value = wire[key] ?: throw AssertionError("the wire's segment has no member `$key`")
                            // The fixture writes a creator as its id.
                            val spelled = if (key == "created_by") (value as JsonObject).getValue("id") else value
                            key to fixtureValue(spelled)
                        },
                    )
                },
            ),
        )
    }

    /** [element] as a fixture value. */
    private fun fixtureValue(element: JsonElement): FixtureValue =
        when (element) {
            is JsonNull -> {
                FixtureValue.Null
            }

            is JsonPrimitive -> {
                when {
                    element.isString -> {
                        FixtureValue.Text(element.content)
                    }

                    element.booleanOrNull != null -> {
                        FixtureValue.Bool(element.content == "true")
                    }

                    else -> {
                        FixtureValue.Number(
                            element.doubleOrNull ?: throw AssertionError("`${element.content}` is no number"),
                        )
                    }
                }
            }

            is JsonArray -> {
                FixtureValue.Array(element.map(::fixtureValue))
            }

            is JsonObject -> {
                FixtureValue.Object(element.entries.map { (key, value) -> key to fixtureValue(value) })
            }
        }
}
