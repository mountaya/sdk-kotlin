package com.mountaya.geo

import java.text.Collator
import java.util.Locale

/**
 * `java.text`'s `Collator` for the locale the tag names, at its default strength, which tells letters apart by
 * their accents and their case only where nothing else does. Both the jvm and android carry it, and on android
 * it is the platform's own ICU. A collator is built for each comparison, since one is not safe to share between
 * threads.
 */
internal actual fun localeCollatedBefore(
    first: String,
    second: String,
    languageTag: String,
): Boolean = Collator.getInstance(Locale.forLanguageTag(languageTag)).compare(first, second) < 0
