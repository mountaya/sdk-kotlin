package com.mountaya.geo

import java.text.Normalizer

/** `java.text.Normalizer`'s NFC, which the jvm and android both carry. */
internal actual fun canonicalComposition(text: String): String = Normalizer.normalize(text, Normalizer.Form.NFC)
