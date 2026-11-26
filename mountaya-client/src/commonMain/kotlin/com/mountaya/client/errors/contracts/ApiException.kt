package com.mountaya.client

import com.mountaya.client.generated.ApiErrorCode
import kotlinx.serialization.json.JsonElement
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * ApiException is an error the API answered: a response that arrived with a failing status, or a
 * Data API answer that carried errors under a success, the envelope it carried and what its
 * headers said.
 *
 * Each 4xx a caller acts on has a subclass of its own, and the whole 5xx range shares
 * [ServerException], since a caller acts identically on every 5xx. A 402 is one [BillingException]
 * and a 429 one [RateLimitException] whatever their entries carry. The two 409s that answer an
 * `Idempotency-Key` are subclasses of
 * [ConflictException], so a caller catching it catches both. A status with no subclass of its own
 * is an `ApiException` itself, which [of] builds; the subclasses are built by their own
 * constructors.
 */
public open class ApiException internal constructor(
    /** The HTTP status the response arrived with, a Data API's success status included. */
    public val status: Int,
    /**
     * Every entry of the failure envelope, in the order the API found them. It is empty where the
     * response carried no envelope it could be read from.
     */
    public val errors: List<ErrorEntry>,
    /**
     * The answer's `extensions.metadata` exactly as sent, or null where it carries none. It tells
     * apart two answers that share a status and a code: a 409 refused by a lock names the member
     * holding it at `holder.id`, and one refused because another write landed first names nothing.
     */
    public val metadata: JsonElement?,
    /**
     * The whole seconds the answer's `Retry-After` named, whatever its status, an HTTP date read
     * as the seconds to it rounded up, or null where it named none: what a caller schedules its
     * next call from where the client did not wait.
     */
    public val retryAfter: Duration?,
    /** The answer's `X-Request-Id` exactly as sent, the one thing a caller quotes to support, or null where it sent none. */
    public val requestId: String?,
    /** The state the answer's `RateLimit-Policy` and `RateLimit` describe, or null where they describe none. */
    public val rateLimit: RateLimitState?,
    /**
     * The answer's `ETag` exactly as sent, or null where it sent none. On a 412 it is the
     * resource's tag now, and not one its caller has read.
     */
    public val etag: String?,
    idempotencyKey: String?,
    cause: Throwable?,
) : MountayaException(describe(status, errors), cause, idempotencyKey) {
    /**
     * The classifying code, which is the first entry's, reported beside the class rather than choosing it, or null where
     * the envelope carries no entry or its first entry no code.
     */
    public val code: ApiErrorCode? get() = errors.firstOrNull()?.code

    /**
     * Returns whether any entry of the envelope carries [code], the first or another, since a refusal may name several
     * codes at once (`transport.errors.envelope`): a 402 whose second entry is coded `FORBIDDEN` answers it for
     * [ApiErrorCode.Forbidden] as well as for [ApiErrorCode.PaymentRequired].
     */
    public fun hasCode(code: ApiErrorCode): Boolean = errors.any { it.code == code }

    override val name: String get() = "ApiError"

    /**
     * The instant the answer arrived, on the client's wall clock, which the wait it names is counted from,
     * or null for an error a caller built.
     */
    internal var answeredAt: Instant? = null

    /**
     * What the failed call's operation declares about the credentials it takes, as far as the next step
     * its message names turns on it, or none for an error a caller built.
     */
    internal var declaration: NextStep.Declaration = NextStep.Declaration.none

    /**
     * The failure as a developer reads it in a log, such as `429 TOO_MANY_REQUESTS: Too many requests.
     * Wait 23 seconds before calling again.`: the status, the code and the first entry's message, then
     * the next step a caller can take where the status names one, sharpened by what the call's
     * operation declares.
     */
    override val message: String
        get() {
            val text = describe(status, errors)
            val step = NextStep.step(status, code, retryAfter, rateLimit, declaration, answeredAt) ?: return text
            return text + (if (text.last() in ".!?") " " else ". ") + step
        }

    /** The factory of the failure a status and an envelope are. */
    public companion object {
        /**
         * Returns the failure an answer of [status] carrying [errors] is: the subclass of its row
         * of the contract's error ladder, or an `ApiException` itself where the status has none.
         * The rest is what the answer carried, each null where it carried none, and [cause] is an
         * underlying failure that rides along for diagnostics.
         */
        public fun of(
            status: Int,
            errors: List<ErrorEntry> = emptyList(),
            metadata: JsonElement? = null,
            retryAfter: Duration? = null,
            requestId: String? = null,
            rateLimit: RateLimitState? = null,
            etag: String? = null,
            idempotencyKey: String? = null,
            cause: Throwable? = null,
        ): ApiException =
            ErrorLadder.exceptionFor(
                status,
                errors,
                AnswerFacts(metadata, retryAfter, requestId, rateLimit, etag, idempotencyKey),
                cause,
            )
    }
}

