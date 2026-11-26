package com.mountaya.admin

/**
 * Delivery is the contract's first signature vector, which the webhook tests sign every other
 * delivery against, so they agree with the contract as well as with themselves.
 */
internal object Delivery {
    /** The secret of the contract's first signature vector. */
    const val SECRET = "whsk_8f3c2a71b845d0e4c7a6b3f180d92e5a17c4e9b0d36f2a58e1c7b4093d6a2f15"

    /** The secret the vectors rotate to, which signs the second `v1` of a rotation. */
    const val ROTATED_SECRET = "whsk_2d9e41f7a0c35b86e2f1d4c907a3b58e6c1f2d4a9b07e35c8d6f1a2b4c3e5d70"

    /** The `t` of the contract's vectors: the Unix second the delivery was signed at. */
    const val SIGNED_AT: Long = 1_763_111_564

    /** The signature the contract's vectors give for [BODY] under [SECRET]. */
    const val VECTOR_SIGNATURE = "ad18de77c0892dba398f210f0f3205666e41c17025feaf8d42ae5998d8545f5d"

    /** The body of the contract's vectors, an `organization.member.updated` event. */
    const val BODY =
        """{"id":"019a8eed-e1ad-7753-b049-b0e20d79577a","object":"webhook_event",""" +
            """"type":"organization.member.updated","api_version":"0.8.0","created_at":"2026-11-14T09:12:44Z",""" +
            """"organization_id":"d5a83e17-92c6-4b0f-a7d3-1e6482f9c05b","task":"organization.member.update",""" +
            """"related_object":{"type":"organization.member","id":"7a4e1c98-3b52-4f60-9d81-c05e2f7a83b1"},""" +
            """"data":{"object":{"object":"organization.member","user_id":"7a4e1c98-3b52-4f60-9d81-c05e2f7a83b1",""" +
            """"role":"contributor","joined_at":"2026-03-11T14:02:09Z"},"previous_attributes":{"role":"viewer"}},""" +
            """"request":{"id":"2c5ed6d8b8f7c706e9a0d5c5144ed90b"},"actor":{"type":"user",""" +
            """"user_id":"1b9d4e72-0c35-4a8f-b6e1-5f2c7d904a18","key_id":"3b1f9c02-6d4e-4a87-b5c1-70e2af3d8916"}}"""

    /** Returns the signature of [body] signed at the `t` written [stated] under [secret]. */
    fun signatureOf(
        body: String,
        stated: String = "$SIGNED_AT",
        secret: String = SECRET,
    ): String = WebhookSignature.expected(stated, body.encodeToByteArray(), secret)

    /** Returns the header of a delivery signed at the vectors' `t` with each signature given. */
    fun headerOf(vararg signatures: String): String =
        (listOf("t=$SIGNED_AT") + signatures.map { "v1=$it" }).joinToString(",")
}
