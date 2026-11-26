package com.mountaya.client

/** The header a conditional write's tag is sent in. */
internal const val IF_MATCH_HEADER = "If-Match"

/** ConditionalTag decides the `If-Match` a conditional write sends. */
internal object ConditionalTag {
    /**
     * Returns the tag a call sends on every attempt, exactly as its caller named it, or null where
     * it sends none.
     *
     * The client never writes a tag its caller did not name, whatever tag it has seen: a write
     * without one replaces what is there, which is what a caller who named none asked for. A tag
     * that is empty or that no header can carry is refused with [ConfigurationException] before
     * any request; anything else is the API's to judge. An operation that has not [declared] an
     * `If-Match` is sent none, and the tag given to it is not read.
     */
    fun tag(
        declared: Boolean,
        given: String?,
    ): String? {
        if (!declared || given == null) return null
        if (given.isEmpty() || !HeaderValue.isCarried(given)) {
            throw ConfigurationException(
                "The tag is empty, or holds a control character or a character outside printable ASCII, which " +
                    "the platform's HTTP stack does not send: pass the resource's `etag` as the API wrote it, or `*`.",
            )
        }
        return given
    }
}
