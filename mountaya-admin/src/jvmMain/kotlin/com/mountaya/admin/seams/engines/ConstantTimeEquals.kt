package com.mountaya.admin

import java.security.MessageDigest

/** The comparison `MessageDigest.isEqual` makes, whose time depends on the lengths alone, never on the bytes. */
internal actual fun constantTimeEquals(
    a: ByteArray,
    b: ByteArray,
): Boolean = MessageDigest.isEqual(a, b)
