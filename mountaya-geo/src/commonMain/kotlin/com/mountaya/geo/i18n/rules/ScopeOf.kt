package com.mountaya.geo

import com.mountaya.client.InternalMountayaApi

/**
 * scopeOf is the scope nested in [outer] that states [statement] (`geo.i18n.nested-scope`).
 *
 * **A setting stated is the scope's own, and one left out is the scope around it's.** The rule reads the
 * same at every depth, which is what lets a surface nest a scope of its own under its host's and keep the
 * host's language, words and glyph sets.
 *
 * - **The words** are the scope around it's while the statement states no words and no language other than
 *   the one around it, a language restating the one around it included. Otherwise they resolve afresh from
 *   the language's base in the elements' own words, with the words the statement states folded on, so words
 *   the scope around it states stay with that scope: an override written for one language is that
 *   language's.
 * - **The locale** follows a language other than the one around it, read as its own tag, unless the
 *   statement states a locale: a regional convention inherited across a change of language is one nobody
 *   chose. A statement of the language around it keeps the locale it inherits.
 * - **The glyph sets** are inherited one by one, and a control set stated never stands in for the marker
 *   set, since a control set holds no glyph for a place and would draw every one as the same pin.
 * - **A composable's own strings** around the statement are kept where its words are the scope around it's, and
 *   left with that scope where they resolve afresh, as the words around it are.
 */
@OptIn(InternalMountayaApi::class)
public fun scopeOf(
    outer: ElementsScope,
    statement: ElementsScopeStatement,
): ElementsScope {
    val language = statement.language ?: outer.language
    val changesLanguage = language != outer.language
    val inheritsWords = statement.words == null && !changesLanguage
    return ElementsScope(
        language = language,
        locale = statement.locale ?: if (changesLanguage) language.value else outer.locale,
        words = if (inheritsWords) outer.words else resolveBundle(WordBundle.elements, language, statement.words),
        controlGlyphs = statement.controlGlyphs ?: outer.controlGlyphs,
        markerGlyphs = statement.markerGlyphs ?: outer.markerGlyphs,
        strings = if (inheritsWords) outer.strings else emptyMap(),
    )
}
