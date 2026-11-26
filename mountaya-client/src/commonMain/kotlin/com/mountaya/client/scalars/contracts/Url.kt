package com.mountaya.client

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/** A scheme, then a colon, then at least one more character, none a line break: the least RFC 3986 calls a URI. */
private val SCHEME = Regex("[A-Za-z][A-Za-z0-9+.-]*:.+")

/**
 * Url is a URL as it was written: an origin a client sends a surface's requests to, or a web address
 * an answer carries or a request sends.
 *
 * It is the one URL type of this SDK, beside `Instant` for the shared scalars, so the Data API's
 * `URL` scalar and REST's `format: uri` are one Kotlin type. It holds the text and parses nothing
 * out of it: the text is what is sent and compared, and it is kept as written, so an answer's web
 * address reads as the text the wire sent, one with no scheme or one a platform's address type
 * would rewrite included, and never fails the answer it arrives in. On the wire it is the JSON
 * string it holds.
 *
 * What the client sends is held to a scheme, a colon and at least one more character, none a line
 * break, where it is sent: a web address with none in a body a caller builds, a path or a host
 * alone, is refused with a [ConfigurationException] before any request leaves, and so is a base URL
 * with none where [BaseUrls] holds it.
 */
@Serializable(with = UrlSerializer::class)
@JvmInline
public value class Url(
    /**
     * The URL exactly as it was written, the text a request sends and an answer sent, as `sdk-swift`'s `URI.text`
     * and `sdk-dart`'s `Url.text` read it.
     */
    public val text: String,
) {
    /** Whether the text starts with a scheme, a colon and at least one more character, none a line break. */
    internal val hasScheme: Boolean get() = SCHEME.matches(text)

    /** Returns the URL exactly as it was written. */
    override fun toString(): String = text
}
