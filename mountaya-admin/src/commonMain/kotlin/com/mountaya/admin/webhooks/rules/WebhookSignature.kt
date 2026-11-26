package com.mountaya.admin

/**
 * WebhookSignature is the signature a webhook delivery carries: the lowercase hexadecimal
 * HMAC-SHA256 of `<t>.<body>`, keyed by the endpoint's whole secret, `whsk_` included, where `<t>` is
 * the header's `t` as written and `<body>` the body's bytes exactly as they arrived.
 */
internal object WebhookSignature {
    private const val HEX_DIGITS = "0123456789abcdef"

    /** Returns the signature of [body], signed at the `t` written [stated], under [secret]. */
    fun expected(
        stated: String,
        body: ByteArray,
        secret: String,
    ): String = hex(hmacSha256(secret.encodeToByteArray(), "$stated.".encodeToByteArray() + body))

    /** Returns [bytes] in lowercase hexadecimal, two characters a byte. */
    fun hex(bytes: ByteArray): String =
        buildString(bytes.size * 2) {
            for (byte in bytes) {
                val value = byte.toInt() and 0xFF
                append(HEX_DIGITS[value shr 4])
                append(HEX_DIGITS[value and 0x0F])
            }
        }

    /**
     * Returns whether any of [candidates] is [expected].
     *
     * Every candidate is compared, a match found early included, so the time taken does not say which
     * of the secrets in force signed the delivery.
     */
    fun verifies(
        candidates: List<String>,
        expected: String,
    ): Boolean {
        var verified = false
        for (candidate in candidates) {
            val matches = equalInConstantTime(candidate, expected)
            verified = matches || verified
        }
        return verified
    }

    /**
     * Returns whether two signatures are the same bytes, by the platform's comparison in constant time,
     * so the time it takes says nothing about where they differ. A length is no secret: a candidate of
     * another length is unequal at once, and an uppercase digit is another byte.
     */
    fun equalInConstantTime(
        candidate: String,
        expected: String,
    ): Boolean = constantTimeEquals(candidate.encodeToByteArray(), expected.encodeToByteArray())
}
