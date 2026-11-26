package com.mountaya.client

/**
 * Response is what a REST or Tile operation answers: the envelope whole, its `data` and the
 * `metadata` beside it, and what the answer's status and headers say of it.
 *
 * [Metadata] is the type the operation declares for the envelope's `extensions.metadata`, which
 * is where pagination lives, and the envelope's open map where it declares none, so metadata the
 * contract adds later is still there to read.
 */
public class Response<out Value, out Metadata>(
    /** The operation's payload, the envelope's `data`, `JsonNull` where the contract types it as null. */
    public val data: Value,
    /** The envelope's `extensions.metadata`. */
    public val metadata: Metadata,
    /** The success status the answer arrived with, such as the 201 of a creation or the 202 of work started. */
    public val status: Int = 200,
    /** The answer's `X-Request-Id` exactly as sent, or null where it sent none. */
    public val requestId: String? = null,
    /** The answer's `Location`, which a 201 carries, or null where it sent none. */
    public val location: String? = null,
    /**
     * The answer's `ETag` exactly as sent, or null where it sent none. A resource's own tag is the
     * one its body states, which is what a conditional write sends back.
     */
    public val etag: String? = null,
    headers: Map<String, String> = emptyMap(),
) {
    /**
     * Every header of the answer, each name in lowercase, `A` to `Z` folded alone, and a header that
     * arrived more than once holding its values joined by a comma and a space, as HTTP reads it: the
     * ones the contract declares and any a server or a proxy in front of it adds. [header] reads one
     * by its name in any letter case. A response built by hand, a test's stand-in, folds the names
     * it is handed the same way.
     */
    public val headers: Map<String, String> = HeaderLookup.fold(headers)

    /** Returns the value of the header named [name], read in any letter case, or null where the answer sent none. */
    public fun header(name: String): String? = headers[HeaderLookup.asciiLowercase(name)]

    override fun equals(other: Any?): Boolean =
        other is Response<*, *> && other.data == data && other.metadata == metadata && other.status == status &&
            other.requestId == requestId && other.location == location && other.etag == etag &&
            other.headers == headers

    override fun hashCode(): Int =
        (
            (
                (((data?.hashCode() ?: 0) * 31 + (metadata?.hashCode() ?: 0)) * 31 + status) * 31 +
                    (requestId?.hashCode() ?: 0)
            ) * 31 + (location?.hashCode() ?: 0) * 7 + (etag?.hashCode() ?: 0)
        ) * 31 + headers.hashCode()

    override fun toString(): String =
        buildString {
            append("Response(status=").append(status).append(", data=").append(data)
            append(", metadata=").append(metadata)
            requestId?.let { append(", requestId=").append(it) }
            location?.let { append(", location=").append(it) }
            etag?.let { append(", etag=").append(it) }
            append(")")
        }
}
