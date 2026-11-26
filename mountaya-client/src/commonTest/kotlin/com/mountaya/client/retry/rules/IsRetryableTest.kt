package com.mountaya.client

import com.mountaya.client.generated.ApiErrorCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsRetryableTest {
    private val policy = RetryPolicy()

    private fun repeated(
        status: Int?,
        idempotent: Boolean = false,
        keyed: Boolean = false,
        code: ApiErrorCode? = null,
        attempt: Int = 0,
        policy: RetryPolicy = this.policy,
    ) = isRetryable(attempt, status, code, idempotent, keyed, policy)

    @Test
    fun aTooManyRequestsIsRepeatedWhateverTheOperationDeclares() {
        assertTrue(repeated(429))
        assertTrue(repeated(429, idempotent = true))
        assertTrue(repeated(429, keyed = true))
    }

    /**
     * A ceiling names no wait and no wait lifts it, so its 429 settles at once, whatever the
     * operation declares and whatever the attempts left; the code read is the contract's spelling.
     */
    @Test
    fun aCeilingIsNeverRepeated() {
        assertFalse(repeated(429, code = ApiErrorCode.LimitReached))
        assertFalse(repeated(429, idempotent = true, code = ApiErrorCode.LimitReached))
        assertFalse(repeated(429, keyed = true, code = ApiErrorCode.LimitReached))
        assertFalse(
            repeated(429, idempotent = true, keyed = true, code = ApiErrorCode.LimitReached, policy = RetryPolicy(9)),
        )
        assertTrue(repeated(429, code = ApiErrorCode("RATE_LIMITED")))
        assertTrue(repeated(429, code = ApiErrorCode("limit_reached")))
        assertTrue(repeated(503, idempotent = true, code = ApiErrorCode.LimitReached))
    }

    @Test
    fun aGatewayFailureIsRepeatedWhereTheOperationIsIdempotentOrTheRequestCarriedAKey() {
        for (status in listOf(502, 503, 504)) {
            assertTrue(repeated(status, idempotent = true), "$status")
            assertTrue(repeated(status, keyed = true), "$status")
            assertFalse(repeated(status), "$status")
        }
    }

    @Test
    fun aNetworkFailureIsRepeatedWhereTheOperationIsIdempotentOrTheRequestCarriedAKey() {
        assertTrue(repeated(null, idempotent = true))
        assertTrue(repeated(null, keyed = true))
        assertFalse(repeated(null))
    }

    /**
     * A 409 whose key is in use asks whether the first attempt has finished, so it is repeated
     * where the request carried a key, past every ceiling of attempts, and no other 409 is.
     */
    @Test
    fun aKeyStillInUseIsRepeatedWhereTheRequestCarriedOneAndCountedAgainstNoCeiling() {
        assertTrue(repeated(409, keyed = true, code = ApiErrorCode.IdempotencyKeyInUse))
        assertTrue(repeated(409, keyed = true, code = ApiErrorCode.IdempotencyKeyInUse, attempt = 7))
        assertTrue(
            repeated(409, keyed = true, code = ApiErrorCode.IdempotencyKeyInUse, policy = RetryPolicy(maxRetries = 0)),
        )
        assertFalse(repeated(409, idempotent = true, code = ApiErrorCode.IdempotencyKeyInUse))
        assertFalse(repeated(409, keyed = true, code = ApiErrorCode("CONFLICT")))
        assertFalse(repeated(409, keyed = true, code = ApiErrorCode("idempotency_key_in_use")))
        assertFalse(repeated(409, keyed = true, code = ApiErrorCode("IDEMPOTENCY_ANSWER_UNAVAILABLE")))
    }

    @Test
    fun everyOtherStatusIsAStatementAboutTheRequestAndIsNotRepeated() {
        val repeatedStatuses =
            (100..599).filter {
                repeated(it, idempotent = true, keyed = true, code = ApiErrorCode.IdempotencyKeyInUse)
            }

        assertEquals(listOf(409, 429, 502, 503, 504), repeatedStatuses)
        assertFalse(repeated(412, idempotent = true, keyed = true))
    }

    @Test
    fun nothingButAKeyInUseIsRepeatedOnceThePolicysRetriesAreSpent() {
        assertTrue(repeated(429, attempt = 1))
        assertFalse(repeated(429, attempt = 2))
        assertFalse(repeated(503, keyed = true, attempt = 2))
        assertFalse(repeated(429, policy = RetryPolicy(maxRetries = 0)))
    }
}
