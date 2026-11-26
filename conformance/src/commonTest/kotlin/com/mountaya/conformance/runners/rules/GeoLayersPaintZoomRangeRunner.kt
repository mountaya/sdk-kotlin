package com.mountaya.conformance

import com.mountaya.geo.paintZoomRange
import kotlinx.coroutines.test.TestScope

/**
 * `geo.layers.paint-zoom-range`: the window the case's `sources` paint across, as `minzoom` and
 * `maxzoom`, or null. Each source and layer is built as the Tile API's, with the zooms the fixture
 * writes and a placeholder in every member it leaves out.
 */
object GeoLayersPaintZoomRangeRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val sources =
            case.fields.items("sources").map {
                fixtureTileSource(it as? FixtureValue.Object ?: throw AssertionError("a source is not an object"))
            }
        val window = paintZoomRange(sources) ?: return ConformanceOutcome.Value(FixtureValue.Null)
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "maxzoom" to FixtureValue.Number(window.maxZoom),
                    "minzoom" to FixtureValue.Number(window.minZoom),
                ),
            ),
        )
    }
}
