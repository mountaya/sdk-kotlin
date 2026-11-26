package com.mountaya.client

/**
 * HeaderName decides whether a name a caller's header is sent under can be one.
 *
 * Ktor refuses a header name holding a space, a control character or one of `"(),/:;<=>?@[\]{}`, and
 * OkHttp an empty one or one holding anything outside visible ASCII, which leaves RFC 9110's token, so a
 * caller's name is held to it before any request leaves the process; Ktor also refuses
 * `Transfer-Encoding` and `Upgrade` from its caller, since the stack writes them itself.
 */
internal object HeaderName {
    /** Whether [name] is a token, one or more letters, digits and `!#$%&'*+-.^_|~` or a backtick, as RFC 9110 writes a field name. */
    fun isToken(name: String): Boolean = name.isNotEmpty() && name.all(::isTokenCharacter)

    /** Whether [name] is one the HTTP stack writes itself as it sends a request, and refuses from its caller. */
    fun isStackWritten(name: String): Boolean = HeaderLookup.asciiLowercase(name) in stackWritten

    private fun isTokenCharacter(character: Char): Boolean =
        character in 'a'..'z' || character in 'A'..'Z' || character in '0'..'9' || character in TOKEN_SYMBOLS

    /** The names the stack writes itself, folded as names are compared. */
    private val stackWritten: Set<String> = setOf("transfer-encoding", "upgrade")

    /** The characters a token holds beside letters and digits. */
    private const val TOKEN_SYMBOLS = "!#$%&'*+-.^_`|~"
}
