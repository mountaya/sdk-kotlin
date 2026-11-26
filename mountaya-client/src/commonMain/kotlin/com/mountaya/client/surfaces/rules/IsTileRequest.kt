package com.mountaya.client

/** The port a scheme reaches where a URL names none. */
private val defaultPorts = mapOf("http" to 80, "https" to 443)

/**
 * isTileRequest tells whether [url] is a request to the Tile API that [tiles] reaches: an `http` or
 * `https` URL of the same origin, its scheme and host read without regard to case and its port with
 * the scheme's default filled in, carrying no user name and no password, whose path, its dot
 * segments resolved, is the base URL's own path or lies under it at a segment boundary.
 *
 * So under the default base, `https://tiles.mountaya.com/v1/zones/12/2093/1425` is one, and
 * `https://tiles.mountaya.com.example.net/v1/zones` is not; under `https://example.com/tiles`,
 * `https://example.com/tiles/v1/zones` is one, and `https://example.com/tiles-private` is not. A
 * trailing slash on the base is nothing. It is how a map's request hook sends a credential to the Tile
 * API and to no other host a style names (`sdk/SPEC.md` §1). A URL is read as RFC 3986 writes one,
 * in common code, since no platform's parser is one every target has.
 */
internal fun isTileRequest(
    url: Url,
    tiles: Url,
): Boolean {
    val request = UrlParts.of(url.toString()) ?: return false
    val base = UrlParts.of(tiles.toString()) ?: return false
    val port = defaultPorts[request.scheme] ?: return false
    if (base.scheme != request.scheme || request.hasUserInfo || request.host.isEmpty()) return false
    if (request.host != base.host || (request.port ?: port) != (base.port ?: port)) return false
    val basePath = base.path.trimEnd('/')
    // A percent-encoded path is ASCII, compared character for character.
    return basePath.isEmpty() || request.path == basePath || request.path.startsWith("$basePath/")
}

/**
 * UrlParts is what [isTileRequest] reads of a URL: its scheme and its host lowercased in ASCII,
 * whether it names a user or a password, the port it names, and its path with its dot segments
 * resolved, as RFC 3986 sections 3 and 5.2.4 write them.
 */
private class UrlParts(
    val scheme: String,
    val hasUserInfo: Boolean,
    val host: String,
    val port: Int?,
    val path: String,
) {
    companion object {
        /** Reads [text], or answers null for one with no scheme, no authority or a port that is no number. */
        fun of(text: String): UrlParts? {
            val colon = text.indexOf(':')
            if (colon <= 0) return null
            val scheme = HeaderLookup.asciiLowercase(text.substring(0, colon))
            val rest = text.substring(colon + 1)
            if (!rest.startsWith("//")) return null
            val afterSlashes = rest.substring(2)
            val authorityEnd = afterSlashes.indexOfFirst { it == '/' || it == '?' || it == '#' }
            val authority = if (authorityEnd < 0) afterSlashes else afterSlashes.substring(0, authorityEnd)
            val tail = if (authorityEnd < 0) "" else afterSlashes.substring(authorityEnd)
            val pathEnd = tail.indexOfFirst { it == '?' || it == '#' }
            val path = if (pathEnd < 0) tail else tail.substring(0, pathEnd)

            val at = authority.lastIndexOf('@')
            val hostAndPort = if (at < 0) authority else authority.substring(at + 1)
            val portStart =
                if (hostAndPort.startsWith("[")) {
                    val close = hostAndPort.indexOf(']')
                    if (close < 0) return null
                    if (close + 1 < hostAndPort.length && hostAndPort[close + 1] != ':') return null
                    if (close + 1 < hostAndPort.length) close + 1 else -1
                } else {
                    hostAndPort.lastIndexOf(':')
                }
            val host = if (portStart < 0) hostAndPort else hostAndPort.substring(0, portStart)
            val digits = if (portStart < 0) "" else hostAndPort.substring(portStart + 1)
            if (!digits.all { it in '0'..'9' }) return null
            val port = if (digits.isEmpty()) null else digits.toIntOrNull() ?: return null
            return UrlParts(scheme, at >= 0, HeaderLookup.asciiLowercase(host), port, withoutDotSegments(path))
        }

        /** [path] with its `.` and `..` segments resolved, RFC 3986 section 5.2.4's algorithm. */
        private fun withoutDotSegments(path: String): String {
            var input = path
            val output = StringBuilder()
            while (input.isNotEmpty()) {
                when {
                    input.startsWith("../") -> {
                        input = input.substring(3)
                    }

                    input.startsWith("./") -> {
                        input = input.substring(2)
                    }

                    input.startsWith("/./") -> {
                        input = input.substring(2)
                    }

                    input == "/." -> {
                        input = "/"
                    }

                    input.startsWith("/../") || input == "/.." -> {
                        input = "/" + input.substring(if (input == "/..") 3 else 4)
                        val last = output.lastIndexOf("/")
                        output.setLength(if (last < 0) 0 else last)
                    }

                    input == "." || input == ".." -> {
                        input = ""
                    }

                    else -> {
                        val next = input.indexOf('/', startIndex = 1)
                        val segment = if (next < 0) input else input.substring(0, next)
                        output.append(segment)
                        input = if (next < 0) "" else input.substring(next)
                    }
                }
            }
            return output.toString()
        }
    }
}
