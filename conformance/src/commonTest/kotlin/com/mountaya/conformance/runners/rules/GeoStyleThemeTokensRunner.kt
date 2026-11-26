package com.mountaya.conformance

import com.mountaya.geo.ThemeScheme
import com.mountaya.geo.generated.ElementTokens
import kotlinx.coroutines.test.TestScope

/**
 * `geo.style.theme-tokens`: the value the case's `token` reads under its `scheme`, with the values its `overrides`
 * state set on a copy of the standard tokens as a consumer sets them: each token whole, a token of its value alone,
 * which stands in both schemes. A token is reached by its member, the runner's table of the contract's names, which
 * the jvm suite holds to the contract's table.
 */
object GeoStyleThemeTokensRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val written = input.text("scheme")
        val scheme =
            ThemeScheme.entries.firstOrNull { it.id == written }
                ?: throw AssertionError("the case's `scheme` is neither light nor dark")
        val overrides =
            input.objectOrNull("overrides") ?: throw AssertionError("the case's `overrides` is not an object")
        val tokens =
            ElementTokens.standard.copy {
                for ((name, value) in overrides.entries) GeoControlFixtures.state(this, name, value)
            }
        return ConformanceOutcome.Value(GeoControlFixtures.value(input.text("token"), tokens, scheme))
    }
}
