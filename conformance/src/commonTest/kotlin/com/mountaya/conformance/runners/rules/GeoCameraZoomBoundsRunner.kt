package com.mountaya.conformance

import com.mountaya.geo.WEB_MERCATOR_MAX_LATITUDE
import com.mountaya.geo.effectiveZoomBounds
import kotlinx.coroutines.test.TestScope

/**
 * `geo.camera.zoom-bounds`: the zoom range a map is held to for the case's `zoomMin` and `zoomMax`.
 *
 * The floor, the buffer and the least span are held to what the rule answers: the minimum of a
 * range that reaches zoom 0, the pull a range's maximum takes, and the span a range of one zoom is
 * widened to. The Web Mercator limit is held to the published constant.
 */
object GeoCameraZoomBoundsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val narrow = effectiveZoomBounds(22.0, 22.0)
        holdDeclaredConstants(
            case,
            mapOf(
                "effectiveMinZoomFloor" to effectiveZoomBounds(0.0, 18.0).minZoom,
                "effectiveZoomMinSpan" to narrow.maxZoom - narrow.minZoom,
                "webMercatorMaxLatitude" to WEB_MERCATOR_MAX_LATITUDE,
                "zoomBuffer" to 18.0 - effectiveZoomBounds(6.0, 18.0).maxZoom,
            ),
        )
        val input = case.fields
        val bounds = effectiveZoomBounds(input.number("zoomMin"), input.number("zoomMax"))
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "maxZoom" to FixtureValue.Number(bounds.maxZoom),
                    "minZoom" to FixtureValue.Number(bounds.minZoom),
                ),
            ),
        )
    }
}
