package com.mountaya.geo

/**
 * Returns [text] in Unicode's canonical decomposition, NFD: each composed letter written as its base
 * and its combining marks in canonical order, so `É` is `E` and a combining acute. The platform's
 * normalizer writes it, Unicode's own mapping on every target: `java.text.Normalizer` on the jvm and
 * android, Foundation's canonical mapping on iOS.
 */
internal expect fun canonicalDecomposition(text: String): String
