package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import com.mountaya.client.generated.TileSchema

/** The member of a language's strings that holds an entry's display name. */
private const val NAME_KEY = "name"

/**
 * localizedName answers the display name of a catalogue entry (an activity, a category, an overlay,
 * a terrain) whose names per language are [locales] and whose identifier is [slug], for a reader of
 * [language]: its name in that language, else its English name, else its slug.
 *
 * The language is read as a label language is, by its primary subtag ([labelLanguage]), so `fr-CA`
 * and `FR` read French and a language the catalogue is not keyed by reads English. A name that is the
 * empty string falls through as a missing one does, since the wire's name may carry one; a name of
 * white space is a name. The slug is the last answer, so a picker in a language nobody translated the
 * catalogue into still says what each entry is.
 */
public fun localizedName(
    locales: RestSchema.Locales,
    slug: String,
    language: String?,
): String =
    firstNamed(
        localeMapOf(locales, labelLanguage(language))?.get(NAME_KEY),
        locales.en?.get(NAME_KEY),
    ) ?: slug

/**
 * localizedName answers the display name of an entry of the Tile API's layers manifest, whose names
 * per language are [locales], as the REST API's entries are named: its name in [language], else in
 * English, else [slug].
 */
public fun localizedName(
    locales: TileSchema.Locales,
    slug: String,
    language: String?,
): String =
    firstNamed(
        tileLocaleMapOf(locales, labelLanguage(language))?.get(NAME_KEY),
        locales.en?.get(NAME_KEY),
    ) ?: slug

/** The first of [names] that is a name, neither missing nor empty. */
private fun firstNamed(vararg names: String?): String? = names.firstOrNull { !it.isNullOrEmpty() }

/** The strings [locales] holds for [language], one of the seven it is keyed by. */
private fun localeMapOf(
    locales: RestSchema.Locales,
    language: RestSchema.Language,
): RestSchema.LocaleMap? =
    when (language) {
        RestSchema.Language.De -> locales.de
        RestSchema.Language.Es -> locales.es
        RestSchema.Language.Fr -> locales.fr
        RestSchema.Language.It -> locales.it
        RestSchema.Language.Ja -> locales.ja
        RestSchema.Language.Zh -> locales.zh
        else -> locales.en
    }

/** The strings the Tile API's [locales] holds for [language], one of the seven it is keyed by. */
private fun tileLocaleMapOf(
    locales: TileSchema.Locales,
    language: RestSchema.Language,
): TileSchema.LocaleMap? =
    when (language) {
        RestSchema.Language.De -> locales.de
        RestSchema.Language.Es -> locales.es
        RestSchema.Language.Fr -> locales.fr
        RestSchema.Language.It -> locales.it
        RestSchema.Language.Ja -> locales.ja
        RestSchema.Language.Zh -> locales.zh
        else -> locales.en
    }
