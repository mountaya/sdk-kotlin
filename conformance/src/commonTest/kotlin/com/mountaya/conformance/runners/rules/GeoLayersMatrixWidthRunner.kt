package com.mountaya.conformance

import com.mountaya.geo.MATRIX_LINE_MAX_WIDTH
import com.mountaya.geo.MATRIX_LINE_MIN_WIDTH
import com.mountaya.geo.matrixLineWidth
import kotlinx.coroutines.test.TestScope

/**
 * `geo.layers.matrix-width`: the width a connector of the case's cost `value` draws at in a grid from
 * `min` to `max`, each a number JSON may not carry decoded where it stands. The two widths are held to the
 * ones Geo publishes.
 */
object GeoLayersMatrixWidthRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(case, mapOf("maxWidth" to MATRIX_LINE_MAX_WIDTH, "minWidth" to MATRIX_LINE_MIN_WIDTH))
        val input = case.fields
        return ConformanceOutcome.Value(
            FixtureValue.Number(matrixLineWidth(input.number("value"), input.number("min"), input.number("max"))),
        )
    }
}
