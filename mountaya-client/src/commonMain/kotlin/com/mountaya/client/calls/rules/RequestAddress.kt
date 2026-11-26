package com.mountaya.client

/**
 * RequestAddress writes the address one request goes to: its surface's origin, the path, and the
 * query, each name and value percent-encoded.
 */
internal object RequestAddress {
    /**
     * Returns the address of [path] under [origin] with [query].
     *
     * An origin may carry a path of its own, such as a proxy's prefix, which the request's path
     * follows. Only RFC 3986's unreserved characters stay as they are in a query name or value, so
     * a `+`, a `&` or a `=` inside a value stays inside it.
     */
    fun url(
        origin: Url,
        path: String,
        query: List<Pair<String, String>>,
    ): Url {
        val text =
            buildString {
                append(origin.toString().trimEnd('/'))
                if (!path.startsWith("/")) append('/')
                append(path)
                if (query.isNotEmpty()) {
                    append('?')
                    query.joinTo(
                        this,
                        "&",
                    ) { (name, value) -> "${encodePathSegment(name)}=${encodePathSegment(value)}" }
                }
            }
        return Url(text)
    }
}
