package com.mountaya.conformance

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.relabelTextField
import kotlinx.coroutines.test.TestScope

/**
 * `geo.style.label-relabel`: the `text-field` a symbol layer whose label is the case's `textField`
 * takes to label in its `language`, as JSON writes it, or null where the layer is left as it is.
 */
object GeoStyleLabelRelabelRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val written =
            relabelTextField(input["textField"]?.let(::fixtureJson), RestSchema.Language(input.text("language")))
        return ConformanceOutcome.Value(written?.let(::fixtureValueOf) ?: FixtureValue.Null)
    }
}