/**
 * AnswerFacts is what an answer carries beside its status and its entries, as the ladder hands it
 * to the class it picks.
 */
internal class AnswerFacts(
    val metadata: JsonElement? = null,
    val retryAfter: Duration? = null,
    val requestId: String? = null,
    val rateLimit: RateLimitState? = null,
    val etag: String? = null,
    val idempotencyKey: String? = null,
)

/** The failure as the API's own words say it: the status, the code, then the first message. */
private fun describe(
    status: Int,
    errors: List<ErrorEntry>,
): String {
    val code = errors.firstOrNull()?.code
    val message = errors.firstOrNull()?.message
    return buildString {
        append(status)
        if (code != null) append(" ").append(code.value)
        if (!message.isNullOrEmpty()) append(": ").append(message)
    }
}

/** BadRequestException is a 400: the request itself was rejected. */
public class BadRequestException(
    errors: List<ErrorEntry> = emptyList(),
    metadata: JsonElement? = null,
    retryAfter: Duration? = null,
    requestId: String? = null,
    rateLimit: RateLimitState? = null,
    etag: String? = null,
    idempotencyKey: String? = null,
    cause: Throwable? = null,
) : ApiException(400, errors, metadata, retryAfter, requestId, rateLimit, etag, idempotencyKey, cause) {
    override val name: String get() = "BadRequestError"
}

/** AuthenticationException is a 401: no valid credential, or none where one is required. */
public class AuthenticationException(
    errors: List<ErrorEntry> = emptyList(),
    metadata: JsonElement? = null,
    retryAfter: Duration? = null,
    requestId: String? = null,
    rateLimit: RateLimitState? = null,
    etag: String? = null,
    idempotencyKey: String? = null,
    cause: Throwable? = null,
) : ApiException(401, errors, metadata, retryAfter, requestId, rateLimit, etag, idempotencyKey, cause) {
    override val name: String get() = "AuthenticationError"
}

/**
 * BillingException is a 402, whatever its entries carry: not entitled to what the call needs,
 * billed by card with no payment method on file, or a subscription opened while invoices are still
 * owed. Each is resolved by changing the arrangement or its billing details, never in code. The
 * Data API's bare `PAYMENT_REQUIRED` is one. A ceiling is never one: it is a [RateLimitException].
 */
public class BillingException(
    errors: List<ErrorEntry> = emptyList(),
    metadata: JsonElement? = null,
    retryAfter: Duration? = null,
    requestId: String? = null,
    rateLimit: RateLimitState? = null,
    etag: String? = null,
    idempotencyKey: String? = null,
    cause: Throwable? = null,
) : ApiException(402, errors, metadata, retryAfter, requestId, rateLimit, etag, idempotencyKey, cause) {
    override val name: String get() = "BillingError"
}

/** ForbiddenException is a 403: the credential is valid and does not carry what the call needs. */
public class ForbiddenException(
    errors: List<ErrorEntry> = emptyList(),
    metadata: JsonElement? = null,
    retryAfter: Duration? = null,
    requestId: String? = null,
    rateLimit: RateLimitState? = null,
    etag: String? = null,
    idempotencyKey: String? = null,
    cause: Throwable? = null,
) : ApiException(403, errors, metadata, retryAfter, requestId, rateLimit, etag, idempotencyKey, cause) {
    override val name: String get() = "ForbiddenError"
}

