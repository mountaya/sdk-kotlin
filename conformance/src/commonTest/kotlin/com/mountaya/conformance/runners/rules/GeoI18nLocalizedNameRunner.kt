package com.mountaya.conformance

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.localizedName
import kotlinx.coroutines.test.TestScope

/**
 * `geo.i18n.localized-name`: the display name of the case's catalogue `entry` in its `language`. The
 * entry's `locales` are built as the REST API's, each language's strings as the fixture writes them.
 */
object GeoI18nLocalizedNameRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val entry = case.fields.objectOrNull("entry") ?: throw AssertionError("the case has no `entry`")
        val written = entry.objectOrNull("locales") ?: throw AssertionError("the entry has no `locales`")

        fun strings(language: String): RestSchema.LocaleMap? =
            written.objectOrNull(language)?.let { names ->
                names.entries.associate { (key, value) ->
                    key to
                        (value as? FixtureValue.Text)?.value
                }
            }

        val locales =
            RestSchema.Locales(
                de = strings("de"),
                en = strings("en"),
                es = strings("es"),
                fr = strings("fr"),
                it = strings("it"),
                ja = strings("ja"),
                zh = strings("zh"),
            )
        return ConformanceOutcome.Value(
            FixtureValue.Text(localizedName(locales, entry.text("slug"), case.fields.textOrNull("language"))),
        )
    }
}
