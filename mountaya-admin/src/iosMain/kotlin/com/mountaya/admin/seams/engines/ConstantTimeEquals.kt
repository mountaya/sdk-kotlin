package com.mountaya.admin

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.posix.timingsafe_bcmp

/**
 * The comparison `timingsafe_bcmp` makes, through the C interface Kotlin/Native generates for it,
 * which is marked experimental for every C library alike: its time depends on the length alone.
 *
 * It compares two runs of one length, so arrays of different lengths are unequal before it is asked,
 * and two empty arrays, which have no first element to pin, are equal.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun constantTimeEquals(
    a: ByteArray,
    b: ByteArray,
): Boolean {
    if (a.size != b.size) return false
    if (a.isEmpty()) return true
    return a.usePinned { first ->
        b.usePinned { second -> timingsafe_bcmp(first.addressOf(0), second.addressOf(0), a.size.convert()) == 0 }
    }
}
