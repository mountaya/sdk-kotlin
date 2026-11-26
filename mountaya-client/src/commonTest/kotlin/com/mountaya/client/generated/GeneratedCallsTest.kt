package com.mountaya.client.generated

import com.mountaya.client.MultipartPart
import com.mountaya.client.RequestBody
import com.mountaya.client.RequestOptions
import com.mountaya.client.ConfigurationException
import com.mountaya.client.SelectionException
import com.mountaya.client.BinaryResponse
import com.mountaya.client.RetryPolicy
import com.mountaya.client.FieldSelection
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** What a printed method hands the transport: the path, the query, the headers and the body, settled from its signature. */
class GeneratedCallsTest {
    @Test
    fun aPathParameterIsSubstitutedAsOneEncodedSegment() =
        runTest {
            val sender = ScriptedSender(data = Json.parseToJsonElement("""{"id":"x"}"""))
            val studio = Studio(sender)

            runCatching { studio.itineraries.getItinerary(itinerary_id = "a/b c é") }

            val call = sender.calls.single()
            assertEquals("/v1/itineraries/a%2Fb%20c%20%C3%A9", call.path)
            assertSame(OperationTable.Rest.getItinerary, call.facts)
            assertEquals(emptyList(), call.query)
            assertEquals(emptyList(), call.headers)
            assertEquals(RequestBody.Empty, call.body)
        }

    @Test
    fun theQueryIsInTheOperationsOrderWithOnlyWhatWasSet() =
        runTest {
            val sender = ScriptedSender(file = BinaryResponse(byteArrayOf(1, 2), "application/gpx+xml"))
            val studio = Studio(sender)

            val file =
                studio.itineraries.getItineraryExport(itinerary_id = "it", format = RestSchema.ExportFormat.Gpx) {
                    surface = RestSchema.ReadSurface.Published
                    variant = RestSchema.ExportVariant("a-variant-this-build-never-saw")
                }

            assertEquals(
                listOf("format" to "gpx", "variant" to "a-variant-this-build-never-saw", "surface" to "published"),
                sender.calls.single().query,
            )
            assertContentEquals(byteArrayOf(1, 2), file.bytes)
            assertEquals("application/gpx+xml", file.contentType)
        }

    @Test
    fun theCallsOptionsReachTheTransport() =
        runTest {
            val sender = ScriptedSender()
            val options = RequestOptions(retry = RetryPolicy(maxRetries = 0))

            Studio(sender).itineraries.deleteItinerary(itinerary_id = "it", options = options)

            assertEquals(options, sender.calls.single().options)
        }

    @Test
    fun anOperationAnsweringWithNoDataReadsTheEnvelopeAsItCame() =
        runTest {
            val sender = ScriptedSender(data = JsonNull, metadata = Json.parseToJsonElement("""{"request_id":"r"}"""))

            val response = Studio(sender).itineraries.deleteItinerary(itinerary_id = "it")

            assertEquals(JsonNull, response.data)
            assertEquals(Json.parseToJsonElement("""{"request_id":"r"}""").jsonObject, response.metadata)
        }

    @Test
    fun declaredMetadataIsReadAsItsType() =
        runTest {
            val sender =
                ScriptedSender(
                    data = Json.parseToJsonElement("[]"),
                    metadata = Json.parseToJsonElement("""{"pagination":{"limit":10,"next_cursor":"c2"}}"""),
                )

            val response = Studio(sender).collections.getCollectionItineraries(collection_id = "c") { limit = 10 }

            assertEquals(emptyList(), response.data)
            assertEquals("c2", response.metadata.pagination.next_cursor)
            assertEquals(listOf("limit" to "10"), sender.calls.single().query)
        }

    /** An empty page of a listing, naming [next] as the page after it: a walk follows an empty page as any other. */
    private fun emptyPage(next: String?) =
        Json.parseToJsonElement("[]") to
            Json.parseToJsonElement("""{"pagination":{"limit":2,"next_cursor":${next?.let { "\"$it\"" } ?: "null"}}}""")

    @Test
    fun aWalkReadsEveryPageThroughItsMethodTheCallersFiltersBesideTheWalksCursorAndLimit() =
        runTest {
            val sender = ScriptedSender(pages = listOf(emptyPage("c2"), emptyPage("c3"), emptyPage(null)))
            val options = RequestOptions(retry = RetryPolicy(maxRetries = 0))

            val itineraries =
                Studio(sender)
                    .collections
                    .walkGetCollectionItineraries("col/1", pageSize = 2, options = options) {
                        sort = RestSchema.GetCollectionItinerariesSort.Name
                    }.toList()

            assertEquals(emptyList(), itineraries)
            assertEquals(List(3) { "/v1/collections/col%2F1/itineraries" }, sender.calls.map { it.path })
            assertEquals(
                listOf(
                    listOf("limit" to "2", "sort" to "name"),
                    listOf("cursor" to "c2", "limit" to "2", "sort" to "name"),
                    listOf("cursor" to "c3", "limit" to "2", "sort" to "name"),
                ),
                sender.calls.map { it.query },
            )
            assertTrue(sender.calls.all { it.options == options && it.facts === OperationTable.Rest.getCollectionItineraries })
        }

    @Test
    fun aWalkAsksForTheOperationsOwnPageSizeResumesFromTheCallersCursorAndStopsAtItsCeiling() =
        runTest {
            val sender = ScriptedSender(pages = List(3) { emptyPage("c${it + 1}") })

            Studio(sender).collections.walkGetCollectionItineraries("col", maxPages = 2) { cursor = "c0" }.toList()

            assertEquals(
                listOf(listOf("cursor" to "c0", "limit" to "50"), listOf("cursor" to "c1", "limit" to "50")),
                sender.calls.map { it.query },
            )
            assertFailsWith<ConfigurationException> { Studio(sender).itineraries.walkGetItineraries(pageSize = 101) }
            assertEquals(2, sender.calls.size, "a refused walk sends nothing")
        }

