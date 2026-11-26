package com.mountaya.client

/**
 * OperationCall is one REST or Tile call, written out by a printed method and handed to the
 * [OperationSender].
 *
 * Everything the operation's signature decided is settled here: the path is substituted and
 * percent-encoded, the query is in the operation's order, and the body is already on the wire's
 * terms. What is left to the sender is the transport's: the origin, the credential, retries and
 * reading the answer.
 *
 * It is `InternalMountayaApi`: the printed admin unit, `mountaya-admin`, calls it from beside the core, and no
 * consumer does (SPEC.md section 7).
 */
@InternalMountayaApi
public class OperationCall(
    /** What the contract declares about the operation. */
    public val facts: OperationFacts,
    /** The path, each parameter substituted and percent-encoded, starting with `/`. */
    public val path: String,
    /** The query, as names and values not yet encoded, in the operation's order. */
    public val query: List<Pair<String, String>>,
    /** The headers the caller set, such as `Last-Event-ID`. A credential's headers are never here. */
    public val headers: List<Pair<String, String>>,
    /** The body. */
    public val body: RequestBody,
    /**
     * The `Idempotency-Key` the caller set, where the operation takes one, or null: the transport
     * then draws one before the first attempt and sends it on every attempt.
     */
    public val idempotencyKey: String? = null,
    /** The `If-Match` the caller set, where the operation takes one, sent exactly as given; null sends none. */
    public val ifMatch: String? = null,
    /** What the call overrides of its client's configuration. */
    public val options: RequestOptions,
) {
    override fun equals(other: Any?): Boolean =
        other is OperationCall && other.facts === facts && other.path == path && other.query == query &&
            other.headers == headers && other.body == body && other.idempotencyKey == idempotencyKey &&
            other.ifMatch == ifMatch && other.options == options

    override fun hashCode(): Int =
        (
            ((path.hashCode() * 31 + query.hashCode()) * 31 + headers.hashCode()) * 31 + body.hashCode()
        ) * 31 + (idempotencyKey?.hashCode() ?: 0) * 7 + (ifMatch?.hashCode() ?: 0)

    override fun toString(): String = "OperationCall(${facts.method} $path, query=$query, headers=$headers, body=$body)"
}
