package com.mountaya.client

/**
 * Credential is what the client authenticates its requests with, one implementation per shape
 * the contract allows.
 *
 * A session token accompanies a publishable key, so it rides on [Publishable], and a share token
 * replaces a key, so [Share] has nothing beside it: a combination the server would misread cannot
 * be built. Each member is checked when it is built: a missing one, one holding a character no
 * header carries, and one whose prefix is not its own are each a [ConfigurationException] before
 * any request carrying it leaves the process.
 */
public sealed interface Credential {
    /**
     * Returns the headers this credential and the client's [language] send to an operation that
     * declares what [declaration] says, called by [method], the HTTP method, on a request
     * and on an event stream's open alike, or throws [ConfigurationException] where the operation does
     * not take them.
     *
     * The refusals are the server's own answers, given before the request leaves: a secret key
     * anywhere but the operation it authenticates, a share token where the operation takes none, a
     * missing session token where the operation requires one, and a token minted without `user_id`
     * where the operation acts as one. An operation that takes no credential is sent none and
     * refuses none, and is sent the language all the same, `*` where [language] is null, which asks
     * for no language in particular. A language that is empty or whitespace alone, or that holds a
     * character no header carries, is refused whatever the operation.
     *
     * The method matters to a share token that asks for the visitor's cookies alone: a read of an
     * operation that declares no share token is sent on the visitor's session with no `X-Share-Token`,
     * one that requires a session token among them, since only the server can tell whether the
     * visitor's cookies hold one, and every other request is a write, refused as every write under a
     * share token is. A read is a request sent with `GET`, `HEAD` or `OPTIONS`, in any letter case, or
     * one of an operation [declaration] declares a read, as every Data API query is over its `POST`.
     */
    public fun headers(
        declaration: CredentialDeclaration,
        language: String? = null,
        method: String = "GET",
    ): Map<String, String> = CredentialHeaders.headers(this, language, declaration, method)

    /**
     * Publishable is a publishable key (`pk_`), which is safe to ship in an app, and the session
     * token (`sess_`) that accompanies it, where it has one.
     *
     * The user an operation acts as is the one the session token was minted for: no header names
     * one. It throws [ConfigurationException] where a member is missing, holds a character no header
     * carries, or has the wrong prefix.
     */
    public class Publishable(
        /** The publishable key. */
        public val key: String,
        /** The session token that accompanies the key, or null where it has none. */
        public val sessionToken: SessionToken? = null,
    ) : Credential {
        init {
            CredentialHeaders.check(this)
        }

        override fun equals(other: Any?): Boolean =
            other is Publishable && other.key == key && other.sessionToken == sessionToken

        override fun hashCode(): Int = key.hashCode() * 31 + (sessionToken?.hashCode() ?: 0)

        /** Returns the credential as a log may show it: each key and token by its prefix alone. */
        override fun toString(): String =
            buildString {
                append("Credential.Publishable(key=").append(CredentialHeaders.redacted(key))
                sessionToken?.let { append(", sessionToken=").append(CredentialHeaders.redacted(it)) }
                append(")")
            }
    }

    /**
     * Secret is a secret key (`sk_`), which mints session tokens and belongs on a server.
     *
     * It is built on the jvm alone, by `Credential.Secret(key)` or the `Credential` factory that takes a secret
     * key, so an Android or iOS app has nothing to call to ship one by accident. It throws
     * [ConfigurationException] where the key is missing, holds a character no header carries, or has the wrong
     * prefix.
     */
    public class Secret internal constructor(
        /** The secret key. */
        public val key: String,
    ) : Credential {
        init {
            CredentialHeaders.check(this)
        }

        override fun equals(other: Any?): Boolean = other is Secret && other.key == key

        override fun hashCode(): Int = key.hashCode()

        /** Returns the credential as a log may show it: the key by its prefix alone. */
        override fun toString(): String = "Credential.Secret(key=${CredentialHeaders.redacted(key)})"

        /**
         * The receiver `Credential.Secret(key)` is called on, which the jvm declares and Android and iOS do not, so the
         * constructor's own call reads the same on a server and has no symbol in an app.
         */
        public companion object
    }

    /**
     * Share is a share-link visitor's token (`share_`), which reads a shared resource and writes
     * nothing. It throws [ConfigurationException] where the token is missing, holds a character no
     * header carries, or has the wrong prefix.
     *
     * A share link that rides the visitor's own session asks for the visitor's cookies, which
     * [cookies] answers. Only a request under this token, to an operation that takes a credential,
     * carries them, and a read that declares no share token is sent the cookies alone and not the
     * token. No cookie comes from anywhere ambient: where they live is the app's to know.
     */
    public class Share(
        /** The share token. */
        public val token: String,
        /** The source of the visitor's cookies, or null where the link asks for none. */
        public val cookies: CookieSource? = null,
    ) : Credential {
        init {
            CredentialHeaders.check(this)
        }

        override fun equals(other: Any?): Boolean = other is Share && other.token == token && other.cookies === cookies

        override fun hashCode(): Int = token.hashCode() * 31 + (cookies?.hashCode() ?: 0)

        /** Returns the credential as a log may show it: the token by its prefix alone, and whether it reads cookies. */
        override fun toString(): String =
            "Credential.Share(token=${CredentialHeaders.redacted(token)}" + (if (cookies != null) ", cookies" else "") +
                ")"
    }
}

/**
 * Credential builds the credential separate parts make, such as a key read from configuration and a
 * share token read from a link.
 *
 * A share token replaces the key, so where one is given it is the whole credential and the other
 * parts are not sent: an empty one beside a key is refused as a missing share token, never read as
 * a fall back to the key. Otherwise the publishable key carries the session token beside it. It
 * throws [ConfigurationException] where the parts make no credential, or where a part is missing,
 * has the wrong prefix or holds a character no header carries.
 */
public fun Credential(
    publishableKey: String? = null,
    sessionToken: SessionToken? = null,
    shareToken: String? = null,
): Credential =
    when {
        shareToken != null -> {
            Credential.Share(shareToken)
        }

        publishableKey != null -> {
            Credential.Publishable(publishableKey, sessionToken)
        }

        sessionToken != null -> {
            throw ConfigurationException(
                "A session token accompanies a publishable key, and is refused alone: pass the publishable key beside it.",
            )
        }

        else -> {
            throw ConfigurationException("No credential: pass a publishable key or a share token.")
        }
    }

/** The secret key [key] built by its class's own constructor, which the jvm's `Credential.Secret(key)` calls. */
internal fun secretCredential(key: String): Credential.Secret = Credential.Secret(key)
