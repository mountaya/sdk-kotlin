package com.mountaya.client

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH

/**
 * SHA-256 from CommonCrypto, through the C interface Kotlin/Native generates for it, which is
 * marked experimental for every C library alike.
 *
 * An empty array has no first element to pin, so an empty input is handed over as no pointer and
 * a length of zero, which CommonCrypto reads as the empty message.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun sha256(bytes: ByteArray): ByteArray {
    val digest = ByteArray(CC_SHA256_DIGEST_LENGTH)
    digest.usePinned { output ->
        val written = output.addressOf(0).reinterpret<UByteVar>()
        if (bytes.isEmpty()) {
            CC_SHA256(null, 0u, written)
        } else {
            bytes.usePinned { input -> CC_SHA256(input.addressOf(0), bytes.size.convert(), written) }
        }
    }
    return digest
}
