package com.mountaya.conformance

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.WordBundle
import com.mountaya.geo.countedText
import com.mountaya.geo.filledText
import com.mountaya.geo.resolveBundle
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * `geo.i18n.counted-words`: the text the case's `word` of the elements' words, resolved for its `language`, takes:
 * a counted word for its `count`, or a template filled with its `value`, or with each of its `values` by the name of
 * its placeholder, the case's own `template` read in
 * place of the table's text where it states one, and its own `forms` in place of the table's counted word
 * where it states them, as a host's own words write them.
 */
object GeoI18nCountedWordsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val language = RestSchema.Language(input.text("language"))
        val path = input.text("word")
        val forms = input.objectOrNull("forms")
        val word =
            if (forms != null) {
                // The forms a host's words hold for a counted word, read in place of the table's.
                JsonObject(
                    forms.entries.associate { (form, text) ->
                        val written = (text as? FixtureValue.Text)?.value ?: throw AssertionError("`$form` is no text")
                        form to JsonPrimitive(written)
                    },
                )
            } else {
                GeoControlFixtures.word(path, resolveBundle(WordBundle.elements, language))
                    ?: throw AssertionError("the words hold nothing at `$path`")
            }
        val count = wholeOrNull(input, "count")
        if (count != null) {
            val text = countedText(word, count, language) ?: throw AssertionError("`$path` is no counted word")
            return ConformanceOutcome.Value(FixtureValue.Text(text))
        }
        val template =
            input.textOrNull("template")
                ?: (word as? JsonPrimitive)?.takeIf { it.isString }?.content
                ?: throw AssertionError("`$path` is no template")
        val values = input.objectOrNull("values")
        if (values != null) {
            val named =
                values.entries.associate { (name, value) ->
                    name to ((value as? FixtureValue.Text)?.value ?: throw AssertionError("`values.$name` is no text"))
                }
            return ConformanceOutcome.Value(FixtureValue.Text(filledText(template, named)))
        }
        return ConformanceOutcome.Value(FixtureValue.Text(filledText(template, input.text("value"))))
    }
}
