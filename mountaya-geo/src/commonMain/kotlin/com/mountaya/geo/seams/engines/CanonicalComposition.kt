package com.mountaya.geo

/**
 * Returns [text] in Unicode's canonical composition, NFC: decomposed, then each base and the marks
 * that compose with it written as the one letter they make, so `E` and a combining acute are `É`, and
 * two Hangul jamo one syllable. `java.text.Normalizer` on the jvm and android, Foundation's canonical
 * mapping on iOS.
 */
internal expect fun canonicalComposition(text: String): String
