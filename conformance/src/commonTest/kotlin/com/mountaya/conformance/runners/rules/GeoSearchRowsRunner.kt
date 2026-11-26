package com.mountaya.conformance

import com.mountaya.client.generated.DataSchema
import com.mountaya.geo.SearchRow
import com.mountaya.geo.SearchRowPayload
import com.mountaya.geo.SearchRowSecondary
import com.mountaya.geo.collectionRow
import com.mountaya.geo.itineraryRow
import com.mountaya.geo.placeRow
import com.mountaya.geo.poiRow
import com.mountaya.geo.wayRow
import com.mountaya.geo.zoneRow
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.Json

/**
 * `geo.search.rows`: the row the case's `entity` of its `kind` renders as for its `query`, in English
 * with metric figures, or null. The entity is decoded by the Data API's own serializer from the JSON
 * the fixture writes. The row is written as the fixture writes one: its `matched` runs as the text
 * each covers, and every member it does not state absent, `activities` and `matched` where it lists
 * none, a payload's geometry where the search answered none.
 */
object GeoSearchRowsRunner : ConformanceRunner {
    private val json = Json

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val entity = fixtureJson(input["entity"] ?: throw AssertionError("the case has no `entity`"))
        val query = input.text("query")
        val row =
            when (val kind = input.text("kind")) {
                "place" -> {
                    placeRow(json.decodeFromJsonElement(DataSchema.Place.serializer(), entity), query)
                }

                "poi" -> {
                    poiRow(json.decodeFromJsonElement(DataSchema.POI.serializer(), entity), query)
                }

                "way" -> {
                    wayRow(json.decodeFromJsonElement(DataSchema.Way.serializer(), entity), query)
                }

                "itinerary" -> {
                    itineraryRow(
                        json.decodeFromJsonElement(DataSchema.ItinerarySearchResult.serializer(), entity),
                        query,
                    )
                }

                "collection" -> {
                    collectionRow(
                        json.decodeFromJsonElement(DataSchema.CollectionSearchResult.serializer(), entity),
                        query,
                    )
                }

                "zone" -> {
                    zoneRow(json.decodeFromJsonElement(DataSchema.ZoneSearchResult.serializer(), entity), query)
                }

                else -> {
                    throw AssertionError("`$kind` is no kind of row")
                }
            }
        return ConformanceOutcome.Value(row?.let(::rowValue) ?: FixtureValue.Null)
    }

    private fun rowValue(row: SearchRow): FixtureValue =
        FixtureValue.Object(
            buildList {
                if (row.activities.isNotEmpty()) add("activities" to textArray(row.activities.map { it.value }))
                add("id" to FixtureValue.Text(row.id))
                if (row.matched.isNotEmpty()) add("matched" to textArray(row.matched.map { row.title.substring(it) }))
                add("payload" to payloadValue(row.payload))
                row.secondary?.let { add("secondary" to secondaryValue(it)) }
                add("title" to FixtureValue.Text(row.title))
            },
        )

    private fun secondaryValue(secondary: SearchRowSecondary): FixtureValue =
        when (secondary) {
            is SearchRowSecondary.Text -> {
                FixtureValue.Object(
                    listOf(
                        "kind" to FixtureValue.Text("text"),
                        "text" to FixtureValue.Text(secondary.text),
                    ),
                )
            }

            is SearchRowSecondary.Metric -> {
                FixtureValue.Object(
                    listOf(
                        "ascent" to numberOrNull(secondary.ascent?.toDouble()),
                        "distance" to numberOrNull(secondary.distance),
                        "kind" to FixtureValue.Text("metric"),
                    ),
                )
            }

            is SearchRowSecondary.Count -> {
                FixtureValue.Object(
                    listOf(
                        "itineraries" to numberOrNull(secondary.itineraries?.toDouble()),
                        "kind" to FixtureValue.Text("count"),
                    ),
                )
            }
        }

    private fun payloadValue(payload: SearchRowPayload): FixtureValue =
        FixtureValue.Object(
            buildList {
                when (payload) {
                    is SearchRowPayload.Place -> {
                        payload.bounds?.let { add("bounds" to numbers(it)) }
                        add("kind" to FixtureValue.Text("place"))
                        add("point" to numbers(payload.point))
                    }

                    is SearchRowPayload.Poi -> {
                        payload.category?.let { add("category" to FixtureValue.Text(it.value)) }
                        add("kind" to FixtureValue.Text("poi"))
                        add("point" to numbers(payload.point))
                    }

                    is SearchRowPayload.Way -> {
                        add("anchor" to numbers(payload.anchor))
                        payload.bounds?.let { add("bounds" to numbers(it)) }
                        payload.coordinates?.let { line ->
                            add("coordinates" to FixtureValue.Array(line.map(::numbers)))
                        }
                        add("kind" to FixtureValue.Text("way"))
                    }

                    is SearchRowPayload.Itinerary -> {
                        payload.bounds?.let { add("bounds" to numbers(it)) }
                        add("itineraryId" to FixtureValue.Text(payload.itineraryId))
                        add("kind" to FixtureValue.Text("itinerary"))
                        payload.start?.let { add("start" to numbers(it)) }
                    }

                    is SearchRowPayload.Collection -> {
                        add("collectionId" to FixtureValue.Text(payload.collectionId))
                        add("description" to textOrNull(payload.description))
                        add("isPrivate" to (payload.isPrivate?.let { FixtureValue.Bool(it) } ?: FixtureValue.Null))
                        add("kind" to FixtureValue.Text("collection"))
                        add("name" to textOrNull(payload.name))
                    }

                    is SearchRowPayload.Zone -> {
                        payload.bounds?.let { add("bounds" to numbers(it)) }
                        add("kind" to FixtureValue.Text("zone"))
                        payload.point?.let { add("point" to numbers(it)) }
                        add("zoneId" to FixtureValue.Text(payload.zoneId))
                    }
                }
            },
        )

    private fun numbers(values: List<Double>): FixtureValue = FixtureValue.Array(values.map { FixtureValue.Number(it) })
}
