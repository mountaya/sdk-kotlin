package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * ElementsScopeStatement is what a nested elements scope states (`geo.i18n.nested-scope`): every member
 * optional, and one it leaves null is the scope around it's (`scopeOf`).
 *
 * Two are equal where they state the same.
 */
public class ElementsScopeStatement(
    /** The language the scope's words are in, or null for the scope around it's. */
    public val language: RestSchema.Language? = null,
    /**
     * The language tag the scope's figures are written in, such as `fr-CH`, or null for the one its language
     * reads in: the scope around it's, or a language it states in its place.
     */
    public val locale: String? = null,
    /**
     * The scope's own words, keyed by section or by language as a bundle's overrides are, or null for none of
     * its own.
     */
    public val words: WordOverrides? = null,
    /** The glyph set the scope's controls draw with, or null for the scope around it's. It never stands in for the marker set. */
    public val controlGlyphs: GlyphSet? = null,
    /** The glyph set the places the scope's elements mark are drawn with, or null for the scope around it's. */
    public val markerGlyphs: GlyphSet? = null,
) {
    override fun equals(other: Any?): Boolean =
        other is ElementsScopeStatement &&
            other.language == language &&
            other.locale == locale &&
            other.words == words &&
            other.controlGlyphs == controlGlyphs &&
            other.markerGlyphs == markerGlyphs

    override fun hashCode(): Int {
        var hash = language.hashCode()
        hash = 31 * hash + locale.hashCode()
        hash = 31 * hash + words.hashCode()
        hash = 31 * hash + controlGlyphs.hashCode()
        return 31 * hash + markerGlyphs.hashCode()
    }

    override fun toString(): String =
        "ElementsScopeStatement(language=$language, locale=$locale, words=$words, " +
            "controlGlyphs=${controlGlyphs?.name}, markerGlyphs=${markerGlyphs?.name})"
}
