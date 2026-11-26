package com.mountaya.client

/**
 * PersistedQueryAttempt is what one attempt of a Data API operation sends of its document: the
 * digest alone, the document alone, or both.
 */
internal class PersistedQueryAttempt(
    /** The persisted-query digest the attempt sends, or null where it sends none. */
    val digest: String?,
    /** Whether the attempt sends the document itself. */
    val sendsDocument: Boolean,
) {
    /**
     * Whether the attempt sends a digest and leaves the document out, which is the one attempt a
     * server answers by asking for the document.
     */
    val isDigestOnly: Boolean get() = digest != null && !sendsDocument

    override fun equals(other: Any?): Boolean =
        other is PersistedQueryAttempt && other.digest == digest && other.sendsDocument == sendsDocument

    override fun hashCode(): Int = (digest?.hashCode() ?: 0) * 31 + if (sendsDocument) 1 else 0

    override fun toString(): String = "PersistedQueryAttempt(digest=$digest, sendsDocument=$sendsDocument)"
}
