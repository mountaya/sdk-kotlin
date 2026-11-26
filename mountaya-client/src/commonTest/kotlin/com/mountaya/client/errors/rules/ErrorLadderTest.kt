package com.mountaya.client

import com.mountaya.client.generated.ApiErrorCode
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorLadderTest {
    private fun nameOf(
        status: Int,
        code: String? = null,
    ): String {
        val errors = if (code == null) emptyList() else listOf(ErrorEntry("", ApiErrorCode(code)))
        return ErrorLadder.exceptionFor(status, errors).name
    }

    @Test
    fun each4xxACallerActsOnHasARowOfItsOwn() {
        val rows =
            listOf(
                400 to "BadRequestError",
                401 to "AuthenticationError",
                402 to "BillingError",
                403 to "ForbiddenError",
                404 to "NotFoundError",
                405 to "MethodNotAllowedError",
                409 to "ConflictError",
                412 to "PreconditionFailedError",
                413 to "PayloadTooLargeError",
                429 to "RateLimitError",
            )
        for ((status, name) in rows) assertEquals(name, nameOf(status), "$status")
    }

    @Test
    fun a402IsOneBillingFailureWhateverItsCode() {
        for (code in listOf(null, "PAYMENT_REQUIRED", "SUBSCRIPTION_REQUIRED", "CARD_REQUIRED")) {
            assertEquals("BillingError", nameOf(402, code), "$code")
        }
    }

    /**
     * A ceiling is the one 429 coded `LIMIT_REACHED`, a rate-limit failure as a spent window is,
     * told from one by its code; the code on any other status classifies nothing.
     */
    @Test
    fun a429IsOneRateLimitFailureWhateverItsCodeACeilingIncluded() {
        for (code in listOf(null, "RATE_LIMITED", "LIMIT_REACHED")) {
            assertEquals("RateLimitError", nameOf(429, code), "$code")
        }
        assertEquals("BillingError", nameOf(402, "LIMIT_REACHED"))
        assertEquals("ForbiddenError", nameOf(403, "LIMIT_REACHED"))
    }

    @Test
    fun only409ReadsItsCodeAndReadsItExactly() {
        assertEquals("IdempotencyKeyInUseError", nameOf(409, "IDEMPOTENCY_KEY_IN_USE"))
        assertEquals("IdempotentAnswerUnavailableError", nameOf(409, "IDEMPOTENCY_ANSWER_UNAVAILABLE"))
        assertEquals("ConflictError", nameOf(409, "idempotency_key_in_use"))
        assertEquals("ConflictError", nameOf(409, "DUPLICATE"))
        assertEquals("ForbiddenError", nameOf(403, "IDEMPOTENCY_KEY_IN_USE"))
        assertEquals("PreconditionFailedError", nameOf(412, "IDEMPOTENCY_KEY_IN_USE"))
    }

    @Test
    fun onlyTheFirstEntryClassifiesA409() {
        val errors =
            listOf(ErrorEntry("", ApiErrorCode("DUPLICATE")), ErrorEntry("", ApiErrorCode("IDEMPOTENCY_KEY_IN_USE")))

        assertEquals("ConflictError", ErrorLadder.exceptionFor(409, errors).name)
    }

    @Test
    fun anySuccessRaisedAsAnErrorIsADataFailure() {
        for (status in listOf(200, 201, 299)) assertEquals("DataError", nameOf(status, "VALIDATION_FAILED"), "$status")
        assertEquals(200, ErrorLadder.exceptionFor(200, emptyList()).status)
    }

    @Test
    fun theWhole5xxRangeIsOneRowAndItsEdgesAreNot() {
        for (status in listOf(500, 501, 502, 503, 504, 599)) assertEquals("ServerError", nameOf(status), "$status")
        for (status in listOf(100, 199, 301, 304, 418, 422, 499, 600)) {
            assertEquals("ApiError", nameOf(status), "$status")
        }
    }

    @Test
    fun theFailureKeepsItsStatusAndItsEnvelope() {
        val errors = listOf(ErrorEntry("Teapot.", ApiErrorCode("TEAPOT")))
        val failure = ErrorLadder.exceptionFor(418, errors)

        assertEquals(418, failure.status)
        assertEquals(errors, failure.errors)
    }
}
