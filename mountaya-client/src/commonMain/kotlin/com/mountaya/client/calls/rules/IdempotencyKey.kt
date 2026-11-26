package com.mountaya.client

import kotlin.uuid.Uuid

/** The header a write's key is sent in. */
internal const val IDEMPOTENCY_KEY_HEADER = "Idempotency-Key"

/** IdempotencyKey decides the `Idempotency-Key` a call sends on every one of its attempts. */
internal object IdempotencyKey {
    /**
     * Returns the key a call sends, or null where it sends none.
     *
     * An operation that declares no key is sent none, the caller's included, which is then not read
     * at all. One that [declared] the header is sent the caller's key as [given], refused with
     * [ConfigurationException] before any request where it is empty or no header can carry it, or
     * else a version 4 UUID drawn once by [draw], in its lowercase canonical form, before the first
     * attempt. Where the runtime offers no cryptographic source the call sends none, never one drawn
     * from a weaker source.
     */
    fun key(
        declared: Boolean,
        given: String?,
        draw: () -> Uuid?,
    ): String? {
        if (!declared) return null
        if (given != null) {
            if (given.isEmpty() || !HeaderValue.isCarried(given)) {
                throw ConfigurationException(
                    "The idempotency key is empty, or holds a control character or a character outside " +
                        "printable ASCII, which the platform's HTTP stack does not send: pass a key such as a UUID.",
                )
            }
            return given
        }
        return draw()?.toString()?.lowercase()
    }
}
