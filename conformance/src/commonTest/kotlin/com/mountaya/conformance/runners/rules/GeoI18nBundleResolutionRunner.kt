package com.mountaya.conformance

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.WordBundle
import com.mountaya.geo.resolveBundle
import com.mountaya.geo.wordOverridesOf
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.JsonObject

/**
 * `geo.i18n.bundle-resolution`: the words the case's `bundle` resolves to in its `language`, English
 * where it names none, with its `overrides` and the `translations` a host handed over. The bundle's
 * `base` is its English and the translations it carries; the overrides are read as JSON writes them,
 * so an override naming a language and a section together is the refusal, answered as `refused`.
 */
object GeoI18nBundleResolutionRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val bundle = input.objectOrNull("bundle") ?: throw AssertionError("the case has no `bundle`")
        val base = languages(bundle.objectOrNull("base"))
        val english =
            base[RestSchema.Language.En] ?: throw AssertionError("the bundle's base carries no English")
        val overrides =
            input.objectOrNull("overrides")?.let { written ->
                wordOverridesOf(fixtureJson(written) as JsonObject)
                    ?: return ConformanceOutcome.Value(FixtureValue.Text("refused"))
            }
        val resolved =
            resolveBundle(
                WordBundle(english, base - RestSchema.Language.En, languages(bundle.objectOrNull("partial"))),
                input.textOrNull("language")?.let { RestSchema.Language(it) } ?: RestSchema.Language.En,
                overrides,
                languages(input.objectOrNull("translations")),
            )
        return ConformanceOutcome.Value(fixtureValueOf(resolved))
    }

    /** The words per language [record] writes, keyed by the wire's spelling of each language. */
    private fun languages(record: FixtureValue.Object?): Map<RestSchema.Language, JsonObject> =
        record?.entries.orEmpty().associate { (language, words) ->
            RestSchema.Language(language) to
                (fixtureJson(words) as? JsonObject ?: throw AssertionError("`$language` holds no words"))
        }
}
