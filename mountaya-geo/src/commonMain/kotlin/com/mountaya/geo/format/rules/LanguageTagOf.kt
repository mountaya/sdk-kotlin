package com.mountaya.geo

/** The language a locale that is no language tag is read in. */
internal const val FALLBACK_LANGUAGE: String = "en"

/** A well-formed language tag: a language of two to eight letters, then subtags of one to eight. */
private val languageTagPattern = Regex("^[A-Za-z]{2,8}(-[A-Za-z0-9]{1,8})*$")

/**
 * Returns [locale] as the language tag a platform's locale services are handed: an underscore
 * parts two subtags as a hyphen does, which is how a platform spells a locale and how the API reads
 * the `Accept-Language` header it answers in, and a value that is no language tag, the empty
 * string included, reads as [FALLBACK_LANGUAGE]. The locale is always an argument, never the
 * platform's default.
 */
internal fun languageTagOf(locale: String): String {
    val hyphenated = locale.replace('_', '-')
    return if (languageTagPattern.matches(hyphenated)) hyphenated else FALLBACK_LANGUAGE
}

/** Returns the primary language subtag of [locale] in lower case, `fr` for `fr-CA` and `FR_ca`. */
internal fun primaryLanguageOf(locale: String): String = languageTagOf(locale).substringBefore('-').lowercase()
