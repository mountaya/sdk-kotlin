package com.example.consumer

import com.mountaya.admin.WebhookSignatureException
import com.mountaya.admin.admin
import com.mountaya.admin.generated.AdminSchema
import com.mountaya.admin.generated.WebhookEvent
import com.mountaya.client.CredentialDeclaration
import com.mountaya.client.DataOperation
import com.mountaya.client.EventStream
import com.mountaya.client.FieldSelection
import com.mountaya.client.Mountaya
import com.mountaya.client.MultipartPart
import com.mountaya.client.OperationDeclaration
import com.mountaya.client.PageWalk
import com.mountaya.client.RequestBody
import com.mountaya.client.RequestOptions
import com.mountaya.client.RetryPolicy
import com.mountaya.client.Stated
import com.mountaya.client.Surface
import com.mountaya.client.UrlTemplate
import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import com.mountaya.client.generated.TileTemplates
import com.mountaya.client.readingAsUser
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/** Calls calls one operation of every kind, and reads what each answers. */
object Calls {
    /** Reads a resource, then deletes it under the tag it was read with, and creates a link. */
    suspend fun envelope(client: Mountaya) {
        val read = client.studio.collections.getCollection("co")
        seen(read.data, read.metadata, read.status, read.requestId, read.location, read.etag)
        val written =
            client.studio.collections.deleteCollection(
                "co",
                options = RequestOptions(retry = RetryPolicy(maxRetries = 0), timeout = 5.seconds),
            ) {
                idempotencyKey = "order-7f3a"
                ifMatch = read.data.etag
            }
        seen(written.data)
        val link = client.studio.itineraries.postItineraryShareLink("it")
        seen(link.data.share_token, link.data.created_at, link.location)
    }

    /** Sends a body a caller builds, its optional fields set in the trailing builder, a merge's `null` clearing one. */
    suspend fun body(client: Mountaya) {
        val updated =
            client.studio.itineraries.patchItinerarySegment(
                "it",
                "se",
                body =
                    RestSchema.SegmentUpdateParams {
                        description = null
                        speed = 4.5
                    },
            )
        seen(updated.data.name)
    }

    /** Reads an export's bytes, the content type the server chose and what its answer said. */
    suspend fun file(client: Mountaya) {
        val export = client.studio.itineraries.getItineraryExport("it", RestSchema.ExportFormat.Gpx)
        seen(export.bytes, export.contentType, export.status, export.requestId)
    }

    /** Reads an agent run's stream to its end, a member the contract adds read as itself. */
    suspend fun events(client: Mountaya) {
        client.agent.runs.getAgentRunEvents("ru").collect { frame ->
            when (val event = frame.event) {
                is RestSchema.AgentMessageEvent -> seen(frame.id, event.content, event.message_index)
                is RestSchema.AgentDoneEvent -> seen(event.reason)
                else -> seen(event)
            }
        }
    }

    /**
     * Reads an itinerary's editor stream as its member's user, which a session token given as a value
     * names to nobody, and an event a webhook delivers, typed by its class, as a receiver does.
     */
    suspend fun editors(client: Mountaya) {
        val stream: EventStream<RestSchema.CollabStreamEvent> = client.studio.collab.getItineraryEvents("it")
        stream.readingAsUser("usr_7a4e").readingAsUser("usr_9b0c").collect { frame ->
            val event = frame.event
            if (event is RestSchema.CollabPresenceJoinEvent) seen(event.user_id, event.connected_at)
        }
    }

    /** Reads every member of a webhook event's envelope, the data its class carries and what an unknown one holds. */
    fun webhook(event: WebhookEvent) {
        seen(event.id, event.`object`, event.type, event.api_version, event.created_at, event.organization_id)
        seen(event.task, event.related_object, event.request, event.actor)
        when (event) {
            is WebhookEvent.OrganizationMemberUpdated -> {
                val former: RestSchema.OrganizationRole? = event.data.previous_attributes?.role
                seen(event.data.`object`.role, former, event.payload)
            }

            is WebhookEvent.ZoneUpdated -> {
                val former: Stated<Instant?>? = event.data.previous_attributes?.end_at
                seen(former?.value, event.data.`object`.name)
            }

            is WebhookEvent.CollectionDeleted -> {
                val none: Nothing? = event.data.previous_attributes
                seen(none, event.data.`object`.id)
            }

            is WebhookEvent.Typed<*, *> -> {
                seen(event.data.`object`)
            }

            is WebhookEvent.Unknown -> {
                seen(event.event.data.`object`, event.event.data.previous_attributes)
            }
        }
        val refused = WebhookSignatureException(WebhookSignatureException.Reason.Timestamp, "late")
        seen(refused.reason.value, refused.name, refused.message)
    }

