package com.mountaya.client

/** The header a key, publishable or secret, is sent in. */
internal const val API_KEY_HEADER = "X-API-Key"

/** The header a session token is sent in, beside a publishable key. */
internal const val SESSION_TOKEN_HEADER = "X-Session-Token"

/** The header a share token is sent in, alone. */
internal const val SHARE_TOKEN_HEADER = "X-Share-Token"

/** The header the client's language is sent in, on every request. */
internal const val ACCEPT_LANGUAGE_HEADER = "Accept-Language"

/**
 * The language a client given none sends: RFC 9110 reads `*` as no language in particular, so the
 * API answers in its own default rather than in whatever a platform's loader would write, the
 * device's languages where URLSession writes them.
 */
internal const val ANY_LANGUAGE = "*"

/** The header a share-link visitor's cookies are sent in. */
internal const val COOKIE_HEADER = "Cookie"

/**
 * CredentialHeaders projects a credential and the client's language onto the headers a request
 * carries, and refuses what an operation does not take before the request leaves the process.
 *
 * The language rides every projection, [ANY_LANGUAGE] where the client names none, so every request
 * and every map request names one and no platform's loader writes its own. An event stream's open
 * is a request to its operation, so it carries the same projection, the language among it, beside
 * the `Accept` that asks for the stream.
 */
internal object CredentialHeaders {
    /** The prefix each kind of credential starts with, and what a reader calls it. */
    private val prefixes =
        listOf(
            "pk_" to "a publishable key",
            "sk_" to "a secret key",
            "sess_" to "a session token",
            "share_" to "a share token",
        )

    /**
     * Projection is what a credential sends to one request: its headers and the language,
     * [ANY_LANGUAGE] where the client names none, and the source of the visitor's cookies where the
     * request asks for them.
     */
    class Projection(
        val headers: Map<String, String>,
        val cookies: CookieSource? = null,
    )

    /**
     * Refuses a visitor's [cookie] that no header carries, a control character or a character outside
     * printable ASCII in it, as it is read for a request the transport does not send itself: a map's. Its
     * value is the visitor's session, so the refusal never writes it out.
     */
    fun checkCookie(cookie: String) {
        if (HeaderValue.isCarried(cookie)) return
        throw ConfigurationException(
            "The visitor's `Cookie` holds a control character or a character outside printable ASCII, which no " +
                "header carries: answer the header the visitor's session writes, or none.",
        )
    }

    /**
     * Refuses a credential with a member that is missing, that holds a character no header carries,
     * or whose prefix is not its own, in that order and member by member: the key or the token
     * first, then the session token beside a key, where it is held as a value. A token read per
     * request is held to the same rules at that read.
     *
     * The prefix and never the length: a length check is a second copy of the server's format, and
     * the day the format widens it refuses a key the server accepts. A refusal never quotes the
     * value, which may be a secret.
     */
    fun check(credential: Credential) {
        when (credential) {
            is Credential.Publishable -> {
                expect(credential.key, "pk_", "The publishable key")
                when (val session = credential.sessionToken) {
                    is SessionToken.Value -> checkSessionToken(session.token)
                    is SessionToken.Minted -> checkSessionToken(session.token)
                    is SessionToken.Function, is SessionToken.Source, null -> Unit
                }
            }

            is Credential.Share -> {
                expect(credential.token, "share_", "The share token")
            }

            is Credential.Secret -> {
                expect(credential.key, "sk_", "The secret key")
            }
        }
    }

    /** Refuses a session token that is missing, that no header carries, or whose prefix is not `sess_`. */
    fun checkSessionToken(token: String) {
        expect(token, "sess_", "The session token")
    }

    /**
     * Refuses a language that is empty or whitespace alone, or that holds a character no header
     * carries. A language left out is none, and sends [ANY_LANGUAGE].
     */
    fun checkLanguage(language: String?) {
        if (language == null) return
        if (HeaderValue.isBlank(language)) {
            throw ConfigurationException(
                "The language is empty: pass a language tag such as `fr`, or leave the language out.",
            )
        }
        if (!HeaderValue.isCarried(language)) {
            throw ConfigurationException(
                "The language holds a control character or a character outside printable ASCII, which the " +
                    "platform's HTTP stack does not send: pass a language tag such as `fr`.",
            )
        }
    }

