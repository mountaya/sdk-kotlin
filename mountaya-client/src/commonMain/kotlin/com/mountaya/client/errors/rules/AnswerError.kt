package com.mountaya.client

import kotlin.time.Duration
import kotlin.time.Instant

/** The header an answer names its request by. */
internal const val REQUEST_ID_HEADER = "X-Request-Id"

/** The header an answer names the wait before the next call in. */
internal const val RETRY_AFTER_HEADER = "Retry-After"

/** The header an answer names a resource's tag in. */
internal const val ETAG_HEADER = "ETag"

/** The header an answer lists the limits it was counted against in. */
internal const val RATE_LIMIT_POLICY_HEADER = "RateLimit-Policy"

/** The header an answer states what is left of each limit in. */
internal const val RATE_LIMIT_HEADER = "RateLimit"

/**
 * AnswerError builds the error an answer raises: the row its status and code fall on, and
 * everything the contract has an error carry off the answer.
 */
internal object AnswerError {
    /**
     * Returns the error an answer of [status] with [headers] and the [envelope] its body carries
     * raises, carrying [idempotencyKey], a `Retry-After` date read against [now], the instant the
     * answer arrived, and what the call's operation [declaration] says of the credentials it takes,
     * which the next step its message names turns on. Each header is read of the answer's headers
     * folded once.
     */
    fun exception(
        status: Int,
        headers: FoldedHeaders,
        envelope: ErrorEnvelope,
        now: Instant,
        idempotencyKey: String?,
        cause: Throwable? = null,
        declaration: NextStep.Declaration = NextStep.Declaration.none,
    ): ApiException {
        val exception =
            ErrorLadder.exceptionFor(
                status,
                envelope.errors,
                AnswerFacts(
                    metadata = envelope.metadata,
                    retryAfter = retryAfter(headers, now),
                    requestId = headers[REQUEST_ID_HEADER],
                    rateLimit = rateLimit(headers),
                    etag = headers[ETAG_HEADER],
                    idempotencyKey = idempotencyKey,
                ),
                cause,
            )
        exception.answeredAt = now
        exception.declaration = declaration
        return exception
    }

    /**
     * Returns the whole seconds an answer's `Retry-After` names, or null where it names none the
     * client reads. An HTTP date is read against [now] and rounded up by the parser the transport
     * waits by too, so a caller who schedules from it never comes back early, and the wait it names
     * is the one the transport took.
     */
    fun retryAfter(
        headers: FoldedHeaders,
        now: Instant,
    ): Duration? = headers[RETRY_AFTER_HEADER]?.let { parseRetryAfter(it, now) }

    /** Returns the state an answer's headers describe, or null where they describe none. */
    fun rateLimit(headers: FoldedHeaders): RateLimitState? =
        RateLimitHeaders.state(headers[RATE_LIMIT_POLICY_HEADER], headers[RATE_LIMIT_HEADER])
}
