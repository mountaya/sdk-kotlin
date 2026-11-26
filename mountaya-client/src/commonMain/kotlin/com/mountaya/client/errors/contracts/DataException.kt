package com.mountaya.client

import kotlinx.serialization.json.JsonElement
import kotlin.time.Duration

/**
 * DataException is an error the API answered under a success: a Data API answer that carried
 * GraphQL errors, or that lacks its operation's field. It is an [ApiException], so catching that
 * catches it, and its [status] is the success it came under, a 200.
 */
public class DataException internal constructor(
    status: Int,
    errors: List<ErrorEntry>,
    facts: AnswerFacts,
    cause: Throwable?,
) : ApiException(
        successStatus(status),
        errors,
        facts.metadata,
        facts.retryAfter,
        facts.requestId,
        facts.rateLimit,
        facts.etag,
        facts.idempotencyKey,
        cause,
    ) {
    /** Returns a [DataException] answered under a 200, carrying [errors] and what its answer said, with [cause] riding along. */
    public constructor(
        errors: List<ErrorEntry> = emptyList(),
        metadata: JsonElement? = null,
        retryAfter: Duration? = null,
        requestId: String? = null,
        rateLimit: RateLimitState? = null,
        etag: String? = null,
        idempotencyKey: String? = null,
        cause: Throwable? = null,
    ) : this(200, errors, AnswerFacts(metadata, retryAfter, requestId, rateLimit, etag, idempotencyKey), cause)

    override val name: String get() = "DataError"
}

private fun successStatus(status: Int): Int {
    require(status in 200..299) { "A data failure comes under a success, and $status is not one." }
    return status
}
