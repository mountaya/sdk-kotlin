package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.generated.elementsWordBundle
import kotlinx.serialization.json.JsonObject

/**
 * WordBundle is the words a surface reads, as the tree a bundle's JSON writes them: a word is a
 * string leaf, and a section an object of words and sections. A typed set of words is decoded from
 * the tree a bundle resolves to, so one fold serves every surface's words.
 *
 * [english] is the words every language falls back to, so a bundle always holds them. [translations]
 * are the other languages the bundle carries, each a whole set of words, and [partial] the languages
 * it carries only some words for, each folded over the language's base. A translation keyed English
 * stands ahead of [english], as a translation of any language stands ahead of the fallback.
 *
 * Two are equal where they hold the same words.
 */
public class WordBundle(
    /** The English words, the base of every language with no translation of its own. */
    public val english: JsonObject,
    /** The translations the bundle carries, keyed by language. */
    public val translations: Map<RestSchema.Language, JsonObject> = emptyMap(),
    /** The words the bundle carries for a language beside its base, keyed by language. */
    public val partial: Map<RestSchema.Language, JsonObject> = emptyMap(),
) {
    override fun equals(other: Any?): Boolean =
        other is WordBundle && other.english == english && other.translations == translations &&
            other.partial == partial

    override fun hashCode(): Int = (english.hashCode() * 31 + translations.hashCode()) * 31 + partial.hashCode()

    override fun toString(): String = "WordBundle(english=$english, translations=$translations, partial=$partial)"

    /** The bundles the contract carries. */
    public companion object {
        /**
         * The words the elements put on screen, by section, as the contract's words table holds them: English, which
         * every language falls back to, and every translation the table carries. A word is a string, a template a
         * string holding `{name}` placeholders, and a counted word an object of the forms CLDR's plural rules name,
         * `other` among them. A consumer's own words are folded over them by `resolveBundle`.
         */
        public val elements: WordBundle get() = elementsWordBundle
    }
}
