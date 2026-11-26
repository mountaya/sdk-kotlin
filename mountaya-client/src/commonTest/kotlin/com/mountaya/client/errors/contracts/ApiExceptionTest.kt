package com.mountaya.client

import com.mountaya.client.generated.ApiErrorCode
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

class ApiExceptionTest {
    @Test
    fun theClassifyingCodeIsTheFirstEntrys() {
        val failure =
            ApiException.of(
                402,
                listOf(
                    ErrorEntry("Add a card.", ApiErrorCode("CARD_REQUIRED")),
                    ErrorEntry("An invoice is still owed.", ApiErrorCode("PAYMENT_REQUIRED")),
                ),
            )

        assertEquals(ApiErrorCode("CARD_REQUIRED"), failure.code)
        assertIs<BillingException>(failure)
        assertEquals(2, failure.errors.size)
    }

    @Test
    fun aResponseWithNoEnvelopeHasNoCodeAndItsStatusStillClassifiesIt() {
        val failure = ApiException.of(503)

        assertNull(failure.code)
        assertIs<ServerException>(failure)
        assertEquals(503, failure.status)
        assertEquals("503", failure.message)
    }

    @Test
    fun everyClassNamesItsRowAndNoTwoShareAName() {
        val names =
            listOf(
                BadRequestException() to "BadRequestError",
                AuthenticationException() to "AuthenticationError",
                BillingException() to "BillingError",
                ForbiddenException() to "ForbiddenError",
                NotFoundException() to "NotFoundError",
                MethodNotAllowedException() to "MethodNotAllowedError",
                ConflictException() to "ConflictError",
                IdempotencyKeyInUseException() to "IdempotencyKeyInUseError",
                IdempotentAnswerUnavailableException() to "IdempotentAnswerUnavailableError",
                PreconditionFailedException() to "PreconditionFailedError",
                PayloadTooLargeException() to "PayloadTooLargeError",
                RateLimitException() to "RateLimitError",
                ServerException(500) to "ServerError",
                DataException() to "DataError",
                ApiException.of(418) to "ApiError",
            )

        for ((failure, name) in names) assertEquals(name, failure.name)
        assertEquals(names.size, names.map { it.second }.toSet().size)
    }

    @Test
    fun eachClassBuiltDirectlyCarriesTheStatusOfItsRow() {
        val statuses =
            listOf(
                BadRequestException() to 400,
                AuthenticationException() to 401,
                BillingException() to 402,
                ForbiddenException() to 403,
                NotFoundException() to 404,
                MethodNotAllowedException() to 405,
                ConflictException() to 409,
                IdempotencyKeyInUseException() to 409,
                IdempotentAnswerUnavailableException() to 409,
                PreconditionFailedException() to 412,
                PayloadTooLargeException() to 413,
                RateLimitException() to 429,
                ServerException(504) to 504,
            )

        for ((failure, status) in statuses) assertEquals(status, failure.status, failure.name)
    }

    /**
     * Catching ConflictException catches both 409s that answer a key, and catching ApiException
     * catches every error the API answered, one under a success included.
     */
    @Test
    fun bothKeyedConflictsAreConflictsAndADataFailureIsOneTheApiAnswered() {
        assertIs<ConflictException>(
            ApiException.of(409, listOf(ErrorEntry("", ApiErrorCode("IDEMPOTENCY_KEY_IN_USE")))),
        )
        assertIs<ConflictException>(
            ApiException.of(409, listOf(ErrorEntry("", ApiErrorCode("IDEMPOTENCY_ANSWER_UNAVAILABLE")))),
        )
        assertIs<ApiException>(DataException(listOf(ErrorEntry("Bad.", ApiErrorCode("VALIDATION_FAILED")))))
    }

    @Test
    fun aServerFailureIsA5xx() {
        for (status in listOf(499, 600, 200)) {
            assertFailsWith<IllegalArgumentException>("$status") { ServerException(status) }
        }
    }

    @Test
    fun theMessageIsTheStatusTheCodeAndTheFirstMessage() {
        assertEquals("404", NotFoundException().message)
        assertEquals("404 NOT_FOUND", NotFoundException(listOf(ErrorEntry("", ApiErrorCode("NOT_FOUND")))).message)
        assertEquals("404: Gone.", NotFoundException(listOf(ErrorEntry("Gone."))).message)
    }

    @Test
    fun anErrorCarriesWhatItsAnswerSaidEachNullWhereItSaidNothing() {
        val metadata = JsonObject(mapOf("holder" to JsonObject(mapOf("id" to JsonPrimitive("m1")))))
        val error =
            ApiException.of(
                409,
                listOf(ErrorEntry("locked", ApiErrorCode("CONFLICT"))),
                metadata = metadata,
                retryAfter = 3.seconds,
                requestId = "4bf9",
                rateLimit = RateLimitState(listOf(RateLimitPolicy("minute", 60))),
                etag = "\"d4\"",
                idempotencyKey = "order-1",
            )

        assertIs<ConflictException>(error)
        assertEquals(metadata, error.metadata)
        assertEquals(3.seconds, error.retryAfter)
        assertEquals("4bf9", error.requestId)
        assertEquals(
            "minute",
            error.rateLimit
                ?.policies
                ?.single()
                ?.name,
        )
        assertEquals("\"d4\"", error.etag)
        assertEquals("order-1", error.idempotencyKey)
        val bare = BadRequestException()
        assertNull(bare.metadata)
        assertNull(bare.retryAfter)
        assertNull(bare.requestId)
        assertNull(bare.idempotencyKey)
    }

    @Test
    fun anUnavailableAnswerNamesTheFirstAnswerOfAWriteThatWasMade() {
        val error =
            ApiException.of(
                409,
                listOf(ErrorEntry("made", ApiErrorCode("IDEMPOTENCY_ANSWER_UNAVAILABLE"))),
                metadata =
                    JsonObject(
                        mapOf("status" to JsonPrimitive(201), "location" to JsonPrimitive("/v1/things/t1")),
                    ),
            )

        assertEquals(
            IdempotentAnswer(201, "/v1/things/t1", null),
            assertIs<IdempotentAnswerUnavailableException>(error).answer,
        )
    }
}
