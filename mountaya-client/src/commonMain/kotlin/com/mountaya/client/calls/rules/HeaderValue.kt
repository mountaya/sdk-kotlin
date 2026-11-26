package com.mountaya.client

/**
 * HeaderValue decides whether a string a request carries as a header can be one.
 *
 * Every member of a credential, the client's language, an `Idempotency-Key`, an `If-Match` and the
 * caller's own headers ride a request as a header, so each is held to what a header carries before
 * any request leaves the process, and every attempt is held to it again as it is written.
 */
internal object HeaderValue {
    /**
     * Whether [value] is empty or whitespace alone, which is a missing value rather than a
     * malformed one. Whitespace is Unicode's `White_Space` property, spelled out here so that every
     * target reads the same set.
     */
    fun isBlank(value: String): Boolean = value.all { isWhitespace(it.code) }

    /**
     * Whether every character of [value] is printable ASCII, U+0020 to U+007E, the one range a header
     * carries in every SDK (`sdk/SPEC.md` §1 and §13), and what OkHttp, which Ktor sends with on the jvm
     * and Android, carries: it refuses any other character in a header, Latin-1's U+00A0 to U+00FF among
     * them. Every target is held to it alike, iOS included, whose URLSession would carry Latin-1, so a
     * call is sent alike wherever it is made. A control character, a line break and a tab are refused
     * with them.
     */
    fun isCarried(value: String): Boolean = value.all { it.code in 0x20..0x7E }

    private fun isWhitespace(code: Int): Boolean =
        code in 0x09..0x0D ||
            code == 0x20 ||
            code == 0x85 ||
            code == 0xA0 ||
            code == 0x1680 ||
            code in 0x2000..0x200A ||
            code == 0x2028 ||
            code == 0x2029 ||
            code == 0x202F ||
            code == 0x205F ||
            code == 0x3000
}
