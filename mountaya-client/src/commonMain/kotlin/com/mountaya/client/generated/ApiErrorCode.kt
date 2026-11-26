// GENERATED FILE. DO NOT EDIT.
//
// Printed by `./gradlew generateClient` from sdk/model.json of mountaya/schemas, contract
// v0.9.0, model digest f9ded6dd4ca71cb8f89c6974c42adec77509ee9a19e5afd01c31b6ea9dcc33ea.
// Every name below is the contract's, verbatim, but an enum member's, printed as its words
// (SPEC.md section 3). A shape that is wrong here is a defect in the contract's description,
// and the next print overwrites any edit.

package com.mountaya.client.generated

/**
 * ApiErrorCode is the code an error's entry carries under `extensions.code`, on every surface the
 * client calls: one vocabulary, the union of the codes the REST API's and the Tile API's
 * `ErrorExtensionsCode` and the Data API's `ErrorCode` declare, which an error's `code`, each of its
 * entries' `code` and its `hasCode` take. A code a later release of the API sends is kept exactly as
 * sent and never refused.
 *
 * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
public value class ApiErrorCode(
    /** The value, exactly as the wire carries it. */
    public val value: kotlin.String,
) {
    override fun toString(): kotlin.String = value

    /** The values the contract names. */
    public companion object {
        /** The `BAD_REQUEST` code, which the REST API, the Tile API and the Data API declare. */
        public val BadRequest: ApiErrorCode = ApiErrorCode("BAD_REQUEST")

        /** The `VALIDATION_FAILED` code, which the REST API, the Tile API and the Data API declare. */
        public val ValidationFailed: ApiErrorCode = ApiErrorCode("VALIDATION_FAILED")

        /** The `UNAUTHORIZED` code, which the REST API, the Tile API and the Data API declare. */
        public val Unauthorized: ApiErrorCode = ApiErrorCode("UNAUTHORIZED")

        /** The `PAYMENT_REQUIRED` code, which the REST API, the Tile API and the Data API declare. */
        public val PaymentRequired: ApiErrorCode = ApiErrorCode("PAYMENT_REQUIRED")

        /** The `LIMIT_REACHED` code, which the REST API declares. */
        public val LimitReached: ApiErrorCode = ApiErrorCode("LIMIT_REACHED")

        /** The `FORBIDDEN` code, which the REST API, the Tile API and the Data API declare. */
        public val Forbidden: ApiErrorCode = ApiErrorCode("FORBIDDEN")

        /** The `NOT_FOUND` code, which the REST API, the Tile API and the Data API declare. */
        public val NotFound: ApiErrorCode = ApiErrorCode("NOT_FOUND")

        /** The `METHOD_NOT_ALLOWED` code, which the REST API, the Tile API and the Data API declare. */
        public val MethodNotAllowed: ApiErrorCode = ApiErrorCode("METHOD_NOT_ALLOWED")

        /** The `CONFLICT` code, which the REST API and the Data API declare. */
        public val Conflict: ApiErrorCode = ApiErrorCode("CONFLICT")

        /** The `AI_PROVIDER_NOT_CONFIGURED` code, which the REST API declares. */
        public val AiProviderNotConfigured: ApiErrorCode = ApiErrorCode("AI_PROVIDER_NOT_CONFIGURED")

        /** The `IDEMPOTENCY_KEY_IN_USE` code, which the REST API declares. */
        public val IdempotencyKeyInUse: ApiErrorCode = ApiErrorCode("IDEMPOTENCY_KEY_IN_USE")

        /** The `IDEMPOTENCY_ANSWER_UNAVAILABLE` code, which the REST API declares. */
        public val IdempotencyAnswerUnavailable: ApiErrorCode = ApiErrorCode("IDEMPOTENCY_ANSWER_UNAVAILABLE")

        /** The `REDELIVERY_IN_PROGRESS` code, which the REST API declares. */
        public val RedeliveryInProgress: ApiErrorCode = ApiErrorCode("REDELIVERY_IN_PROGRESS")

        /** The `SECRET_ROTATION_IN_OVERLAP` code, which the REST API declares. */
        public val SecretRotationInOverlap: ApiErrorCode = ApiErrorCode("SECRET_ROTATION_IN_OVERLAP")

        /** The `PRECONDITION_FAILED` code, which the REST API declares. */
        public val PreconditionFailed: ApiErrorCode = ApiErrorCode("PRECONDITION_FAILED")

        /** The `PAYLOAD_TOO_LARGE` code, which the REST API and the Data API declare. */
        public val PayloadTooLarge: ApiErrorCode = ApiErrorCode("PAYLOAD_TOO_LARGE")

        /** The `TOO_MANY_REQUESTS` code, which the REST API, the Tile API and the Data API declare. */
        public val TooManyRequests: ApiErrorCode = ApiErrorCode("TOO_MANY_REQUESTS")

        /** The `INTERNAL_ERROR` code, which the REST API, the Tile API and the Data API declare. */
        public val InternalError: ApiErrorCode = ApiErrorCode("INTERNAL_ERROR")

        /** The `NOT_IMPLEMENTED` code, which the REST API and the Data API declare. */
        public val NotImplemented: ApiErrorCode = ApiErrorCode("NOT_IMPLEMENTED")

        /** The `BAD_GATEWAY` code, which the REST API and the Data API declare. */
        public val BadGateway: ApiErrorCode = ApiErrorCode("BAD_GATEWAY")

        /** The `SERVICE_UNAVAILABLE` code, which the REST API and the Data API declare. */
        public val ServiceUnavailable: ApiErrorCode = ApiErrorCode("SERVICE_UNAVAILABLE")

        /** The `GATEWAY_TIMEOUT` code, which the REST API, the Tile API and the Data API declare. */
        public val GatewayTimeout: ApiErrorCode = ApiErrorCode("GATEWAY_TIMEOUT")
    }
}
