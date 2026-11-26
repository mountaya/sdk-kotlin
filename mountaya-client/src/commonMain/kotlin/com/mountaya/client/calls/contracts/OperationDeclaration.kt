package com.mountaya.client

import kotlin.time.Duration

/**
 * OperationDeclaration is what an operation declares that the client reads at request time, for a
 * call to one this build prints no method for.
 *
 * Every printed method carries its operation's declaration already; this one is written by the
 * caller, from the contract, and nothing in it is inferred from the method. A write that declares
 * no `Idempotency-Key` sends none, a read may act as a user, and only [idempotent] says whether a
 * repeat after a dropped connection is safe. Each fact left out is the one no operation states by
 * default: on the REST surface, not idempotent, no key, no condition, no budget of its own.
 */
public class OperationDeclaration(
    /** The contract's name for the operation, which the `onResponse` hook is told. */
    public val operationId: String,
    /** The HTTP method, uppercase. */
    public val method: String,
    /**
     * The surface the operation belongs to, whose origin the request goes to. Only the REST and
     * Tile surfaces answer an envelope, so a declaration names [Surface.Rest] or [Surface.Tiles].
     */
    public val surface: Surface = Surface.Rest,
    /** The operation's `x-idempotent`: a repeat has no second effect. */
    public val idempotent: Boolean = false,
    /** What the operation takes, and so what it refuses locally. */
    public val credentials: CredentialDeclaration = CredentialDeclaration(),
    /**
     * The operation's `x-timeout-ms`, the most the API spends on it before it answers a 504
     * itself, or null where it declares none, as an event stream does.
     */
    public val timeout: Duration? = null,
    /** Whether the operation takes an `Idempotency-Key`, which every call to it then sends. */
    public val declaresIdempotencyKey: Boolean = false,
    /** Whether the operation takes an `If-Match`, which a call sends only where its caller names a tag. */
    public val isConditional: Boolean = false,
) {
    override fun equals(other: Any?): Boolean =
        other is OperationDeclaration &&
            other.operationId == operationId &&
            other.method == method &&
            other.surface == surface &&
            other.idempotent == idempotent &&
            other.credentials == credentials &&
            other.timeout == timeout &&
            other.declaresIdempotencyKey == declaresIdempotencyKey &&
            other.isConditional == isConditional

    override fun hashCode(): Int =
        listOf(surface, idempotent, credentials, timeout, declaresIdempotencyKey, isConditional)
            .fold(operationId.hashCode() * 31 + method.hashCode()) { hash, fact -> hash * 31 + fact.hashCode() }

    override fun toString(): String = "OperationDeclaration($surface $operationId: $method)"
}
