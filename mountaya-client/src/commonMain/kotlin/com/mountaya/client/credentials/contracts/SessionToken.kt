package com.mountaya.client

import com.mountaya.client.generated.RestSchema

/**
 * SessionToken is the session token a publishable key carries, and what the client is told of the
 * user it was minted for.
 *
 * The user an operation acts as is the one its session token was minted for, and no header names
 * one. A token whose user the client is told lets an operation that requires a user be refused
 * locally under a token naming none, where the API would answer it with a 401 that names no rule;
 * a token whose user the client is not told is sent for the API to decide.
 *
 * A token is read for every request, so a refreshed one reaches the next request without
 * rebuilding the client.
 */
public sealed interface SessionToken {
    /**
     * The token a request sent now would carry, which is sent as `X-Session-Token`, or null where
     * there is none: a function's answer, or a source's snapshot, read afresh.
     */
    public val token: String?

    /** Value is a token whose user the client is not told. */
    public class Value(
        override val token: String,
    ) : SessionToken {
        override fun equals(other: Any?): Boolean = other is Value && other.token == token

        override fun hashCode(): Int = token.hashCode()

        /** Returns the token as a log may show it: by its prefix alone. */
        override fun toString(): String = "SessionToken.Value(${CredentialHeaders.redacted(token)})"
    }

    /**
     * Minted is a token as `postSession` answered it, naming the user it was minted for at
     * `user_id`, or no user where that is null.
     */
    public class Minted(
        /** The session `postSession` answered. */
        public val session: RestSchema.Session,
    ) : SessionToken {
        override val token: String get() = session.token

        /** The user the token was minted for, or null where it was minted for none. */
        public val userId: String? get() = session.user_id

        override fun equals(other: Any?): Boolean = other is Minted && other.session == session

        override fun hashCode(): Int = session.hashCode()

        /** Returns the token as a log may show it: by its prefix alone, and nothing else of the session. */
        override fun toString(): String = "SessionToken.Minted(${CredentialHeaders.redacted(token)})"
    }

    /**
     * Function is read for every request and answers the token to send, or null for none, in which
     * case the request goes out under the key alone.
     *
     * It does not suspend, since a map's request hook reads it on a thread of its own and cannot,
     * and a token it answers is held to a token's rules at that read. It does not tell the client
     * whom it names.
     */
    public class Function(
        /** The function that answers the token a request sends now, or null for none. */
        public val read: () -> String?,
    ) : SessionToken {
        override val token: String? get() = read()

        override fun equals(other: Any?): Boolean = other is Function && other.read === read

        override fun hashCode(): Int = read.hashCode()

        /** Returns the token as a log may show it: by where it is read from. */
        override fun toString(): String = "SessionToken.Function"
    }

    /**
     * Source is a source that keeps a token and tells of each change, such as a
     * `SessionTokenEngine`: a request made while it holds no token and reports one on its way
     * waits for it, and the user its snapshot names decides what an operation acting as a user
     * refuses.
     */
    public class Source(
        /** The source the token is read from. */
        public val source: SessionTokenSource,
    ) : SessionToken {
        override val token: String? get() = source.snapshot.value.token

        override fun equals(other: Any?): Boolean = other is Source && other.source === source

        override fun hashCode(): Int = source.hashCode()

        /** Returns the token as a log may show it: by where it is read from. */
        override fun toString(): String = "SessionToken.Source"
    }
}
