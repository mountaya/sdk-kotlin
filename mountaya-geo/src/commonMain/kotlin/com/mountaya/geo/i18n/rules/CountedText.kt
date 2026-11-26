package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * countedText writes the counted word [word] for [count] (`geo.i18n.counted-words`): the form its language
 * names for the count, with the count in place of its `{count}` placeholder.
 *
 * A counted word is what the elements' words table writes for a word that counts, an object of the forms
 * CLDR's plural rules name, `{"one": "{count} result", "other": "{count} results"}`. The form is the
 * language's: in French the `one` form for zero and one, and in English, as in every language the table does
 * not hold, the `one` form for one; any other count, and a count whose form the word does not state, takes the
 * `other` form.
 *
 * The count is written by the platform's locale services in [language], its digits and its grouping that
 * language's, never digits assembled by hand: 1234 is `1,234` in English and `1 234` in French, grouped by a
 * narrow no-break space, U+202F (`formatNumber`). The first `{count}` a form holds takes it, and a form that
 * holds none is written as it is.
 *
 * Null where [word] is no counted word, an object holding no `other` form that is text: no rule of the geo
 * layer throws, so the refusal is an answer.
 */
public fun countedText(
    word: JsonElement,
    count: Int,
    language: RestSchema.Language,
): String? {
    val forms = word as? JsonObject ?: return null
    val other = textOf(forms["other"]) ?: return null
    val isOne = if (language == RestSchema.Language.Fr) count == 0 || count == 1 else count == 1
    val form = if (isOne) textOf(forms["one"]) ?: other else other
    val placeholder = firstPlaceholder(form, "count") ?: return form
    val written = formatNumber(count.toDouble(), language.value, maximumFractionDigits = 0)
    return form.replaceRange(placeholder, written)
}

/** The text [element] is, or null where it is no JSON string. */
private fun textOf(element: JsonElement?): String? = (element as? JsonPrimitive)?.takeIf { it.isString }?.content