    /**
     * Returns [value] as a log may show it: the credential prefix it starts with, or nothing where
     * it starts with none, then an ellipsis. A key or a token is enough to act as its owner, so no
     * more of one is printed, and the prefix is what tells a reader which part sits where.
     */
    fun redacted(value: String): String = (prefixes.firstOrNull { value.startsWith(it.first) }?.first ?: "") + "…"

    /** Returns [token] as a log may show it: one held as a value by its prefix, one read per request by where it is read from. */
    fun redacted(token: SessionToken): String =
        when (token) {
            is SessionToken.Value -> redacted(token.token)
            is SessionToken.Minted -> redacted(token.token)
            is SessionToken.Function -> "function"
            is SessionToken.Source -> "source"
        }

    /**
     * Returns what [credential] and [language] send to an operation declaring [declaration], called by
     * [method], or refuses them with [ConfigurationException], reading a session token the credential
     * reads per request as it is now.
     *
     * The credential and the language are checked first, whatever the operation declares, so a
     * malformed one is refused even where the operation takes no credential. Whether the request is a
     * read is [isRead]'s to say.
     */
    fun headers(
        credential: Credential,
        language: String?,
        declaration: CredentialDeclaration,
        method: String,
    ): Map<String, String> {
        check(credential)
        checkLanguage(language)
        val session = (credential as? Credential.Publishable)?.sessionToken?.let { ResolvedSession.reading(it, null) }
        return project(credential, session, language, declaration, isRead = isRead(method, declaration)).headers
    }

    /** The methods a request reads with, in ASCII lowercase. */
    private val readMethods = setOf("get", "head", "options")

    /**
     * Whether a request sent with [method] to an operation declaring [declaration] is a read, which a
     * share token that asks for cookies may make alone: the operation declares one, as every Data API
     * query is over its `POST`, or [method] is `GET`, `HEAD` or `OPTIONS` in any letter case. Every other
     * request is a write.
     */
    fun isRead(
        method: String,
        declaration: CredentialDeclaration,
    ): Boolean = declaration.read || HeaderLookup.asciiLowercase(method) in readMethods

    /**
     * Returns what a checked [credential], with its session token as [session] read it for this
     * request, and a checked [language] send to an operation declaring [declaration], or refuses
     * them with [ConfigurationException].
     *
     * The operation's own declarations decide, in this order: a secret key where one does not
     * authenticate it, and every other credential where one does; a share token where it takes a
     * session; no session token where it requires one, the failure of the mint that left none
     * riding along as the refusal's cause; a session token minted without `user_id` where it acts as one;
     * and a share token where it declares none. A share token that asks for the visitor's cookies
     * sends them to every operation that takes a credential, and a read that declares no share
     * token is sent the cookies alone, one that requires a session token among them, since only the
     * server can tell whether the visitor's cookies hold one; [isRead] is what tells a read, as
     * [CredentialHeaders.isRead] says. The language is [language], or [ANY_LANGUAGE] where it is null,
     * whatever the operation declares.
     */
    fun project(
        credential: Credential,
        session: ResolvedSession?,
        language: String?,
        declaration: CredentialDeclaration,
        isRead: Boolean,
    ): Projection {
        val headers = LinkedHashMap<String, String>()
        if (declaration.takesNoCredential) {
            headers[ACCEPT_LANGUAGE_HEADER] = language ?: ANY_LANGUAGE
            return Projection(headers)
        }
        val cookies =
            when (credential) {
                is Credential.Publishable -> {
                    publishable(credential, session, declaration, headers)
                    null
                }

                is Credential.Share -> {
                    share(credential, declaration, isRead, headers)
                }

                is Credential.Secret -> {
                    secret(credential, declaration, headers)
                    null
                }
            }
        headers[ACCEPT_LANGUAGE_HEADER] = language ?: ANY_LANGUAGE
        return Projection(headers, cookies)
    }

