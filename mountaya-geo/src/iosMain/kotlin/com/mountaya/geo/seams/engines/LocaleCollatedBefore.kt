package com.mountaya.geo

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSMakeRange
import platform.Foundation.NSOrderedAscending
import platform.Foundation.NSString
import platform.Foundation.compare
import platform.Foundation.create

/**
 * Foundation's locale-aware comparison of strings, `compare(_:options:range:locale:)` with no option, in the
 * locale [platformLocale] reads the tag as. Building the `NSString` it compares is Kotlin/Native's beta interop,
 * as the canonical mappings' is, and the range it compares over, a C structure, its foreign-memory interop, as
 * the composed character sequences' is.
 */
@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
internal actual fun localeCollatedBefore(
    first: String,
    second: String,
    languageTag: String,
): Boolean {
    val text = NSString.create(string = first)
    val range = NSMakeRange(0uL, text.length)
    return text.compare(second, 0uL, range, platformLocale(languageTag)) == NSOrderedAscending
}
