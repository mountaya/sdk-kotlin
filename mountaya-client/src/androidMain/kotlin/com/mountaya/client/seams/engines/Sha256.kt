package com.mountaya.client

import java.security.MessageDigest

/** SHA-256 from `MessageDigest`, which every Java and Android runtime is required to provide. */
internal actual fun sha256(bytes: ByteArray): ByteArray = MessageDigest.getInstance("SHA-256").digest(bytes)
