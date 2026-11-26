package com.mountaya.client

import com.mountaya.client.generated.ApiErrorCode

/** ErrorLadder places a status an error came under on the contract's error ladder. */
internal object ErrorLadder {
    /**
     * Returns the failure [status] and [errors] are, carrying [facts].
     *
     * Any success raised as an error is a [DataException]: only the Data API answers a failure under
     * one. A 402 is one billing failure and a 429 one rate-limit failure whatever their code, a
     * ceiling's 429 coded `LIMIT_REACHED` included, and only a 409 reads the classifying code, the
     * first entry's, exactly as the contract spells it. A 1xx, a 3xx, a 304 included, and a 4xx
     * with no row of its own are an `ApiException` itself.
     */
    fun exceptionFor(
        status: Int,
        errors: List<ErrorEntry>,
        facts: AnswerFacts = AnswerFacts(),
        cause: Throwable? = null,
    ): ApiException {
        val metadata = facts.metadata
        val retryAfter = facts.retryAfter
        val requestId = facts.requestId
        val rateLimit = facts.rateLimit
        val etag = facts.etag
        val key = facts.idempotencyKey
        return when (status) {
            in 200..299 -> DataException(status, errors, facts, cause)
            400 -> BadRequestException(errors, metadata, retryAfter, requestId, rateLimit, etag, key, cause)
            401 -> AuthenticationException(errors, metadata, retryAfter, requestId, rateLimit, etag, key, cause)
            402 -> BillingException(errors, metadata, retryAfter, requestId, rateLimit, etag, key, cause)
            403 -> ForbiddenException(errors, metadata, retryAfter, requestId, rateLimit, etag, key, cause)
            404 -> NotFoundException(errors, metadata, retryAfter, requestId, rateLimit, etag, key, cause)
            405 -> MethodNotAllowedException(errors, metadata, retryAfter, requestId, rateLimit, etag, key, cause)
            409 -> conflict(errors, facts, cause)
            412 -> PreconditionFailedException(errors, metadata, retryAfter, requestId, rateLimit, etag, key, cause)
            413 -> PayloadTooLargeException(errors, metadata, retryAfter, requestId, rateLimit, etag, key, cause)
            429 -> RateLimitException(errors, metadata, retryAfter, requestId, rateLimit, etag, key, cause)
            in 500..599 -> ServerException(status, errors, metadata, retryAfter, requestId, rateLimit, etag, key, cause)
            else -> ApiException(status, errors, metadata, retryAfter, requestId, rateLimit, etag, key, cause)
        }
    }

    private fun conflict(
        errors: List<ErrorEntry>,
        facts: AnswerFacts,
        cause: Throwable?,
    ): ConflictException =
        when (errors.firstOrNull()?.code) {
            ApiErrorCode.IdempotencyKeyInUse -> {
                IdempotencyKeyInUseException(
                    errors,
                    facts.metadata,
                    facts.retryAfter,
                    facts.requestId,
                    facts.rateLimit,
                    facts.etag,
                    facts.idempotencyKey,
                    cause,
                )
            }

            ApiErrorCode.IdempotencyAnswerUnavailable -> {
                IdempotentAnswerUnavailableException(
                    errors,
                    facts.metadata,
                    facts.retryAfter,
                    facts.requestId,
                    facts.rateLimit,
                    facts.etag,
                    facts.idempotencyKey,
                    cause,
                )
            }

            else -> {
                ConflictException(errors, facts, cause)
            }
        }
}