    /**
     * `Last-Event-ID` is what a stream's reopen sends, never a caller's header, so a stream's open
     * carries no header of the caller's at this tag.
     */
    @Test
    fun aStreamIsColdAndItsOpenCarriesNoHeaderOfTheCallers() =
        runTest {
            val done = Json.parseToJsonElement("""{"type":"done","reason":"completed"}""")
            val sender = ScriptedSender(events = listOf("41" to done))

            val events = Agent(sender).runs.getAgentRunEvents(run_id = "run")
            val call = sender.calls.single()
            val frames = events.toList()

            assertEquals(emptyList(), call.headers)
            assertEquals("41", frames.single().id)
            val event = assertIs<RestSchema.AgentDoneEvent>(frames.single().event)
            assertEquals(RestSchema.AgentRunReason.Completed, event.reason)
        }

    @Test
    fun aMergeBodySendsOnlyWhatWasSetAndNullClears() =
        runTest {
            val sender = ScriptedSender(data = Json.parseToJsonElement("{}"))
            val collections = Studio(sender).collections

            for (body in listOf(
                RestSchema.CollectionUpdateParams(),
                RestSchema.CollectionUpdateParams { description = "Alpine huts" },
                RestSchema.CollectionUpdateParams { description = null },
            )) {
                runCatching { collections.patchCollection(collection_id = "c", body = body) }
            }

            assertEquals(
                listOf("{}", """{"description":"Alpine huts"}""", """{"description":null}"""),
                sender.calls.map { (it.body as RequestBody.Json).text },
            )
        }

    /**
     * The key and the tag ride the call's own builder, and each is handed to the transport exactly
     * as the caller set it, or not at all.
     */
    @Test
    fun aWriteHandsTheTransportTheKeyAndTheTagItsCallerSetAndNoneUnset() =
        runTest {
            val sender = ScriptedSender(data = JsonNull)
            val collections = Studio(sender).collections

            collections.deleteCollection(collection_id = "c") {
                idempotencyKey = "delete-c-1"
                ifMatch = "\"v7\""
            }
            collections.deleteCollection(collection_id = "c")

            val (set, unset) = sender.calls
            assertEquals("delete-c-1", set.idempotencyKey)
            assertEquals("\"v7\"", set.ifMatch)
            assertNull(unset.idempotencyKey)
            assertNull(unset.ifMatch)
            assertTrue(set.facts.declaresIdempotencyKey && set.facts.isConditional)
        }

    @Test
    fun aFormBodyIsSentAsPartsInTheContractsOrder() =
        runTest {
            val sender = ScriptedSender(data = Json.parseToJsonElement("{}"))
            val file = byteArrayOf(60, 103, 112, 120)
            val body =
                RestSchema.PostItineraryPreviewMultipartBody(
                    provider = RestSchema.ImportProvider.Gpx,
                    file = file,
                    activity_id = "activity",
                ) { granularity = RestSchema.ImportGranularity.Single }

            runCatching { Studio(sender).itineraries.postItineraryPreview(body = body) }

            val parts = assertIs<RequestBody.Multipart>(sender.calls.single().body).parts
            assertEquals(
                listOf(
                    MultipartPart.Text("provider", "gpx"),
                    // The media type the form's `encoding` names for its file, which the model states.
                    MultipartPart.File("file", file, contentType = "application/octet-stream"),
                    MultipartPart.Text("activity_id", "activity"),
                    MultipartPart.Text("granularity", "single"),
                ),
                parts,
            )
        }

    @Test
    fun aDataApiCallSendsTheNarrowedDocumentAndTheInputAsAVariable() =
        runTest {
            val sender = ScriptedSender(result = Json.parseToJsonElement("""{"routes":[{"distance":15200.0}]}"""))
            val input =
                DataSchema.DirectionsInput(
                    activity = DataSchema.ActivitySlug.HikingAndTrail,
                    waypoints = listOf(listOf(6.86, 45.83), listOf(6.87, 45.84)),
                )

            val result = Data(sender).directions(input, select = FieldSelection { field("routes") { field("distance") } })

            val call = sender.dataCalls.single()
            assertSame(OperationTable.Data.directions, call.facts)
            assertEquals(
                "query Directions(\$input: DirectionsInput!) {\n  directions(input: \$input) {\n    routes {\n      distance\n    }\n  }\n}\n",
                call.document,
            )
            assertEquals("""{"activity":"HIKING_AND_TRAIL","waypoints":[[6.86,45.83],[6.87,45.84]]}""", call.input)
            assertEquals(15200.0, result.routes?.single()?.distance)
            assertNull(result.routes?.single()?.duration)
        }

    @Test
    fun aDataApiCallWithNoSelectionSendsTheFullDocument() =
        runTest {
            val sender = ScriptedSender()

            Data(sender).directions(DataSchema.DirectionsInput(DataSchema.ActivitySlug.Running, emptyList()))

            assertEquals(DataOperations.directions.document, sender.dataCalls.single().document)
        }

    @Test
    fun aFieldOutsideTheTreeFailsAtTheCallBeforeAnythingIsSent() =
        runTest {
            val sender = ScriptedSender()

            assertFailsWith<SelectionException> {
                Data(sender).directions(
                    DataSchema.DirectionsInput(DataSchema.ActivitySlug.Running, emptyList()),
                    select = FieldSelection { field("routes") { field("nope") } },
                )
            }
            assertTrue(sender.dataCalls.isEmpty())
        }
}
