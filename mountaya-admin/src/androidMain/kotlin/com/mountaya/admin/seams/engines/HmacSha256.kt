package com.mountaya.admin

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/** The algorithm's name, as `Mac` and `SecretKeySpec` know it. */
private const val HMAC_SHA256 = "HmacSHA256"

/**
 * HMAC-SHA256 from `javax.crypto.Mac`, which every Java and Android runtime is required to provide.
 *
 * `SecretKeySpec` refuses an empty key, so the empty key is handed over as the one zero byte RFC 2104
 * pads to the same block.
 */
internal actual fun hmacSha256(
    key: ByteArray,
    message: ByteArray,
): ByteArray {
    val mac = Mac.getInstance(HMAC_SHA256)
    mac.init(SecretKeySpec(if (key.isEmpty()) ByteArray(1) else key, HMAC_SHA256))
    return mac.doFinal(message)
}
