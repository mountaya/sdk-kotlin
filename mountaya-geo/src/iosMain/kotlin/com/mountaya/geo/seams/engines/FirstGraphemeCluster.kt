package com.mountaya.geo

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSString
import platform.Foundation.create
import platform.Foundation.rangeOfComposedCharacterSequenceAtIndex

/** A carriage return and the line feed after it, which Unicode's segmentation never parts (its rule GB3). */
private const val LINE_END = "\r\n"

/**
 * Foundation's `rangeOfComposedCharacterSequenceAtIndex`, the composed character sequence that opens the
 * text: Foundation's reading of an extended grapheme cluster, the characters a reader composes. It parts a
 * carriage return from the line feed after it, which compose nothing, so that one pair is read as Unicode's
 * segmentation reads it. Building the `NSString` it is read from is Kotlin/Native's beta interop, as the
 * canonical mappings' is, and reading the `NSRange` it answers, a C structure, is its foreign-memory
 * interop, as the digests' is.
 */
@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
internal actual fun firstGraphemeCluster(text: String): String {
    if (text.isEmpty()) return ""
    if (text.startsWith(LINE_END)) return LINE_END
    val length = NSString.create(string = text).rangeOfComposedCharacterSequenceAtIndex(0u).useContents { length }
    return text.substring(0, length.toInt().coerceIn(1, text.length))
}
