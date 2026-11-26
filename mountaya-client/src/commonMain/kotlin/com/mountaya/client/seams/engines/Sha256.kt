package com.mountaya.client

/**
 * Returns the SHA-256 digest of [bytes], 32 bytes long, from the platform's own implementation.
 *
 * Every target this module builds for has one, so no digest is ever written here by hand: a
 * hand-rolled one is a second implementation of something the platform already proves.
 */
internal expect fun sha256(bytes: ByteArray): ByteArray
