package com.mountaya.geo

import kotlinx.serialization.json.JsonObject

/**
 * wordOverridesOf reads a consumer's override as JSON writes it, [words], into its shape: keyed by
 * language where every key is a language a map labels in, spelled as the wire spells it (`fr`, never
 * `FR` or `fr-CA`), and keyed by section where none is. An empty override is keyed by section and
 * changes nothing.
 *
 * Every one of the seven languages is a language key, never only the ones a bundle is translated
 * into: `{"de": {...}}` over a bundle with no German is German words, where read as sections it would
 * be a section named `de` folded onto English. No section of a bundle is named after a language.
 *
 * Null, the refusal, for an override naming a language and a section together: one key decides the
 * shape, so such an override's sections would be folded in no language. A language whose words are
 * not an object, `null` among them, is a language left out.
 */
public fun wordOverridesOf(words: JsonObject): WordOverrides? {
    val languages = labelLanguages.associateBy { it.value }
    val named = words.keys.count { it in languages }
    return when (named) {
        0 -> {
            WordOverrides.BySection(words)
        }

        words.size -> {
            WordOverrides.ByLanguage(
                buildMap {
                    for ((key, value) in words) {
                        val language = languages.getValue(key)
                        if (value is JsonObject) put(language, value)
                    }
                },
            )
        }

        else -> {
            null
        }
    }
}
