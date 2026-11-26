package com.mountaya.geo

/**
 * Returns the first character a reader sees of [text], its first extended grapheme cluster: a letter
 * with every mark written over it, an emoji whole with its modifiers and joined parts, a flag's two
 * regional indicators, a Hangul syllable of its jamo, a carriage return with the line feed after it; the
 * empty text where [text] holds none. Common Kotlin reads no grapheme cluster, so the platform's
 * segmentation of Unicode's text answers it: `java.text.BreakIterator`'s character instance on the jvm
 * and android, and Foundation's composed character sequences on iOS.
 */
internal expect fun firstGraphemeCluster(text: String): String