    private fun publishable(
        credential: Credential.Publishable,
        session: ResolvedSession?,
        declaration: CredentialDeclaration,
        headers: MutableMap<String, String>,
    ) {
        if (declaration.requiresSecretKey) {
            throw ConfigurationException(
                "This operation mints a session token, which a secret key alone authenticates, on a " +
                    "server: a publishable key cannot call it.",
            )
        }
        val token = session?.token
        if (token == null) {
            if (declaration.requiresSession) {
                throw ConfigurationException(
                    "This operation requires a session token beside the publishable key, and none is set.",
                    cause = session?.mintError,
                )
            }
            headers[API_KEY_HEADER] = credential.key
            return
        }
        // Only a token whose user the client is told can be refused for naming none: one given as a
        // value or by a function is sent for the API to decide.
        val user = session.user
        if (declaration.actingUser == CredentialDeclaration.ActingUser.Required &&
            user is ResolvedSession.User.Known &&
            user.id == null
        ) {
            throw ConfigurationException(
                "This operation acts as the user its session token was minted for, and the token was " +
                    "minted without user_id: mint it with `postSession`'s `user_id` set to the user.",
            )
        }
        headers[API_KEY_HEADER] = credential.key
        headers[SESSION_TOKEN_HEADER] = token
    }

    private fun share(
        credential: Credential.Share,
        declaration: CredentialDeclaration,
        isRead: Boolean,
        headers: MutableMap<String, String>,
    ): CookieSource? {
        if (declaration.requiresSecretKey) {
            throw ConfigurationException(
                "This operation mints a session token, which a secret key alone authenticates, on a " +
                    "server: a share token cannot call it.",
            )
        }
        val cookies = credential.cookies
        if (declaration.requiresSession) {
            // A session the visitor's cookies hold is invisible here and plain to the server, so a read
            // under a token that asks for them goes out on them, for the server to judge.
            if (cookies != null && isRead) return cookies
            throw ConfigurationException(
                "This operation requires a session token, which accompanies a publishable key: a " +
                    "share token cannot call it.",
            )
        }
        if (declaration.acceptsShareToken) {
            headers[SHARE_TOKEN_HEADER] = credential.token
            return cookies
        }
        if (cookies == null || !isRead) {
            throw ConfigurationException(
                "A share token reads a shared resource and writes nothing, and this operation does not " +
                    "take one.",
            )
        }
        // A read that takes no share token rides the visitor's own session alone.
        return cookies
    }

    private fun secret(
        credential: Credential.Secret,
        declaration: CredentialDeclaration,
        headers: MutableMap<String, String>,
    ) {
        if (!declaration.requiresSecretKey) {
            throw ConfigurationException(
                "A secret key authenticates only the operation that mints a session token, and the API " +
                    "answers it anywhere else as if no credential had been sent.",
            )
        }
        headers[API_KEY_HEADER] = credential.key
    }

    /**
     * Refuses a member that is missing, that no header carries, or whose prefix is not [prefix], in
     * that order, so an empty key is named as missing rather than as a wrong prefix.
     */
    private fun expect(
        value: String,
        prefix: String,
        part: String,
    ) {
        val expected = prefixes.firstOrNull { it.first == prefix }?.second ?: "this part"
        if (HeaderValue.isBlank(value)) {
            throw ConfigurationException("$part is empty or whitespace alone, which is a missing one: pass $expected.")
        }
        if (!HeaderValue.isCarried(value)) {
            throw ConfigurationException(
                "$part holds a control character or a character outside printable ASCII, which the platform's " +
                    "HTTP stack does not send: pass $expected as it was issued.",
            )
        }
        if (value.startsWith(prefix)) return
        val found = prefixes.firstOrNull { value.startsWith(it.first) }
        val message =
            buildString {
                append("$part starts with `$prefix`")
                if (found != null) {
                    append(", and the one given starts with `${found.first}`, which is ${found.second}'s")
                }
                append(": pass $expected here.")
            }
        throw ConfigurationException(message)
    }
}
