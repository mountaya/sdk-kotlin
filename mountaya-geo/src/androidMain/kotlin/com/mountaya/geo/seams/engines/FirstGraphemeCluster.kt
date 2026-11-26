package com.mountaya.geo

import java.text.BreakIterator
import java.util.Locale

/**
 * `java.text.BreakIterator`'s character instance, under the root locale so no reader's locale moves a
 * boundary, which reads the extended grapheme clusters of Unicode's text segmentation.
 */
internal actual fun firstGraphemeCluster(text: String): String {
    if (text.isEmpty()) return ""
    val boundaries = BreakIterator.getCharacterInstance(Locale.ROOT)
    boundaries.setText(text)
    val end = boundaries.next()
    return if (end == BreakIterator.DONE) text else text.substring(0, end)
}
