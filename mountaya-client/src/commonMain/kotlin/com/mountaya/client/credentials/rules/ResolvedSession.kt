package com.mountaya.client

import kotlinx.coroutines.flow.first

/**
 * ResolvedSession is a publishable key's session token as one request reads it: the token, what
 * the client is told of its user, and the failure of the mint that left none.
 */
internal class ResolvedSession(
    /** The token the request carries, or null where it carries none. */
    val token: String?,
    /** What the client is told of the user the token was minted for. */
    val user: User,
    /** The failure of the last mint, where a source holds no token for it. */
    val mintError: Throwable? = null,
) {
    /** User is what the client is told of the user a token was minted for. */
    sealed interface User {
        /** Unknown is a token whose user the client is not told: a value or a function's answer. */
        object Unknown : User

        /** Known is the user a minted token or a source names, null being none. */
        class Known(
            val id: String?,
        ) : User
    }

    companion object {
        /**
         * Returns [token] as a request reads it now: a value as it is, a function by calling it, and
         * a source by [snapshot], or by the value of its own where none is given.
         *
         * A token a function answers or a source holds is held to a token's rules at this read, and
         * refused with `ConfigurationException` where it breaks one; a function answering none, or a
         * source holding none, is a request under the key alone.
         */
        fun reading(
            token: SessionToken,
            snapshot: SessionSnapshot?,
        ): ResolvedSession =
            when (token) {
                is SessionToken.Value -> {
                    ResolvedSession(token.token, User.Unknown)
                }

                is SessionToken.Minted -> {
                    ResolvedSession(token.token, User.Known(token.userId))
                }

                is SessionToken.Function -> {
                    val value = token.read()
                    value?.let(CredentialHeaders::checkSessionToken)
                    ResolvedSession(value, User.Unknown)
                }

                is SessionToken.Source -> {
                    val read = snapshot ?: token.source.snapshot.value
                    val value = read.token
                    if (value == null) {
                        ResolvedSession(null, User.Known(null), read.error)
                    } else {
                        CredentialHeaders.checkSessionToken(value)
                        ResolvedSession(value, User.Known(read.userId))
                    }
                }
            }

        /**
         * Returns the snapshot a request goes out with: the source's own where it holds a token or
         * reports none on its way, and otherwise the first one that does, read off its state.
         *
         * The wait is bounded by the caller: a cancelled coroutine, its attempt's budget spent
         * included, stops waiting with its `CancellationException`.
         */
        suspend fun awaitingFirstToken(source: SessionTokenSource): SessionSnapshot {
            val now = source.snapshot.value
            if (now.token != null || !now.isMinting) return now
            return source.snapshot.first { it.token != null || !it.isMinting }
        }
    }
}
