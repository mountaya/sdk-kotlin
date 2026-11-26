// GENERATED FILE. DO NOT EDIT.
//
// Printed by `./gradlew generateClient` from sdk/model.json of mountaya/schemas, contract
// v0.9.0, model digest f9ded6dd4ca71cb8f89c6974c42adec77509ee9a19e5afd01c31b6ea9dcc33ea.
// Every name below is the contract's, verbatim, but an enum member's, printed as its words
// (SPEC.md section 3). A shape that is wrong here is a defect in the contract's description,
// and the next print overwrites any edit.

@file:kotlin.OptIn(com.mountaya.client.InternalMountayaApi::class)

package com.mountaya.admin.generated

/** OperationTable is what the contract declares about each printed operation, one property per operation and nothing inferred from a method. A test holds it to `sdk/model.json` fact by fact. */
internal object OperationTable {
    /** The operations of the REST API, in the model's order. */
    internal object Rest {
        val deleteMyInvitation: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "deleteMyInvitation",
                method = "DELETE",
                path = "/v1/me/invitations/{invitation_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = true,
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
                tasks = kotlin.collections.listOf("organization.invitation.decline"),
            )

        val deleteOrganizationAiProviderConfig: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "deleteOrganizationAiProviderConfig",
                method = "DELETE",
                path = "/v1/organizations/{organization_id}/ai-provider-config",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 409, 412, 429, 500, 504),
                tasks = kotlin.collections.listOf("organization.ai_provider_config.delete"),
            )

        val deleteOrganizationBillingPaymentMethod: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "deleteOrganizationBillingPaymentMethod",
                method = "DELETE",
                path = "/v1/organizations/{organization_id}/billing/payment-methods/{payment_method_id}",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 409, 429, 500, 503, 504),
                tasks = kotlin.collections.listOf("organization.billing.payment_method.detach"),
            )

        val deleteOrganizationBillingSubscription: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "deleteOrganizationBillingSubscription",
                method = "DELETE",
                path = "/v1/organizations/{organization_id}/billing/subscription",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 409, 429, 500, 503, 504),
                tasks = kotlin.collections.listOf("organization.billing.subscription.cancel"),
            )

        val deleteOrganizationInvitation: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "deleteOrganizationInvitation",
                method = "DELETE",
                path = "/v1/organizations/{organization_id}/invitations/{invitation_id}",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 409, 429, 500, 504),
                tasks = kotlin.collections.listOf("organization.invitation.revoke"),
            )

        val deleteOrganizationMember: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "deleteOrganizationMember",
                method = "DELETE",
                path = "/v1/organizations/{organization_id}/members/{user_id}",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 409, 429, 500, 504),
                tasks = kotlin.collections.listOf("organization.member.delete", "organization.member.leave"),
            )

        val deleteOrganizationPublishableKey: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "deleteOrganizationPublishableKey",
                method = "DELETE",
                path = "/v1/organizations/{organization_id}/publishable-keys/{key_id}",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 409, 412, 429, 500, 504),
                tasks = kotlin.collections.listOf("organization.publishable_key.revoke"),
            )

        val deleteOrganizationWebhookEndpoint: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "deleteOrganizationWebhookEndpoint",
                method = "DELETE",
                path = "/v1/organizations/{organization_id}/webhook-endpoints/{webhook_endpoint_id}",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 409, 412, 429, 500, 504),
                tasks = kotlin.collections.listOf("organization.webhook_endpoint.delete"),
            )

        val getAiProviders: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getAiProviders",
                method = "GET",
                path = "/v1/ai-providers",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getBillingProducts: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getBillingProducts",
                method = "GET",
                path = "/v1/billing/products",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getMyInvitations: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getMyInvitations",
                method = "GET",
                path = "/v1/me/invitations",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = true,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination =
                    com.mountaya.client.Pagination(
                        sort = kotlin.collections.listOf(),
                        defaultSort = null,
                    ),
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getMyOrganizationNotificationPreferences: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getMyOrganizationNotificationPreferences",
                method = "GET",
                path = "/v1/me/organizations/{organization_id}/notification-preferences",
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

        val getMyOrganizations: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getMyOrganizations",
                method = "GET",
                path = "/v1/me/organizations",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = true,
                        read = false,
                    ),
                timeoutMs = 30000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination =
                    com.mountaya.client.Pagination(
                        sort = kotlin.collections.listOf(),
                        defaultSort = null,
                    ),
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getMyPreferences: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getMyPreferences",
                method = "GET",
                path = "/v1/me/preferences",
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

        val getOrganization: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganization",
                method = "GET",
                path = "/v1/organizations/{organization_id}",
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

        val getOrganizationAiProviderConfig: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganizationAiProviderConfig",
                method = "GET",
                path = "/v1/organizations/{organization_id}/ai-provider-config",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getOrganizationBillingInfo: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganizationBillingInfo",
                method = "GET",
                path = "/v1/organizations/{organization_id}/billing/info",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getOrganizationBillingInvoices: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganizationBillingInvoices",
                method = "GET",
                path = "/v1/organizations/{organization_id}/billing/invoices",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                pagination =
                    com.mountaya.client.Pagination(
                        sort = kotlin.collections.listOf(),
                        defaultSort = null,
                    ),
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getOrganizationBillingPaymentMethods: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganizationBillingPaymentMethods",
                method = "GET",
                path = "/v1/organizations/{organization_id}/billing/payment-methods",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                pagination =
                    com.mountaya.client.Pagination(
                        sort = kotlin.collections.listOf(),
                        defaultSort = null,
                    ),
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getOrganizationBillingPromotionCode: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganizationBillingPromotionCode",
                method = "GET",
                path = "/v1/organizations/{organization_id}/billing/promotion-codes/{code}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getOrganizationBillingSubscription: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganizationBillingSubscription",
                method = "GET",
                path = "/v1/organizations/{organization_id}/billing/subscription",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getOrganizationInvitation: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganizationInvitation",
                method = "GET",
                path = "/v1/organizations/{organization_id}/invitations/{invitation_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getOrganizationInvitations: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganizationInvitations",
                method = "GET",
                path = "/v1/organizations/{organization_id}/invitations",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                pagination =
                    com.mountaya.client.Pagination(
                        sort = kotlin.collections.listOf("-created_at", "created_at"),
                        defaultSort = "-created_at",
                    ),
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getOrganizationMembers: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganizationMembers",
                method = "GET",
                path = "/v1/organizations/{organization_id}/members",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                pagination =
                    com.mountaya.client.Pagination(
                        sort = kotlin.collections.listOf("joined_at", "-joined_at"),
                        defaultSort = "joined_at",
                    ),
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getOrganizationPublishableKey: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganizationPublishableKey",
                method = "GET",
                path = "/v1/organizations/{organization_id}/publishable-keys/{key_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getOrganizationPublishableKeys: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganizationPublishableKeys",
                method = "GET",
                path = "/v1/organizations/{organization_id}/publishable-keys",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                pagination =
                    com.mountaya.client.Pagination(
                        sort = kotlin.collections.listOf("-created_at", "created_at", "name", "-name"),
                        defaultSort = "-created_at",
                    ),
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getOrganizationSettings: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganizationSettings",
                method = "GET",
                path = "/v1/organizations/{organization_id}/settings",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getOrganizationUsageData: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganizationUsageData",
                method = "GET",
                path = "/v1/organizations/{organization_id}/usage/data",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getOrganizationUsageTiles: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganizationUsageTiles",
                method = "GET",
                path = "/v1/organizations/{organization_id}/usage/tiles",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getOrganizationWebhookDeliveries: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganizationWebhookDeliveries",
                method = "GET",
                path = "/v1/organizations/{organization_id}/webhook-deliveries",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                pagination =
                    com.mountaya.client.Pagination(
                        sort = kotlin.collections.listOf(),
                        defaultSort = null,
                    ),
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getOrganizationWebhookDelivery: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganizationWebhookDelivery",
                method = "GET",
                path = "/v1/organizations/{organization_id}/webhook-deliveries/{delivery_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getOrganizationWebhookEndpoint: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganizationWebhookEndpoint",
                method = "GET",
                path = "/v1/organizations/{organization_id}/webhook-endpoints/{webhook_endpoint_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("ETag", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getOrganizationWebhookEndpoints: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganizationWebhookEndpoints",
                method = "GET",
                path = "/v1/organizations/{organization_id}/webhook-endpoints",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                pagination =
                    com.mountaya.client.Pagination(
                        sort = kotlin.collections.listOf(),
                        defaultSort = null,
                    ),
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getOrganizationWebhookEvent: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganizationWebhookEvent",
                method = "GET",
                path = "/v1/organizations/{organization_id}/webhook-events/{event_id}",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getOrganizationWebhookEvents: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganizationWebhookEvents",
                method = "GET",
                path = "/v1/organizations/{organization_id}/webhook-events",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                pagination =
                    com.mountaya.client.Pagination(
                        sort = kotlin.collections.listOf(),
                        defaultSort = null,
                    ),
                success = com.mountaya.client.SuccessKind.Envelope,
                successResponses =
                    kotlin.collections.listOf(
                        com.mountaya.client.SuccessResponse(200, kotlin.collections.listOf("X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getOrganizationWebhookHealth: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getOrganizationWebhookHealth",
                method = "GET",
                path = "/v1/organizations/{organization_id}/webhook-health",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val getWebhookEventTypes: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "getWebhookEventTypes",
                method = "GET",
                path = "/v1/webhook-event-types",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Optional,
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 429, 500, 504),
                tasks = kotlin.collections.emptyList(),
            )

        val patchMyOrganizationNotificationPreferences: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "patchMyOrganizationNotificationPreferences",
                method = "PATCH",
                path = "/v1/me/organizations/{organization_id}/notification-preferences",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("organization.member.notification_preferences.update"),
            )

        val patchMyPreferences: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "patchMyPreferences",
                method = "PATCH",
                path = "/v1/me/preferences",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("user.preferences.update"),
            )

        val patchOrganization: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "patchOrganization",
                method = "PATCH",
                path = "/v1/organizations/{organization_id}",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 409, 412, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("organization.update"),
            )

        val patchOrganizationAiProviderConfig: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "patchOrganizationAiProviderConfig",
                method = "PATCH",
                path = "/v1/organizations/{organization_id}/ai-provider-config",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 409, 412, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("organization.ai_provider_config.update"),
            )

        val patchOrganizationBillingInfo: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "patchOrganizationBillingInfo",
                method = "PATCH",
                path = "/v1/organizations/{organization_id}/billing/info",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 409, 413, 429, 500, 503, 504),
                tasks = kotlin.collections.listOf("organization.billing.info.update"),
            )

        val patchOrganizationMember: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "patchOrganizationMember",
                method = "PATCH",
                path = "/v1/organizations/{organization_id}/members/{user_id}",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 409, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("organization.member.update"),
            )

        val patchOrganizationPublishableKey: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "patchOrganizationPublishableKey",
                method = "PATCH",
                path = "/v1/organizations/{organization_id}/publishable-keys/{key_id}",
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
                tasks = kotlin.collections.listOf("organization.publishable_key.update"),
            )

        val patchOrganizationSettings: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "patchOrganizationSettings",
                method = "PATCH",
                path = "/v1/organizations/{organization_id}/settings",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 409, 412, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("organization.settings.update"),
            )

        val patchOrganizationWebhookEndpoint: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "patchOrganizationWebhookEndpoint",
                method = "PATCH",
                path = "/v1/organizations/{organization_id}/webhook-endpoints/{webhook_endpoint_id}",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 409, 412, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("organization.webhook_endpoint.update"),
            )

        val postMyInvitationAccept: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postMyInvitationAccept",
                method = "POST",
                path = "/v1/me/invitations/{invitation_id}/accept",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required,
                        acceptsShareToken = false,
                        requiresSession = true,
                        requiresSecretKey = false,
                        takesNoCredential = false,
                        pendingInvitee = true,
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
                tasks = kotlin.collections.listOf("organization.invitation.accept"),
            )

        val postOrganization: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postOrganization",
                method = "POST",
                path = "/v1/organizations",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 409, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("organization.create"),
            )

        val postOrganizationAiProviderConfig: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postOrganizationAiProviderConfig",
                method = "POST",
                path = "/v1/organizations/{organization_id}/ai-provider-config",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 409, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("organization.ai_provider_config.create"),
            )

        val postOrganizationBillingPaymentMethod: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postOrganizationBillingPaymentMethod",
                method = "POST",
                path = "/v1/organizations/{organization_id}/billing/payment-methods",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 413, 429, 500, 503, 504),
                tasks = kotlin.collections.listOf("organization.billing.payment_method.attach"),
            )

        val postOrganizationBillingPaymentMethodSetDefault: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postOrganizationBillingPaymentMethodSetDefault",
                method = "POST",
                path = "/v1/organizations/{organization_id}/billing/payment-methods/{payment_method_id}/set-default",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 409, 429, 500, 503, 504),
                tasks = kotlin.collections.listOf("organization.billing.payment_method.set_default"),
            )

        val postOrganizationBillingPaymentMethodSetup: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postOrganizationBillingPaymentMethodSetup",
                method = "POST",
                path = "/v1/organizations/{organization_id}/billing/payment-methods/setup",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 409, 429, 500, 504),
                tasks = kotlin.collections.listOf("organization.billing.payment_method.setup"),
            )

        val postOrganizationBillingProvision: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postOrganizationBillingProvision",
                method = "POST",
                path = "/v1/organizations/{organization_id}/billing/provision",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 409, 413, 429, 500, 503, 504),
                tasks = kotlin.collections.listOf("organization.billing.provision"),
            )

        val postOrganizationBillingSubscription: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postOrganizationBillingSubscription",
                method = "POST",
                path = "/v1/organizations/{organization_id}/billing/subscription",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 409, 429, 500, 503, 504),
                tasks = kotlin.collections.listOf("organization.billing.subscription.run"),
            )

        val postOrganizationInvitation: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postOrganizationInvitation",
                method = "POST",
                path = "/v1/organizations/{organization_id}/invitations",
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
                        com.mountaya.client.SuccessResponse(201, kotlin.collections.listOf("Idempotent-Replayed", "Location", "X-Request-Id")),
                    ),
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 409, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("organization.invitation.create"),
            )

        val postOrganizationPublishableKey: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postOrganizationPublishableKey",
                method = "POST",
                path = "/v1/organizations/{organization_id}/publishable-keys",
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
                tasks = kotlin.collections.listOf("organization.publishable_key.create"),
            )

        val postOrganizationWebhookDelivery: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postOrganizationWebhookDelivery",
                method = "POST",
                path = "/v1/organizations/{organization_id}/webhook-deliveries",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 409, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("organization.webhook_delivery.resend", "organization.webhook_delivery.test", "organization.webhook_delivery.redeliver"),
            )

        val postOrganizationWebhookEndpoint: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postOrganizationWebhookEndpoint",
                method = "POST",
                path = "/v1/organizations/{organization_id}/webhook-endpoints",
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
                errorStatuses = kotlin.collections.listOf(400, 401, 403, 404, 409, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("organization.webhook_endpoint.create"),
            )

        val postSession: com.mountaya.client.OperationFacts =
            com.mountaya.client.OperationFacts(
                surface = com.mountaya.client.Surface.Rest,
                operationId = "postSession",
                method = "POST",
                path = "/v1/sessions",
                idempotent = true,
                credentials =
                    com.mountaya.client.CredentialDeclaration(
                        actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None,
                        acceptsShareToken = false,
                        requiresSession = false,
                        requiresSecretKey = true,
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
                errorStatuses = kotlin.collections.listOf(400, 401, 402, 403, 404, 413, 429, 500, 504),
                tasks = kotlin.collections.listOf("session.create"),
            )
    }

    /** Every operation of the REST API the admin unit prints, in the model's order. */
    val restOperations: kotlin.collections.List<com.mountaya.client.OperationFacts> =
        kotlin.collections.listOf(
            Rest.deleteMyInvitation,
            Rest.deleteOrganizationAiProviderConfig,
            Rest.deleteOrganizationBillingPaymentMethod,
            Rest.deleteOrganizationBillingSubscription,
            Rest.deleteOrganizationInvitation,
            Rest.deleteOrganizationMember,
            Rest.deleteOrganizationPublishableKey,
            Rest.deleteOrganizationWebhookEndpoint,
            Rest.getAiProviders,
            Rest.getBillingProducts,
            Rest.getMyInvitations,
            Rest.getMyOrganizationNotificationPreferences,
            Rest.getMyOrganizations,
            Rest.getMyPreferences,
            Rest.getOrganization,
            Rest.getOrganizationAiProviderConfig,
            Rest.getOrganizationBillingInfo,
            Rest.getOrganizationBillingInvoices,
            Rest.getOrganizationBillingPaymentMethods,
            Rest.getOrganizationBillingPromotionCode,
            Rest.getOrganizationBillingSubscription,
            Rest.getOrganizationInvitation,
            Rest.getOrganizationInvitations,
            Rest.getOrganizationMembers,
            Rest.getOrganizationPublishableKey,
            Rest.getOrganizationPublishableKeys,
            Rest.getOrganizationSettings,
            Rest.getOrganizationUsageData,
            Rest.getOrganizationUsageTiles,
            Rest.getOrganizationWebhookDeliveries,
            Rest.getOrganizationWebhookDelivery,
            Rest.getOrganizationWebhookEndpoint,
            Rest.getOrganizationWebhookEndpoints,
            Rest.getOrganizationWebhookEvent,
            Rest.getOrganizationWebhookEvents,
            Rest.getOrganizationWebhookHealth,
            Rest.getWebhookEventTypes,
            Rest.patchMyOrganizationNotificationPreferences,
            Rest.patchMyPreferences,
            Rest.patchOrganization,
            Rest.patchOrganizationAiProviderConfig,
            Rest.patchOrganizationBillingInfo,
            Rest.patchOrganizationMember,
            Rest.patchOrganizationPublishableKey,
            Rest.patchOrganizationSettings,
            Rest.patchOrganizationWebhookEndpoint,
            Rest.postMyInvitationAccept,
            Rest.postOrganization,
            Rest.postOrganizationAiProviderConfig,
            Rest.postOrganizationBillingPaymentMethod,
            Rest.postOrganizationBillingPaymentMethodSetDefault,
            Rest.postOrganizationBillingPaymentMethodSetup,
            Rest.postOrganizationBillingProvision,
            Rest.postOrganizationBillingSubscription,
            Rest.postOrganizationInvitation,
            Rest.postOrganizationPublishableKey,
            Rest.postOrganizationWebhookDelivery,
            Rest.postOrganizationWebhookEndpoint,
            Rest.postSession,
        )
}
