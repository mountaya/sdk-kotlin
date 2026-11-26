package com.mountaya.admin

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CCHmac
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.CoreCrypto.kCCHmacAlgSHA256

/**
 * HMAC-SHA256 from CommonCrypto, through the C interface Kotlin/Native generates for it, which is
 * marked experimental for every C library alike.
 *
 * An empty array has no first element to pin, so an empty key or message is handed over as no
 * pointer and a length of zero, which CommonCrypto reads as the empty one.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun hmacSha256(
    key: ByteArray,
    message: ByteArray,
): ByteArray {
    val mac = ByteArray(CC_SHA256_DIGEST_LENGTH)
    mac.usePinned { output ->
        key.withAddress { keyAddress ->
            message.withAddress { messageAddress ->
                CCHmac(
                    kCCHmacAlgSHA256,
                    keyAddress,
                    key.size.convert(),
                    messageAddress,
                    message.size.convert(),
                    output.addressOf(0),
                )
            }
        }
    }
    return mac
}

/** Runs [block] with the address of this array's first byte, pinned, or with none where it has no byte. */
@OptIn(ExperimentalForeignApi::class)
private inline fun ByteArray.withAddress(block: (CPointer<ByteVar>?) -> Unit) {
    if (isEmpty()) block(null) else usePinned { block(it.addressOf(0)) }
}
