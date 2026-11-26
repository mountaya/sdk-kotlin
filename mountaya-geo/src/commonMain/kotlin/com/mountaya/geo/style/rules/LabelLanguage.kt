package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * labelLanguages are the languages a map labels in, the ones the contract's `Locales` is keyed by,
 * each a `name:{language}` field of the base map's tiles: English, French, Spanish, Italian, German,
 * Chinese and Japanese, English first as the one a tag naming none of them reads.
 */
public val labelLanguages: List<RestSchema.Language> =
    listOf(
        RestSchema.Language.En,
        RestSchema.Language.Fr,
        RestSchema.Language.Es,
        RestSchema.Language.It,
        RestSchema.Language.De,
        RestSchema.Language.Zh,
        RestSchema.Language.Ja,
    )

/**
 * labelLanguage reads the language a map labels in from a language [tag]: its primary subtag, the
 * run before the first hyphen or underscore, compared whole and without regard to case against
 * [labelLanguages]. So `fr-CA`, `fr_CA` and `FR` label in French and `zh-Hant-TW` in Chinese, while
 * `frr` (North Frisian, compared whole and never as a prefix), `pt-BR`, `und-FR`, `*`, the empty tag
 * and no tag at all label in English.
 *
 * An underscore parts subtags as a hyphen does, which is how the API reads the `Accept-Language`
 * header it answers in, so a map's labels and the words the API answers in are one language. The
 * case is compared over the ASCII letters, the only ones a language subtag is written in.
 */
public fun labelLanguage(tag: String?): RestSchema.Language {
    if (tag == null) return RestSchema.Language.En
    val end = tag.indexOfFirst { it == '-' || it == '_' }
    val primary = asciiLowercase(if (end < 0) tag else tag.substring(0, end))
    return labelLanguages.firstOrNull { it.value == primary } ?: RestSchema.Language.En
}
