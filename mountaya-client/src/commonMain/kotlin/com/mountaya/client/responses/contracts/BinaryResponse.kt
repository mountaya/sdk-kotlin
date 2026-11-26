package com.mountaya.client

/**
 * BinaryResponse is what an operation answering with a file returns: its bytes, the content type
 * the server chose for them, and what the answer said about itself.
 *
 * The export answers in one of several formats, and the content type is how a caller tells which
 * one arrived without guessing from the bytes. Its success declares an `X-Request-Id`, which is
 * what a caller quotes to support about the call.
 */
public class BinaryResponse(
    /** The file, exactly as the server sent it. */
    public val bytes: ByteArray,
    /** The response's `Content-Type`, such as `application/gpx+xml`. */
    public val contentType: String,
    /** The HTTP status the file arrived with. */
    public val status: Int = 200,
    /** The answer's `X-Request-Id`, exactly as sent, or null where it sent none. */
    public val requestId: String? = null,
) {
    override fun equals(other: Any?): Boolean =
        other is BinaryResponse && other.contentType == contentType && other.status == status &&
            other.requestId == requestId && other.bytes.contentEquals(bytes)

    override fun hashCode(): Int =
        ((bytes.contentHashCode() * 31 + contentType.hashCode()) * 31 + status) * 31 + (requestId?.hashCode() ?: 0)

    override fun toString(): String =
        "BinaryResponse(${bytes.size} bytes, contentType=$contentType, status=$status, requestId=$requestId)"
}
