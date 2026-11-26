package com.mountaya.conformance

import com.mountaya.geo.labelLanguage
import com.mountaya.geo.labelLanguages
import kotlinx.coroutines.test.TestScope

/**
 * `geo.style.label-language`: the language a map labels in for the case's `tag`, or for no tag. The
 * fallback is held to the language no tag reads, and the languages to the ones Geo labels in.
 */
object GeoStyleLabelLanguageRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            rules = emptyMap(),
            values =
                mapOf(
                    "fallback" to FixtureValue.Text(labelLanguage(null).value),
                    "languages" to textArray(labelLanguages.map { it.value }),
                ),
        )
        return ConformanceOutcome.Value(FixtureValue.Text(labelLanguage(case.fields.textOrNull("tag")).value))
    }
}
