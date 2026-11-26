// GENERATED FILE. DO NOT EDIT.
//
// Printed by `./gradlew generateClient` from sdk/model.json of mountaya/schemas, contract
// v0.9.0, model digest f9ded6dd4ca71cb8f89c6974c42adec77509ee9a19e5afd01c31b6ea9dcc33ea.
// Every name below is the contract's, verbatim, but an enum member's, printed as its words
// (SPEC.md section 3). A shape that is wrong here is a defect in the contract's description,
// and the next print overwrites any edit.

package com.mountaya.client.generated

/** OperationTable is what the contract declares about each printed operation, one property per operation and nothing inferred from a method. A test holds it to `sdk/model.json` fact by fact. */
internal object OperationTable {
    /** The operations of the REST API, in the model's order. */
    internal object Rest {
        val deleteCollection: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "deleteCollection",
                method = "DELETE",
                path = "/v1/collections/{collection_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 429, 500, 504),
                tasks = kotlin.collections.listOf("collection.delete"),
            )

        val deleteCollectionItinerary: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "deleteCollectionItinerary",
                method = "DELETE",
                path = "/v1/collections/{collection_id}/itineraries/{itinerary_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 429, 500, 504),
                tasks = kotlin.collections.listOf("collection.itinerary.remove"),
            )

        val deleteCollectionShareLink: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "deleteCollectionShareLink",
                method = "DELETE",
                path = "/v1/collections/{collection_id}/share-link",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 429, 500, 504),
                tasks = kotlin.collections.listOf("collection.share_link.delete"),
            )

        val deleteCollectionZone: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "deleteCollectionZone",
                method = "DELETE",
                path = "/v1/collections/{collection_id}/zones/{zone_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 429, 500, 504),
                tasks = kotlin.collections.listOf("collection.zone.remove"),
            )

        val deleteItinerary: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "deleteItinerary",
                method = "DELETE",
                path = "/v1/itineraries/{itinerary_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.delete"),
            )

        val deleteItineraryPublication: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "deleteItineraryPublication",
                method = "DELETE",
                path = "/v1/itineraries/{itinerary_id}/publication",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.unpublish"),
            )

        val deleteItinerarySegment: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "deleteItinerarySegment",
                method = "DELETE",
                path = "/v1/itineraries/{itinerary_id}/segments/{segment_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.segment.delete"),
            )

        val deleteItinerarySegmentGroup: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "deleteItinerarySegmentGroup",
                method = "DELETE",
                path = "/v1/itineraries/{itinerary_id}/segment-groups/{group_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.segment_group.delete"),
            )

        val deleteItineraryShareLink: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "deleteItineraryShareLink",
                method = "DELETE",
                path = "/v1/itineraries/{itinerary_id}/share-link",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.share_link.delete"),
            )

        val deleteItineraryWaypoint: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "deleteItineraryWaypoint",
                method = "DELETE",
                path = "/v1/itineraries/{itinerary_id}/waypoints/{waypoint_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.waypoint.delete"),
            )

        val deleteZone: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "deleteZone",
                method = "DELETE",
                path = "/v1/zones/{zone_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 429, 500, 504),
                tasks = kotlin.collections.listOf("zone.delete"),
            )

        val deleteZonePublication: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "deleteZonePublication",
                method = "DELETE",
                path = "/v1/zones/{zone_id}/publication",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 429, 500, 504),
                tasks = kotlin.collections.listOf("zone.unpublish"),
            )

        val getActivities: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getActivities",
                method = "GET",
                path = "/v1/activities",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = true,
                        requiresSession = false,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getAgentRunEvents: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getAgentRunEvents",
                method = "GET",
                path = "/v1/agent/runs/{run_id}/events",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = null,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.EventStream,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 429, 500, 503),
                tasks = kotlin.collections.emptyList(),
            )

        val getCollection: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getCollection",
                method = "GET",
                path = "/v1/collections/{collection_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = true,
                        requiresSession = false,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getCollectionItineraries: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getCollectionItineraries",
                method = "GET",
                path = "/v1/collections/{collection_id}/itineraries",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = true,
                        requiresSession = false,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination =
                    com.mountaya.client.Pagination(
                        sort = kotlin.collections.listOf("position", "-updated_at", "name", "-name"),
                        defaultSort = "position",
                    ),
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getCollectionShareLink: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getCollectionShareLink",
                method = "GET",
                path = "/v1/collections/{collection_id}/share-link",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getCollectionZones: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getCollectionZones",
                method = "GET",
                path = "/v1/collections/{collection_id}/zones",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = true,
                        requiresSession = false,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination =
                    com.mountaya.client.Pagination(
                        sort = kotlin.collections.listOf("-updated_at", "updated_at", "name", "-name"),
                        defaultSort = "-updated_at",
                    ),
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getCollections: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getCollections",
                method = "GET",
                path = "/v1/collections",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = false,
                        requiresSession = false,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination =
                    com.mountaya.client.Pagination(
                        sort = kotlin.collections.listOf("-updated_at", "updated_at", "-created_at", "created_at", "name", "-name"),
                        defaultSort = "-updated_at",
                    ),
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getItineraries: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getItineraries",
                method = "GET",
                path = "/v1/itineraries",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = false,
                        requiresSession = false,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination =
                    com.mountaya.client.Pagination(
                        sort = kotlin.collections.listOf("-updated_at", "updated_at", "-created_at", "created_at", "name", "-name"),
                        defaultSort = "-updated_at",
                    ),
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getItinerary: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getItinerary",
                method = "GET",
                path = "/v1/itineraries/{itinerary_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = true,
                        requiresSession = false,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("Cache-Control", "ETag", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getItineraryEvents: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getItineraryEvents",
                method = "GET",
                path = "/v1/itineraries/{itinerary_id}/events",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = null,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.EventStream,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 429, 500, 503),
                tasks = kotlin.collections.emptyList(),
            )

        val getItineraryExport: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getItineraryExport",
                method = "GET",
                path = "/v1/itineraries/{itinerary_id}/export",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = true,
                        requiresSession = false,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Binary,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("Content-Disposition", "Content-Length", "Referrer-Policy", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 413, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getItineraryPace: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getItineraryPace",
                method = "GET",
                path = "/v1/itineraries/{itinerary_id}/pace",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = true,
                        requiresSession = false,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("Cache-Control", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getItineraryShareLink: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getItineraryShareLink",
                method = "GET",
                path = "/v1/itineraries/{itinerary_id}/share-link",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getLayers: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getLayers",
                method = "GET",
                path = "/v1/layers",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = true,
                        requiresSession = false,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("Cache-Control", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getOrganizationEvents: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganizationEvents",
                method = "GET",
                path = "/v1/organizations/{organization_id}/events",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = null,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.EventStream,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 429, 500, 503),
                tasks = kotlin.collections.emptyList(),
            )

        val getZone: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getZone",
                method = "GET",
                path = "/v1/zones/{zone_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = true,
                        requiresSession = false,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("Cache-Control", "ETag", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getZoneEvents: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getZoneEvents",
                method = "GET",
                path = "/v1/zones/{zone_id}/events",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = null,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.EventStream,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 429, 500, 503),
                tasks = kotlin.collections.emptyList(),
            )

        val getZones: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getZones",
                method = "GET",
                path = "/v1/zones",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = false,
                        requiresSession = false,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination =
                    com.mountaya.client.Pagination(
                        sort = kotlin.collections.listOf("-updated_at", "updated_at", "-created_at", "created_at", "name", "-name"),
                        defaultSort = "-updated_at",
                    ),
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val patchCollection: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "patchCollection",
                method = "PATCH",
                path = "/v1/collections/{collection_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("collection.update"),
            )

        val patchCollectionItinerary: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "patchCollectionItinerary",
                method = "PATCH",
                path = "/v1/collections/{collection_id}/itineraries/{itinerary_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("collection.itinerary.move"),
            )

        val patchItinerary: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "patchItinerary",
                method = "PATCH",
                path = "/v1/itineraries/{itinerary_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.update"),
            )

        val patchItinerarySegment: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "patchItinerarySegment",
                method = "PATCH",
                path = "/v1/itineraries/{itinerary_id}/segments/{segment_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.segment.update"),
            )

        val patchItinerarySegmentGroup: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "patchItinerarySegmentGroup",
                method = "PATCH",
                path = "/v1/itineraries/{itinerary_id}/segment-groups/{group_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.segment_group.update"),
            )

        val patchItineraryWaypoint: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "patchItineraryWaypoint",
                method = "PATCH",
                path = "/v1/itineraries/{itinerary_id}/waypoints/{waypoint_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.waypoint.update"),
            )

        val patchZone: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "patchZone",
                method = "PATCH",
                path = "/v1/zones/{zone_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 413, 429, 500, 503, 504),
                tasks = kotlin.collections.listOf("zone.update"),
            )

        val postAgentRun: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postAgentRun",
                method = "POST",
                path = "/v1/agent/runs",
                idempotent = false,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(202, kotlin.collections.listOf("Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("agent.run.start"),
            )

        val postAgentRunFeedback: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postAgentRunFeedback",
                method = "POST",
                path = "/v1/agent/runs/{run_id}/feedback",
                idempotent = false,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(202, kotlin.collections.listOf("Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 413, 429, 500, 503, 504),
                tasks = kotlin.collections.listOf("agent.run.feedback.send"),
            )

        val postAgentRunInput: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postAgentRunInput",
                method = "POST",
                path = "/v1/agent/runs/{run_id}/input",
                idempotent = false,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(202, kotlin.collections.listOf("Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("agent.run.input.send"),
            )

        val postCollection: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postCollection",
                method = "POST",
                path = "/v1/collections",
                idempotent = false,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(201, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "Location", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("collection.create"),
            )

        val postCollectionItineraries: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postCollectionItineraries",
                method = "POST",
                path = "/v1/collections/{collection_id}/itineraries",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("collection.itinerary.add"),
            )

        val postCollectionShareLink: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postCollectionShareLink",
                method = "POST",
                path = "/v1/collections/{collection_id}/share-link",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("Idempotent-Replayed", "X-Request-Id")),
                        com.mountaya.client.SuccessResponse(201, kotlin.collections.listOf("Idempotent-Replayed", "Location", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 429, 500, 504),
                tasks = kotlin.collections.listOf("collection.share_link.create"),
            )

        val postCollectionShareLinkRotate: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postCollectionShareLinkRotate",
                method = "POST",
                path = "/v1/collections/{collection_id}/share-link/rotate",
                idempotent = false,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 429, 500, 504),
                tasks = kotlin.collections.listOf("collection.share_link.rotate"),
            )

        val postCollectionZones: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postCollectionZones",
                method = "POST",
                path = "/v1/collections/{collection_id}/zones",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("collection.zone.add"),
            )

        val postItinerary: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postItinerary",
                method = "POST",
                path = "/v1/itineraries",
                idempotent = false,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(201, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "Location", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.create"),
            )

        val postItineraryCollab: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postItineraryCollab",
                method = "POST",
                path = "/v1/itineraries/{itinerary_id}/collab",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.collab.signal"),
            )

        val postItineraryDuplicate: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postItineraryDuplicate",
                method = "POST",
                path = "/v1/itineraries/{itinerary_id}/duplicate",
                idempotent = false,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(201, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "Location", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.duplicate"),
            )

        val postItineraryImport: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postItineraryImport",
                method = "POST",
                path = "/v1/itineraries/import",
                idempotent = false,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 55000,
                declaresIdempotencyKey = true,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(201, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "Location", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.import"),
            )

        val postItineraryPreview: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postItineraryPreview",
                method = "POST",
                path = "/v1/itineraries/preview",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 55000,
                declaresIdempotencyKey = true,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.preview"),
            )

        val postItineraryPublication: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postItineraryPublication",
                method = "POST",
                path = "/v1/itineraries/{itinerary_id}/publication",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.publish"),
            )

        val postItinerarySegment: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postItinerarySegment",
                method = "POST",
                path = "/v1/itineraries/{itinerary_id}/segments",
                idempotent = false,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(201, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "Location", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.segment.create", "itinerary.segment.insert"),
            )

        val postItinerarySegmentConvert: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postItinerarySegmentConvert",
                method = "POST",
                path = "/v1/itineraries/{itinerary_id}/segments/{segment_id}/convert",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.segment.convert"),
            )

        val postItinerarySegmentGroup: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postItinerarySegmentGroup",
                method = "POST",
                path = "/v1/itineraries/{itinerary_id}/segment-groups",
                idempotent = false,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(201, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "Location", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.segment_group.create"),
            )

        val postItinerarySegmentMerge: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postItinerarySegmentMerge",
                method = "POST",
                path = "/v1/itineraries/{itinerary_id}/segments/{segment_id}/merge",
                idempotent = false,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.segment.merge"),
            )

        val postItinerarySegmentSplit: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postItinerarySegmentSplit",
                method = "POST",
                path = "/v1/itineraries/{itinerary_id}/segments/{segment_id}/split",
                idempotent = false,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(201, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "Location", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.segment.split"),
            )

        val postItineraryShareLink: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postItineraryShareLink",
                method = "POST",
                path = "/v1/itineraries/{itinerary_id}/share-link",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("Idempotent-Replayed", "X-Request-Id")),
                        com.mountaya.client.SuccessResponse(201, kotlin.collections.listOf("Idempotent-Replayed", "Location", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.share_link.create"),
            )

        val postItineraryShareLinkRotate: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postItineraryShareLinkRotate",
                method = "POST",
                path = "/v1/itineraries/{itinerary_id}/share-link/rotate",
                idempotent = false,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.share_link.rotate"),
            )

        val postItineraryWaypoint: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postItineraryWaypoint",
                method = "POST",
                path = "/v1/itineraries/{itinerary_id}/waypoints",
                idempotent = false,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(201, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "Location", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("itinerary.waypoint.create"),
            )

        val postOrganizationLookup: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postOrganizationLookup",
                method = "POST",
                path = "/v1/organizations/lookup",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = false,
                        requiresSession = false,
                        requiresSecretKey = false,
                        takesNoCredential = true,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 404, 413, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val postZone: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postZone",
                method = "POST",
                path = "/v1/zones",
                idempotent = false,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(201, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "Location", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 413, 429, 500, 503, 504),
                tasks = kotlin.collections.listOf("zone.create"),
            )

        val postZoneCollab: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postZoneCollab",
                method = "POST",
                path = "/v1/zones/{zone_id}/collab",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("zone.collab.signal"),
            )

        val postZonePublication: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postZonePublication",
                method = "POST",
                path = "/v1/zones/{zone_id}/publication",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = true,
                isConditional = true,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "Idempotent-Replayed", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 412, 429, 500, 504),
                tasks = kotlin.collections.listOf("zone.publish"),
            )
    }

    /** The operations of the Tile API, in the model's order. */
    internal object Tiles {
        val getCollectionSpecification: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Tiles,
                operationId = "getCollectionSpecification",
                method = "GET",
                path = "/v1/collections/{collection_id}/specification",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = true,
                        requiresSession = false,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("Cache-Control", "RateLimit", "RateLimit-Policy", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 405, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getItinerariesSpecification: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Tiles,
                operationId = "getItinerariesSpecification",
                method = "GET",
                path = "/v1/itineraries/specification",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = true,
                        requiresSession = false,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("Cache-Control", "RateLimit", "RateLimit-Policy", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 405, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getItinerarySpecification: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Tiles,
                operationId = "getItinerarySpecification",
                method = "GET",
                path = "/v1/itineraries/{itinerary_id}/specification",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = true,
                        requiresSession = false,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("Cache-Control", "RateLimit", "RateLimit-Policy", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 405, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getOverlaySpecification: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Tiles,
                operationId = "getOverlaySpecification",
                method = "GET",
                path = "/v1/overlays/{overlay}/specification",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = true,
                        requiresSession = false,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("Cache-Control", "RateLimit", "RateLimit-Policy", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 405, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getTileLayers: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Tiles,
                operationId = "getTileLayers",
                method = "GET",
                path = "/v1/tile-layers",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = true,
                        requiresSession = false,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("Cache-Control", "RateLimit", "RateLimit-Policy", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 405, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getZonesSpecification: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Tiles,
                operationId = "getZonesSpecification",
                method = "GET",
                path = "/v1/zones/specification",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = true,
                        requiresSession = false,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("Cache-Control", "RateLimit", "RateLimit-Policy", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 405, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )
    }

    /** The operations of the Data API, in the model's order. */
    internal object Data {
        val analyzeGeometry: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Data,
                operationId = "analyzeGeometry",
                method = "POST",
                path = "/graphql",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = true,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Graphql,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.emptyList()),
                    ),
                errorStatuses = kotlin.collections.emptyList(),
                tasks = kotlin.collections.emptyList(),
            )

        val directions: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Data,
                operationId = "directions",
                method = "POST",
                path = "/graphql",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = true,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Graphql,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.emptyList()),
                    ),
                errorStatuses = kotlin.collections.emptyList(),
                tasks = kotlin.collections.emptyList(),
            )

        val exportItinerary: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Data,
                operationId = "exportItinerary",
                method = "POST",
                path = "/graphql",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = true,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Graphql,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.emptyList()),
                    ),
                errorStatuses = kotlin.collections.emptyList(),
                tasks = kotlin.collections.emptyList(),
            )

        val geocode: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Data,
                operationId = "geocode",
                method = "POST",
                path = "/graphql",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = true,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Graphql,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.emptyList()),
                    ),
                errorStatuses = kotlin.collections.emptyList(),
                tasks = kotlin.collections.emptyList(),
            )

        val isochrones: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Data,
                operationId = "isochrones",
                method = "POST",
                path = "/graphql",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = true,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Graphql,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.emptyList()),
                    ),
                errorStatuses = kotlin.collections.emptyList(),
                tasks = kotlin.collections.emptyList(),
            )

        val itinerary: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Data,
                operationId = "itinerary",
                method = "POST",
                path = "/graphql",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = true,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Graphql,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.emptyList()),
                    ),
                errorStatuses = kotlin.collections.emptyList(),
                tasks = kotlin.collections.emptyList(),
            )

        val matrix: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Data,
                operationId = "matrix",
                method = "POST",
                path = "/graphql",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = true,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Graphql,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.emptyList()),
                    ),
                errorStatuses = kotlin.collections.emptyList(),
                tasks = kotlin.collections.emptyList(),
            )

        val pois: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Data,
                operationId = "pois",
                method = "POST",
                path = "/graphql",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = true,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Graphql,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.emptyList()),
                    ),
                errorStatuses = kotlin.collections.emptyList(),
                tasks = kotlin.collections.emptyList(),
            )

        val reverseGeocode: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Data,
                operationId = "reverseGeocode",
                method = "POST",
                path = "/graphql",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = true,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Graphql,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.emptyList()),
                    ),
                errorStatuses = kotlin.collections.emptyList(),
                tasks = kotlin.collections.emptyList(),
            )

        val search: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Data,
                operationId = "search",
                method = "POST",
                path = "/graphql",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = true,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Graphql,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.emptyList()),
                    ),
                errorStatuses = kotlin.collections.emptyList(),
                tasks = kotlin.collections.emptyList(),
            )

        val suggestions: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Data,
                operationId = "suggestions",
                method = "POST",
                path = "/graphql",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = true,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Graphql,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.emptyList()),
                    ),
                errorStatuses = kotlin.collections.emptyList(),
                tasks = kotlin.collections.emptyList(),
            )

        val terrainGrid: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Data,
                operationId = "terrainGrid",
                method = "POST",
                path = "/graphql",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = true,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Graphql,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.emptyList()),
                    ),
                errorStatuses = kotlin.collections.emptyList(),
                tasks = kotlin.collections.emptyList(),
            )

        val terrainProfile: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Data,
                operationId = "terrainProfile",
                method = "POST",
                path = "/graphql",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = true,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Graphql,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.emptyList()),
                    ),
                errorStatuses = kotlin.collections.emptyList(),
                tasks = kotlin.collections.emptyList(),
            )

        val terrainSamples: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Data,
                operationId = "terrainSamples",
                method = "POST",
                path = "/graphql",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = false,
                        read = true,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = com.mountaya.client.SuccessKind.Graphql,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.emptyList()),
                    ),
                errorStatuses = kotlin.collections.emptyList(),
                tasks = kotlin.collections.emptyList(),
            )
    }

    /** The path every Data API operation is sent to under the Data API's origin, a call by declaration's included. */
    val dataEndpoint: kotlin.String = "/graphql"

    /** Every operation of the REST API the core prints, in the model's order. */
    val restOperations: kotlin.collections.List<com.mountaya.client.OperationFacts> =
        kotlin.collections.listOf(
            Rest.deleteCollection,
            Rest.deleteCollectionItinerary,
            Rest.deleteCollectionShareLink,
            Rest.deleteCollectionZone,
            Rest.deleteItinerary,
            Rest.deleteItineraryPublication,
            Rest.deleteItinerarySegment,
            Rest.deleteItinerarySegmentGroup,
            Rest.deleteItineraryShareLink,
            Rest.deleteItineraryWaypoint,
            Rest.deleteZone,
            Rest.deleteZonePublication,
            Rest.getActivities,
            Rest.getAgentRunEvents,
            Rest.getCollection,
            Rest.getCollectionItineraries,
            Rest.getCollectionShareLink,
            Rest.getCollectionZones,
            Rest.getCollections,
            Rest.getItineraries,
            Rest.getItinerary,
            Rest.getItineraryEvents,
            Rest.getItineraryExport,
            Rest.getItineraryPace,
            Rest.getItineraryShareLink,
            Rest.getLayers,
            Rest.getOrganizationEvents,
            Rest.getZone,
            Rest.getZoneEvents,
            Rest.getZones,
            Rest.patchCollection,
            Rest.patchCollectionItinerary,
            Rest.patchItinerary,
            Rest.patchItinerarySegment,
            Rest.patchItinerarySegmentGroup,
            Rest.patchItineraryWaypoint,
            Rest.patchZone,
            Rest.postAgentRun,
            Rest.postAgentRunFeedback,
            Rest.postAgentRunInput,
            Rest.postCollection,
            Rest.postCollectionItineraries,
            Rest.postCollectionShareLink,
            Rest.postCollectionShareLinkRotate,
            Rest.postCollectionZones,
            Rest.postItinerary,
            Rest.postItineraryCollab,
            Rest.postItineraryDuplicate,
            Rest.postItineraryImport,
            Rest.postItineraryPreview,
            Rest.postItineraryPublication,
            Rest.postItinerarySegment,
            Rest.postItinerarySegmentConvert,
            Rest.postItinerarySegmentGroup,
            Rest.postItinerarySegmentMerge,
            Rest.postItinerarySegmentSplit,
            Rest.postItineraryShareLink,
            Rest.postItineraryShareLinkRotate,
            Rest.postItineraryWaypoint,
            Rest.postOrganizationLookup,
            Rest.postZone,
            Rest.postZoneCollab,
            Rest.postZonePublication,
        )

    /** Every operation of the Tile API, in the model's order. */
    val tileOperations: kotlin.collections.List<com.mountaya.client.OperationFacts> =
        kotlin.collections.listOf(
            Tiles.getCollectionSpecification,
            Tiles.getItinerariesSpecification,
            Tiles.getItinerarySpecification,
            Tiles.getOverlaySpecification,
            Tiles.getTileLayers,
            Tiles.getZonesSpecification,
        )

    /** Every operation of the Data API, in the model's order. */
    val dataOperations: kotlin.collections.List<com.mountaya.client.OperationFacts> =
        kotlin.collections.listOf(
            Data.analyzeGeometry,
            Data.directions,
            Data.exportItinerary,
            Data.geocode,
            Data.isochrones,
            Data.itinerary,
            Data.matrix,
            Data.pois,
            Data.reverseGeocode,
            Data.search,
            Data.suggestions,
            Data.terrainGrid,
            Data.terrainProfile,
            Data.terrainSamples,
        )
}
