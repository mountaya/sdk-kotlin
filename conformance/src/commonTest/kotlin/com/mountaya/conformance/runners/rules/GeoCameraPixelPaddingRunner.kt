package com.mountaya.conformance

import com.mountaya.geo.PixelPadding
import com.mountaya.geo.WEB_MERCATOR_MAX_LATITUDE
import com.mountaya.geo.WEB_MERCATOR_TILE_SIZE_PIXELS
import com.mountaya.geo.boundsWithPixelPadding
import com.mountaya.geo.groundMetersPerPixel
import kotlinx.coroutines.test.TestScope

/**
 * `geo.camera.pixel-padding`: the case's `bounds` grown by its `padding`, one number for every edge
 * or one per edge, at its `zoom` and `centerLatitude`. The tile size and the Web Mercator limit are
 * held to the published constants, and the circumference to the ground one pixel spans at zoom 0
 * on the equator times the tiles across it.
 */
object GeoCameraPixelPaddingRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            mapOf(
                "tileSizePx" to WEB_MERCATOR_TILE_SIZE_PIXELS,
                "webMercatorCircumferenceMeters" to groundMetersPerPixel(0.0, 0.0) * WEB_MERCATOR_TILE_SIZE_PIXELS,
                "webMercatorMaxLatitude" to WEB_MERCATOR_MAX_LATITUDE,
            ),
        )
        val input = case.fields
        val padding =
            when (val written = input["padding"]) {
                is FixtureValue.Number -> {
                    PixelPadding(written.value)
                }

                is FixtureValue.Object -> {
                    PixelPadding(
                        top = written.number("top"),
                        right = written.number("right"),
                        bottom = written.number("bottom"),
                        left = written.number("left"),
                    )
                }

                else -> {
                    throw AssertionError("`padding` is neither a number nor one per edge")
                }
            }
        val grown =
            boundsWithPixelPadding(
                fixturePosition(input["bounds"], "bounds"),
                padding,
                input.number("zoom"),
                input.number("centerLatitude"),
            )
        return ConformanceOutcome.Value(FixtureValue.Array(grown.map { FixtureValue.Number(it) }))
    }
}
