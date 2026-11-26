package com.mountaya.navigation

/**
 * voiceLanguageOf answers the language a voice speaks in, each a BCP 47 tag: [language] where the device
 * holds a voice for it, else [fallback] where one is given and the device holds a voice for it, and null,
 * the speech engine's own default voice, where it holds neither. [isAvailable] answers whether the device
 * holds a voice for a tag. A fallback that is the language itself is tried once.
 *
 * The order `sdk-dart`'s voice tries its voices in, the host's language first, then its fallback, then the
 * platform's own.
 */
internal fun voiceLanguageOf(
    language: String,
    fallback: String?,
    isAvailable: (tag: String) -> Boolean,
): String? =
    when {
        isAvailable(language) -> language
        fallback != null && fallback != language && isAvailable(fallback) -> fallback
        else -> null
    }
