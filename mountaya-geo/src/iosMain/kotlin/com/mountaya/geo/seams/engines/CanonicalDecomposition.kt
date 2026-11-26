package com.mountaya.geo

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSString
import platform.Foundation.create
import platform.Foundation.decomposedStringWithCanonicalMapping

/**
 * Foundation's `decomposedStringWithCanonicalMapping`, Unicode's NFD. Building the `NSString` the
 * mapping is read from is Kotlin/Native's beta interop, which the two canonical mappings alone opt into.
 */
@OptIn(BetaInteropApi::class)
internal actual fun canonicalDecomposition(text: String): String =
    NSString.create(string = text).decomposedStringWithCanonicalMapping