/** NotFoundException is a 404: no such resource, or none this credential can view. */
public class NotFoundException(
    errors: List<ErrorEntry> = emptyList(),
    metadata: JsonElement? = null,
    retryAfter: Duration? = null,
    requestId: String? = null,
    rateLimit: RateLimitState? = null,
    etag: String? = null,
    idempotencyKey: String? = null,
    cause: Throwable? = null,
) : ApiException(404, errors, metadata, retryAfter, requestId, rateLimit, etag, idempotencyKey, cause) {
    override val name: String get() = "NotFoundError"
}

/** MethodNotAllowedException is a 405: no such method on that path. */
public class MethodNotAllowedException(
    errors: List<ErrorEntry> = emptyList(),
    metadata: JsonElement? = null,
    retryAfter: Duration? = null,
    requestId: String? = null,
    rateLimit: RateLimitState? = null,
    etag: String? = null,
    idempotencyKey: String? = null,
    cause: Throwable? = null,
) : ApiException(405, errors, metadata, retryAfter, requestId, rateLimit, etag, idempotencyKey, cause) {
    override val name: String get() = "MethodNotAllowedError"
}

/**
 * ConflictException is a 409: a concurrent edit, a duplicate, or a state that forbids the act.
 *
 * It is the one row with rows beneath it, the two 409s that answer an `Idempotency-Key`, so it is
 * open, and its constructor is internal, like [ApiException]'s, so no class outside this module
 * joins the hierarchy. A caller builds one by its name all the same, through its companion.
 */
public open class ConflictException internal constructor(
    errors: List<ErrorEntry>,
    facts: AnswerFacts,
    cause: Throwable?,
) : ApiException(
        409,
        errors,
        facts.metadata,
        facts.retryAfter,
        facts.requestId,
        facts.rateLimit,
        facts.etag,
        facts.idempotencyKey,
        cause,
    ) {
    override val name: String get() = "ConflictError"

    /** The factory of a [ConflictException]. */
    public companion object {
        /** Returns a [ConflictException] carrying [errors] and what its answer said, with [cause] riding along. */
        public operator fun invoke(
            errors: List<ErrorEntry> = emptyList(),
            metadata: JsonElement? = null,
            retryAfter: Duration? = null,
            requestId: String? = null,
            rateLimit: RateLimitState? = null,
            etag: String? = null,
            idempotencyKey: String? = null,
            cause: Throwable? = null,
        ): ConflictException =
            ConflictException(
                errors,
                AnswerFacts(metadata, retryAfter, requestId, rateLimit, etag, idempotencyKey),
                cause,
            )
    }
}

/**
 * IdempotencyKeyInUseException is a 409 coded `IDEMPOTENCY_KEY_IN_USE`: a request carrying the same
 * `Idempotency-Key` is still running, so the write may still be made, and the call that meets it
 * waits and repeats. It is raised once the call stops asking, carrying the key.
 */
public class IdempotencyKeyInUseException(
    errors: List<ErrorEntry> = emptyList(),
    metadata: JsonElement? = null,
    retryAfter: Duration? = null,
    requestId: String? = null,
    rateLimit: RateLimitState? = null,
    etag: String? = null,
    idempotencyKey: String? = null,
    cause: Throwable? = null,
) : ConflictException(errors, AnswerFacts(metadata, retryAfter, requestId, rateLimit, etag, idempotencyKey), cause) {
    override val name: String get() = "IdempotencyKeyInUseError"
}

/**
 * IdempotentAnswerUnavailableException is a 409 coded `IDEMPOTENCY_ANSWER_UNAVAILABLE`: the request
 * that first carried the `Idempotency-Key` landed, and its answer can no longer be given. It is
 * never repeated, and [answer] is what the API names of the answer the write was first given.
 */
