package com.mountaya.client

import com.mountaya.client.generated.ApiErrorCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class AnswerErrorTest {
    private val now = Instant.fromEpochSeconds(1_800_000_000)

    @Test
    fun anErrorCarriesTheAnswersRequestIdTagWaitAndRateLimitStateAsSent() {
        val error =
            AnswerError.exception(
                status = 412,
                headers =
                    FoldedHeaders(
                        mapOf(
                            "x-request-id" to "a3ce929d0e0e47364bf92f3577b34da6",
                            "ETag" to "\"d43\"",
                            "Retry-After" to "7",
                            "RateLimit-Policy" to "\"minute\";q=60;w=60",
                            "RateLimit" to "\"minute\";r=0;t=17",
                        ),
                    ),
                envelope =
                    ErrorEnvelope.read(
                        """{"errors":[{"message":"stale","extensions":{"code":"PRECONDITION_FAILED"}}]}"""
                            .encodeToByteArray(),
                    ),
                now = now,
                idempotencyKey = "order-7f3a",
            )

        assertIs<PreconditionFailedException>(error)
        assertEquals(ApiErrorCode("PRECONDITION_FAILED"), error.code)
        assertEquals("a3ce929d0e0e47364bf92f3577b34da6", error.requestId)
        assertEquals("\"d43\"", error.etag)
        assertEquals(7.seconds, error.retryAfter)
        assertEquals(
            0L,
            error.rateLimit
                ?.policies
                ?.first()
                ?.remaining,
        )
        assertEquals("order-7f3a", error.idempotencyKey)
    }

    @Test
    fun aWaitNamedAsAnHttpDateIsCarriedInWholeSecondsRoundedUp() {
        val headers = FoldedHeaders(mapOf("Retry-After" to "Sun, 06 Nov 1994 08:49:37 GMT"))
        val earlier = Instant.fromEpochSeconds(784_111_777) - 3_500.milliseconds

        assertEquals(4.seconds, AnswerError.retryAfter(headers, earlier))
        assertNull(AnswerError.retryAfter(FoldedHeaders(mapOf("Retry-After" to "soon")), now))
        assertNull(AnswerError.retryAfter(FoldedHeaders(emptyMap()), now))
    }
}
