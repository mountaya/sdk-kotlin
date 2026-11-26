package com.mountaya.geo

import com.mountaya.client.InternalMountayaApi
import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * ElementsScope is what every element under one scope reads (`geo.i18n.nested-scope`): the language its
 * words are in, the locale its figures are written in, the words themselves, resolved for that language,
 * and the two glyph sets, the one its controls draw with and the one the places it marks are drawn with.
 *
 * [standard] is the scope outside every statement: English, written in the locale `en`, with the elements'
 * own words and the bundled glyph sets. A nested scope is `scopeOf` of the scope around it and what it
 * states, which is how a composition carries one into an element.
 *
 * Its constructor is `internal`, so a member the contract adds breaks no caller: a scope is [standard], or
 * a scope nested in it. Two are equal where they hold the same, a JSON value as the serialization library
 * compares it.
 *
 * A composable's own `strings` stand over the scope's words for it alone: [withStrings] answers the scope with them
 * layered on, each word a path names read there, before the scope's own, by the composable and every rule it hands
 * the scope to. It is Mountaya's own, read by `mountaya-elements` across the module boundary (SPEC.md section 7).
 */
@OptIn(InternalMountayaApi::class)
public class ElementsScope internal constructor(
    /** The language the scope's words are in. */
    public val language: RestSchema.Language,
    /** The language tag a number, a date and a moment are written in, such as `fr-CH`. */
    public val locale: String,
    /**
     * The scope's words, by section, resolved for [language] from the elements' own with the overrides
     * folded on (`resolveBundle`).
     */
    public val words: JsonObject,
    /** The glyph set the scope's controls draw with. */
    public val controlGlyphs: GlyphSet,
    /** The glyph set the places the scope's elements mark are drawn with. */
    public val markerGlyphs: GlyphSet,
    /**
     * The words a composable states for itself, by path, its section and its key joined by dots, each standing over
     * the scope's own word there; none outside every composable's own.
     */
    @InternalMountayaApi
    public val strings: Map<String, String>,
) {
    /**
     * Returns the word the scope reads at [path], its section and its key joined by dots, `controls.zoomIn`: the one a
     * composable's own strings name there, else the scope's own where it is text, else the English word of the
     * elements' own words, which hold every key, where an override left text out, and the path itself where none holds
     * text, so an element never draws a blank where a word belongs.
     */
    public fun word(path: String): String = text(path) ?: path

    /**
     * text is the word the scope reads at [path] as [word] reads it, null where neither a composable's own strings,
     * the scope's words nor the elements' own English words hold text there.
     */
    internal fun text(path: String): String? =
        strings[path] ?: textAt(path, words) ?: textAt(path, WordBundle.elements.english)

    /**
     * Answers this scope with [strings] layered over it, the words a composable states for itself by path, each
     * standing over the word there, those of the scope already standing where [strings] names none, and this scope
     * itself where they are null or empty, so a composable handed none provides the very scope around it.
     */
    @InternalMountayaApi
    public fun withStrings(strings: Map<String, String>?): ElementsScope =
        if (strings.isNullOrEmpty()) {
            this
        } else {
            ElementsScope(language, locale, words, controlGlyphs, markerGlyphs, this.strings + strings)
        }

    override fun equals(other: Any?): Boolean =
        other is ElementsScope &&
            other.language == language &&
            other.locale == locale &&
            other.words == words &&
            other.controlGlyphs == controlGlyphs &&
            other.markerGlyphs == markerGlyphs &&
            other.strings == strings

    override fun hashCode(): Int {
        var hash = language.hashCode()
        hash = 31 * hash + locale.hashCode()
        hash = 31 * hash + words.hashCode()
        hash = 31 * hash + controlGlyphs.hashCode()
        hash = 31 * hash + markerGlyphs.hashCode()
        return 31 * hash + strings.hashCode()
    }

    override fun toString(): String =
        "ElementsScope(language=$language, locale=$locale, controlGlyphs=${controlGlyphs.name}, " +
            "markerGlyphs=${markerGlyphs.name})"

    /** The scope outside every statement. */
    public companion object {
        /**
         * The scope outside every statement: English, the locale `en`, the elements' own words, the bundled control set
         * and the bundled marker set.
         */
        public val standard: ElementsScope =
            ElementsScope(
                RestSchema.Language.En,
                "en",
                resolveBundle(WordBundle.elements),
                GlyphSet.control,
                GlyphSet.marker,
                emptyMap(),
            )

        /** The text [words] holds at the dotted [path], or null where it holds none there. */
        private fun textAt(
            path: String,
            words: JsonObject,
        ): String? {
            val keys = path.split(".")
            var section: JsonObject = words
            for (key in keys.dropLast(1)) section = section[key] as? JsonObject ?: return null
            val word: JsonElement = section[keys.last()] ?: return null
            return (word as? JsonPrimitive)?.takeIf { it.isString }?.content
        }
    }
}
