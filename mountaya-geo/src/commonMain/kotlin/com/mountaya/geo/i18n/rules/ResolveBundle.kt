package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

/**
 * The keys a fold never writes, each the name of a member every JavaScript object has: an override
 * read from JSON that states one is left out there, and here, so a word lands in every SDK alike. A
 * paint's rank band named so is left out by the same rule (`geo.style.map-paint`).
 */
internal val prototypeKeys: Set<String> = setOf("__proto__", "constructor", "prototype")

/**
 * resolveBundle answers the words [bundle] resolves to for [language], three layers folded innermost
 * first:
 *
 * 1. the base, the translation for the language: one the host handed over among [handedOver] ahead
 *    of one the bundle carries, and the bundle's English where neither stands, so a surface in a
 *    language nobody translated reads English rather than nothing;
 * 2. the bundle's partial layer for the language, so a language partly translated reads its own
 *    words where it has them;
 * 3. the consumer's [overrides], keyed by section for whichever language is selected, or keyed by
 *    language and folded for that language alone.
 *
 * A word is a leaf and replaces, a list replaces too, and two sections merge, at any depth; a word
 * written `null` is a word left out, so the one beneath it stands. A layer that changes no word
 * folds nothing, and the answer is then the very tree beneath it.
 */
public fun resolveBundle(
    bundle: WordBundle,
    language: RestSchema.Language = RestSchema.Language.En,
    overrides: WordOverrides? = null,
    handedOver: Map<RestSchema.Language, JsonObject> = emptyMap(),
): JsonObject {
    val base = handedOver[language] ?: bundle.translations[language] ?: bundle.english
    val patch =
        when (overrides) {
            null -> null
            is WordOverrides.BySection -> overrides.words
            is WordOverrides.ByLanguage -> overrides.words[language]
        }
    return listOfNotNull(bundle.partial[language], patch).fold(base) { resolved, layer ->
        if (changesNothing(resolved, layer)) resolved else foldOnto(resolved, layer)
    }
}

/** Whether folding [patch] onto [base] would change no word. */
private fun changesNothing(
    base: JsonObject,
    patch: JsonObject,
): Boolean =
    patch.all { (key, value) ->
        if (value is JsonNull || key in prototypeKeys) return@all true
        val held = base[key]
        held == value || (held is JsonObject && value is JsonObject && changesNothing(held, value))
    }

/** [base] with each word and section [patch] states folded on. */
private fun foldOnto(
    base: JsonObject,
    patch: JsonObject,
): JsonObject {
    val section = LinkedHashMap<String, JsonElement>(base)
    for ((key, value) in patch) {
        if (value is JsonNull || key in prototypeKeys) continue
        val current = section[key]
        section[key] = if (current is JsonObject && value is JsonObject) foldOnto(current, value) else value
    }
    return JsonObject(section)
}
