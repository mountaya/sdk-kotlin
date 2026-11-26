package com.mountaya.conformance

import com.mountaya.geo.ZoomBounds
import com.mountaya.geo.zoomIntoRange
import kotlinx.coroutines.test.TestScope

/**
 * `geo.camera.zoom-into-range`: the zoom a camera at the case's `zoom` moves to so that its `window`,
 * `{ maxzoom, minzoom }` as a style's layer writes it, paints, or null where it stays. The window is read
 * as the `ZoomBounds` `paintZoomRange` answers, a number JSON cannot carry kept as the fixture decodes it.
 */
object GeoCameraZoomIntoRangeRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val window = input.objectOrNull("window") ?: throw AssertionError("the input states no `window`")
        val moved = zoomIntoRange(input.number("zoom"), ZoomBounds(window.number("minzoom"), window.number("maxzoom")))
        return ConformanceOutcome.Value(moved?.let { FixtureValue.Number(it) } ?: FixtureValue.Null)
    }
}
