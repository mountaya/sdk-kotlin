package com.mountaya.geo

import java.text.Normalizer

/** `java.text.Normalizer`'s NFD, which the jvm and android both carry. */
internal actual fun canonicalDecomposition(text: String): String = Normalizer.normalize(text, Normalizer.Form.NFD)
