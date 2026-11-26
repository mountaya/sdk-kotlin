package com.mountaya.conformance

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.selectBoundary
import kotlinx.coroutines.test.TestScope

/**
 * `geo.boundaries.select`: the name of the boundary a map centred on the case's `center` locks to,
 * of its `boundaries` in order, each built as the generated `RestSchema.Boundaries` the API answers,
 * or null where there is none to lock to.
 */
object GeoBoundariesSelectRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val boundaries =
            input.items("boundaries").map { item ->
                val boundary = item as? FixtureValue.Object ?: throw AssertionError("a boundary is not an object")
                RestSchema.Boundaries(bbox = fixturePosition(boundary["bbox"], "bbox"), name = boundary.text("name"))
            }
        val lock = selectBoundary(boundaries, fixturePosition(input["center"], "center"))
        return ConformanceOutcome.Value(lock?.let { FixtureValue.Text(it.name) } ?: FixtureValue.Null)
    }
}