public class IdempotentAnswerUnavailableException(
    errors: List<ErrorEntry> = emptyList(),
    metadata: JsonElement? = null,
    retryAfter: Duration? = null,
    requestId: String? = null,
    rateLimit: RateLimitState? = null,
    etag: String? = null,
    idempotencyKey: String? = null,
    cause: Throwable? = null,
) : ConflictException(errors, AnswerFacts(metadata, retryAfter, requestId, rateLimit, etag, idempotencyKey), cause) {
    override val name: String get() = "IdempotentAnswerUnavailableError"

    /**
     * The answer the write was first given, as `extensions.metadata` states it: its status where
     * the metadata states a whole number there, and its `Location` and `ETag` where it states a
     * string, each null otherwise. The write was made, and this is what its caller reads the
     * resource again from.
     */
    public val answer: IdempotentAnswer get() = IdempotentAnswer.of(metadata)
}

/**
 * PreconditionFailedException is a 412: the `If-Match` a conditional write sent names a version the
 * resource has moved past, and [etag] is the resource's tag now. It is never repeated: the caller
 * reads the resource again and decides what the write should be.
 */
public class PreconditionFailedException(
    errors: List<ErrorEntry> = emptyList(),
    metadata: JsonElement? = null,
    retryAfter: Duration? = null,
    requestId: String? = null,
    rateLimit: RateLimitState? = null,
    etag: String? = null,
    idempotencyKey: String? = null,
    cause: Throwable? = null,
) : ApiException(412, errors, metadata, retryAfter, requestId, rateLimit, etag, idempotencyKey, cause) {
    override val name: String get() = "PreconditionFailedError"
}

/** PayloadTooLargeException is a 413: a body over the operation's ceiling. */
public class PayloadTooLargeException(
    errors: List<ErrorEntry> = emptyList(),
    metadata: JsonElement? = null,
    retryAfter: Duration? = null,
    requestId: String? = null,
    rateLimit: RateLimitState? = null,
    etag: String? = null,
    idempotencyKey: String? = null,
    cause: Throwable? = null,
) : ApiException(413, errors, metadata, retryAfter, requestId, rateLimit, etag, idempotencyKey, cause) {
    override val name: String get() = "PayloadTooLargeError"
}

/**
 * RateLimitException is a 429: a limit counted over a window, per minute, per day or per month, is
 * spent until the window resets, and [retryAfter] is the wait the answer names; or a ceiling is
 * reached, the seats, the published itineraries or the published zones an arrangement allows, the
 * invitations an organization may have waiting at once or the itineraries a personal space keeps.
 *
 * A ceiling's [code] reads `LIMIT_REACHED`, and its [metadata] names the ceiling with what it
 * allows and what is held. No wait lifts a ceiling, only something freed or the arrangement
 * changed, so the API names none, [retryAfter] is null, and the client never repeats it. A caller
 * tells a ceiling from a spent window by its code, never by the wait it lacks.
 */
public class RateLimitException(
    errors: List<ErrorEntry> = emptyList(),
    metadata: JsonElement? = null,
    retryAfter: Duration? = null,
    requestId: String? = null,
    rateLimit: RateLimitState? = null,
    etag: String? = null,
    idempotencyKey: String? = null,
    cause: Throwable? = null,
) : ApiException(429, errors, metadata, retryAfter, requestId, rateLimit, etag, idempotencyKey, cause) {
    override val name: String get() = "RateLimitError"
}

/**
 * ServerException is any 5xx: the request arrived and the API could not answer it. [status] is
 * the one it answered with, and a status outside 500 to 599 is refused.
 */
public class ServerException(
    status: Int,
    errors: List<ErrorEntry> = emptyList(),
    metadata: JsonElement? = null,
    retryAfter: Duration? = null,
    requestId: String? = null,
    rateLimit: RateLimitState? = null,
    etag: String? = null,
    idempotencyKey: String? = null,
    cause: Throwable? = null,
) : ApiException(
        serverStatus(status),
        errors,
        metadata,
        retryAfter,
        requestId,
        rateLimit,
        etag,
        idempotencyKey,
        cause,
    ) {
    override val name: String get() = "ServerError"
}

private fun serverStatus(status: Int): Int {
    require(status in 500..599) { "A server failure is a 5xx, and $status is not one." }
    return status
}