    /** Asks the Data API for a narrowed result. */
    suspend fun data(client: Mountaya) {
        val directions =
            client.data.directions(
                DataSchema.DirectionsInput(
                    activity = DataSchema.ActivitySlug.HikingAndTrail,
                    waypoints = listOf(listOf(6.86, 45.83), listOf(6.87, 45.84)),
                ),
                select =
                    FieldSelection {
                        field("routes") {
                            field("distance")
                            field("duration")
                        }
                    },
            )
        seen(directions.routes?.firstOrNull()?.distance)
    }

    /** Walks a paged listing, a page requested only as the rows before it are read. */
    suspend fun pages(client: Mountaya) {
        PageWalk(pageSize = 20, maxPages = 5) { paged -> client.studio.itineraries.getItineraries { paged() } }
            .collect { itinerary -> seen(itinerary.id, itinerary.name, itinerary.etag) }
        seen(PageWalk.defaultPageSize, PageWalk.maxPageSize)
        // A printed listing's own walk, at the page size its operation states.
        client.studio.itineraries
            .walkGetItineraries(maxPages = 5, options = RequestOptions(retry = RetryPolicy(maxRetries = 1))) {
                sort = RestSchema.GetItinerariesSort.UpdatedAtDescending
            }.collect { itinerary -> seen(itinerary.id) }
        // An admin listing's walk, the admin unit's, which names the core's role and answers its own members.
        val admin = RestSchema.OrganizationRole.Admin
        client.admin.members
            .walkGetOrganizationMembers("org_1", pageSize = 100) { role = admin }
            .collect { member: AdminSchema.OrganizationMember -> seen(member.user_id, member.role) }
    }

    /** Reads the routes a map renderer fetches on its own, each a template no method binds. */
    fun templates() {
        for (template in TileTemplates.all) {
            seen(
                template.operationId,
                template.path,
                template.pathParameters,
                template.queryParameters,
                template.mediaTypes,
            )
        }
        seen(TileTemplates.getItineraryTile.filled(mapOf("itinerary_id" to "itn_4e5f")))
        seen(UrlTemplate("getTile", "/v1/{z}/{x}/{y}", listOf("z", "x", "y")))
    }

    /** Calls an operation this build prints no method for, by its declaration, and opens one of its streams. */
    suspend fun declared(client: Mountaya) {
        val operation =
            OperationDeclaration(
                operationId = "getThing",
                method = "GET",
                surface = Surface.Rest,
                idempotent = true,
                credentials = CredentialDeclaration(requiresSession = true),
                timeout = 30.seconds,
            )
        val thing =
            client.send(
                operation,
                "/v1/things/th",
                JsonElement.serializer(),
                JsonObject.serializer().nullable,
                query = listOf("limit" to "2"),
                headers = mapOf("X-Trace" to "t-1"),
            )
        seen(thing.data, thing.metadata, thing.headers, thing.header("cache-control"))
        client.events(operation, "/v1/things/th/events").collect { frame ->
            seen(frame.id, frame.event.event, frame.event.data, frame.event.id)
        }
        val created =
            client.send(
                OperationDeclaration("postThing", "POST", declaresIdempotencyKey = true),
                "/v1/things",
                JsonElement.serializer(),
                JsonObject.serializer().nullable,
                body = RequestBody.Json(JsonObject(mapOf("name" to JsonPrimitive("Hut")))),
            )
        val previewed =
            client.send(
                OperationDeclaration("postThingPreview", "POST", idempotent = true),
                "/v1/things/preview",
                JsonElement.serializer(),
                JsonObject.serializer().nullable,
                body =
                    RequestBody.Multipart(
                        listOf(
                            MultipartPart.Text("name", "Hut"),
                            MultipartPart.File("track", "<gpx/>".encodeToByteArray()),
                        ),
                    ),
            )
        seen(created.location, previewed.status, RequestBody.Empty)
        val weather =
            client.query(
                DataOperation(
                    "weather",
                    "WeatherInput!",
                    FieldSelection { field("temperature") },
                    timeout = 30.seconds,
                ),
                JsonObject(mapOf("point" to JsonPrimitive("6.86,45.83"))),
                JsonElement.serializer(),
                select = FieldSelection { field("temperature") },
            )
        seen(weather)
        client
            .walk(
                OperationDeclaration("getThings", "GET", idempotent = true),
                "/v1/things",
                JsonElement.serializer(),
                query = listOf("kind" to "hut"),
                pageSize = 25,
                maxPages = 2,
            ).collect { row -> seen(row) }
    }
}
