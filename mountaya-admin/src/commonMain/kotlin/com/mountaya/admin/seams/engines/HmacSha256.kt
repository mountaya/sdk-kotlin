package com.mountaya.admin

/**
 * Returns the HMAC-SHA256 of [message] under [key], 32 bytes long, from the platform's own
 * implementation of RFC 2104 over SHA-256.
 *
 * A key of any length is taken, the empty one included: RFC 2104 pads a key to SHA-256's 64-byte
 * block with zeros, and hashes first one longer than the block, so the empty key and a key of one
 * zero byte are the same key. Every target answers the same for each, whatever its platform accepts.
 */
internal expect fun hmacSha256(
    key: ByteArray,
    message: ByteArray,
): ByteArray
