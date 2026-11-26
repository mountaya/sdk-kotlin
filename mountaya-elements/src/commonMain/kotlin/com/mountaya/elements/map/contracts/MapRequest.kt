package com.mountaya.elements

import com.mountaya.client.Url

/**
 * MapRequest is a request a map's renderer makes for itself, for its style, a tile, a sprite or a
 * glyph, as an app's own hook reads and rewrites it before the client's credential is attached: the
 * URL it goes to and the headers it carries.
 *
 * A header's name is read without regard to case where the request is sent, so a name the hook writes
 * in another case is the same header. Two requests are equal where they go to the same URL and carry
 * the same headers, each name and value as written.
 */
public class MapRequest(
    /** Where the request goes. */
    public val url: Url,
    /** Every header the request carries, by name. */
    public val headers: Map<String, String> = emptyMap(),
) {
    override fun equals(other: Any?): Boolean = other is MapRequest && other.url == url && other.headers == headers

    override fun hashCode(): Int = url.hashCode() * 31 + headers.hashCode()

    /**
     * Returns the request as a log shows it: its URL and the names of its headers, never a value, which may be a
     * credential.
     */
    override fun toString(): String = "MapRequest($url, headers=${headers.keys})"
}
