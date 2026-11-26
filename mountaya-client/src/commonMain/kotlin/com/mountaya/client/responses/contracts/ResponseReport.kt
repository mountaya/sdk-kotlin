package com.mountaya.client

/**
 * ResponseReport is what a client's `onResponse` hook is told of one attempt an answer came back
 * to.
 *
 * An attempt that never arrived has no answer to report. The hook is told of every other one, a
 * failure, an attempt that is repeated and every open of an event stream included, and nothing it
 * does changes what the call answers.
 */
public class ResponseReport(
    /** The contract's name for the operation the attempt called. */
    public val operationId: String,
    /** The surface the operation belongs to. */
    public val surface: Surface,
    /** The status the answer arrived with. */
    public val status: Int,
    /** Which attempt of the call the answer came back to, 0 for the first, a dropped one counted. */
    public val attempt: Int,
    /**
     * Whether the answer is one the API kept for a key it had seen and gave again, which
     * `Idempotent-Replayed: true` says.
     */
    public val isReplayed: Boolean = false,
    /** The answer's `X-Request-Id` exactly as sent, or null where it sent none. */
    public val requestId: String? = null,
    /** The state the answer's rate-limit headers describe, or null where they describe none. */
    public val rateLimit: RateLimitState? = null,
) {
    override fun equals(other: Any?): Boolean =
        other is ResponseReport && other.operationId == operationId && other.surface == surface &&
            other.status == status && other.attempt == attempt && other.isReplayed == isReplayed &&
            other.requestId == requestId && other.rateLimit == rateLimit

    override fun hashCode(): Int =
        listOf(surface, status, attempt, isReplayed, requestId, rateLimit)
            .fold(operationId.hashCode()) { hash, part -> hash * 31 + (part?.hashCode() ?: 0) }

    override fun toString(): String =
        "ResponseReport($surface $operationId, status=$status, attempt=$attempt, isReplayed=$isReplayed, " +
            "requestId=$requestId, rateLimit=$rateLimit)"
}
