package com.mountaya.client

import com.mountaya.client.generated.ApiErrorCode

private const val TOO_MANY_REQUESTS = 429
private const val CONFLICT = 409
private val GATEWAY_FAILURES = setOf(502, 503, 504)

/**
 * Returns whether a failed attempt is followed by another, before the wait its answer names is
 * held to the call's budget.
 *
 * [attempt] counts the attempts already repeated, so the first attempt is 0. [status] is the
 * response's status, or null when no response arrived, and [code] its first entry's code.
 * [idempotent] is what the operation declares: its `x-idempotent` on REST, and true for every Data
 * API operation. [keyed] says the request carried an `Idempotency-Key`.
 *
 * A 409 is decided first and alone: one coded `IDEMPOTENCY_KEY_IN_USE` is repeated where the request
 * carried a key, and is counted against no ceiling of attempts, since it asks whether the first
 * attempt has finished rather than sending the write again; any other 409 is not. A 429 coded
 * `LIMIT_REACHED` is never repeated, whatever the operation declares and whatever the attempts
 * left: it refuses a ceiling, a count of something held that lifts when something is freed or the
 * arrangement changes and never by waiting, so it names no wait. Past the policy's `maxRetries`
 * nothing else is repeated. Any other 429 always is, since the API has said it did nothing. A
 * 502, 503 or 504, and a failure with no status at all, are repeated where the operation declares
 * itself idempotent or the request carried a key: a dropped socket does not say whether a write
 * landed, and a key is what makes asking again safe. Every other status is a statement about the
 * request itself, which a repeat would only hear again, a 412 in particular being its caller's to
 * settle.
 */
internal fun isRetryable(
    attempt: Int,
    status: Int?,
    code: ApiErrorCode?,
    idempotent: Boolean,
    keyed: Boolean,
    policy: RetryPolicy,
): Boolean {
    if (status == CONFLICT) return keyed && code == ApiErrorCode.IdempotencyKeyInUse
    if (status == TOO_MANY_REQUESTS && code == ApiErrorCode.LimitReached) return false
    if (attempt >= policy.maxRetries) return false
    return when (status) {
        TOO_MANY_REQUESTS -> true
        null, in GATEWAY_FAILURES -> idempotent || keyed
        else -> false
    }
}
