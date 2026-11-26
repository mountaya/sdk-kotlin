package com.mountaya.client

/**
 * CredentialDeclaration is what an operation declares about the credentials it takes.
 *
 * Each fact is read from the contract per operation, never inferred from the method: a read may
 * act as a user, and a write may take no credential at all. A fact left out is false, and an
 * acting user left out is [ActingUser.None].
 */
public class CredentialDeclaration(
    /** What the operation needs of the user its session token was minted for, its `x-acting-user`. */
    public val actingUser: ActingUser = ActingUser.None,
    /**
     * Whether a share token on its own authenticates the operation, which is true of the reads of
     * a shared resource and of nothing else.
     */
    public val acceptsShareToken: Boolean = false,
    /** Whether every way to authenticate the operation names a session token. */
    public val requiresSession: Boolean = false,
    /** Whether a secret key authenticates the operation, which is the one that mints a session token. */
    public val requiresSecretKey: Boolean = false,
    /** Whether the operation takes no credential at all, so it is sent none and refuses none. */
    public val takesNoCredential: Boolean = false,
    /**
     * Whether a user whose invitation is still waiting is answered as a member is, the operation's
     * `x-pending-invitee`. It decides nothing the client checks.
     */
    public val pendingInvitee: Boolean = false,
    /**
     * Whether the operation is declared a read whatever its method, as every Data API query is over its
     * `POST`. A share token that asks for the visitor's cookies makes a read on the visitor's session
     * alone; a request sent with `GET`, `HEAD` or `OPTIONS`, in any letter case, is a read whatever this
     * says.
     */
    public val read: Boolean = false,
) {
    /**
     * ActingUser is what an operation needs of the user its session token was minted for.
     *
     * No header names a user: the user an operation acts as is the one its session token names,
     * so this says what the token must name, never what is sent. It is open, so a `when` over it
     * ends in `else`, and its values are the contract's own spellings.
     */
    @kotlin.jvm.JvmInline
    public value class ActingUser internal constructor(
        /** The contract's spelling: `required`, `optional` or `none`. */
        public val value: String,
    ) {
        override fun toString(): String = value

        /** The values the contract names. */
        public companion object {
            /**
             * An operation acting as the user, so its token must name one. A token a session source reports as minted
             * without `user_id` is refused locally, since the API answers it with a 401.
             */
            public val Required: ActingUser = ActingUser("required")

            /** A read a token minted without `user_id` may make, which holds a user the token names to its role. */
            public val Optional: ActingUser = ActingUser("optional")

            /** An operation acting as nobody, a user the token names being ignored. */
            public val None: ActingUser = ActingUser("none")
        }
    }

    override fun equals(other: Any?): Boolean =
        other is CredentialDeclaration &&
            other.actingUser == actingUser &&
            other.acceptsShareToken == acceptsShareToken &&
            other.requiresSession == requiresSession &&
            other.requiresSecretKey == requiresSecretKey &&
            other.takesNoCredential == takesNoCredential &&
            other.pendingInvitee == pendingInvitee &&
            other.read == read

    override fun hashCode(): Int =
        listOf(acceptsShareToken, requiresSession, requiresSecretKey, takesNoCredential, pendingInvitee, read)
            .fold(actingUser.hashCode()) { hash, fact -> hash * 2 + if (fact) 1 else 0 }

    override fun toString(): String =
        "CredentialDeclaration(actingUser=$actingUser, acceptsShareToken=$acceptsShareToken, " +
            "requiresSession=$requiresSession, requiresSecretKey=$requiresSecretKey, " +
            "takesNoCredential=$takesNoCredential, pendingInvitee=$pendingInvitee, read=$read)"
}
