package com.mountaya.client

import kotlin.time.Instant

/**
 * SessionSnapshot is what a session source holds at one moment, as one value replaced on every
 * change rather than changed in place.
 */
public class SessionSnapshot(
    /** The session token, or null where the source holds none. */
    public val token: String? = null,
    /** When the token expires, or null where the source does not know. */
    public val expiresAt: Instant? = null,
    /**
     * The user the token was minted for, or null where it was minted for none, which refuses an
     * operation that acts as a user before any request.
     */
    public val userId: String? = null,
    /** Whether a mint is on its way: a request made while the source holds no token waits for it. */
    public val isMinting: Boolean = false,
    /**
     * The last mint's failure, or null where the last one succeeded: an operation that requires a
     * session, refused for want of a token, carries it as its cause.
     */
    public val error: Throwable? = null,
) {
    /**
     * Answers whether [other] holds the same token, expiry and user, and agrees on whether a mint is on its way and
     * whether the last one failed.
     */
    override fun equals(other: Any?): Boolean =
        other is SessionSnapshot && other.token == token && other.expiresAt == expiresAt &&
            other.userId == userId && other.isMinting == isMinting && (other.error == null) == (error == null)

    override fun hashCode(): Int =
        (((token?.hashCode() ?: 0) * 31 + (expiresAt?.hashCode() ?: 0)) * 31 + (userId?.hashCode() ?: 0)) * 4 +
            (if (isMinting) 2 else 0) + (if (error != null) 1 else 0)

    /** Returns the snapshot as a log may show it: the token by its prefix alone, and nothing of the user. */
    override fun toString(): String =
        buildString {
            append("SessionSnapshot(token=").append(token?.let(CredentialHeaders::redacted) ?: "null")
            expiresAt?.let { append(", expiresAt=").append(formatTimestamp(it)) }
            if (isMinting) append(", isMinting=true")
            if (error != null) append(", failed")
            append(")")
        }
}
