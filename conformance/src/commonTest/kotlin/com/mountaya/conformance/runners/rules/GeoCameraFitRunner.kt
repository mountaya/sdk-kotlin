package com.mountaya.conformance

import com.mountaya.geo.PixelPadding
import com.mountaya.geo.WEB_MERCATOR_TILE_SIZE_PIXELS
import com.mountaya.geo.ZoomBounds
import com.mountaya.geo.cameraForBounds
import kotlinx.coroutines.test.TestScope

/**
 * `geo.camera.fit`: the viewport that fits the case's `bounds` in its `canvas`, `[width, height]`, less its
 * `padding`, one number for every edge or one per edge, none where it is left out, under its `maxZoom`, the
 * window's where it is left out, and within its zoom `window`, `{ max, min }`. The tile the world is drawn
 * on is held to the one Geo publishes.
 */
object GeoCameraFitRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(case, rules = mapOf("tileSizePx" to WEB_MERCATOR_TILE_SIZE_PIXELS))
        val input = case.fields
        val window = input.objectOrNull("window") ?: throw AssertionError("the input states no `window`")
        val camera =
            cameraForBounds(
                bounds = fixtureNumbers(input.items("bounds"), "`bounds`"),
                canvas = fixtureNumbers(input.items("canvas"), "`canvas`"),
                window = ZoomBounds(window.number("min"), window.number("max")),
                padding = padding(input["padding"]),
                maxZoom = input.numberOrNull("maxZoom"),
            ) ?: return ConformanceOutcome.Value(FixtureValue.Null)
        // A viewport faces north by its type, so the bearing every fit lands at is written as 0.
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "bearing" to FixtureValue.Number(0.0),
                    "center" to FixtureValue.Array(camera.center.map { FixtureValue.Number(it) }),
                    "zoom" to FixtureValue.Number(camera.zoom),
                ),
            ),
        )
    }

    /** The padding the fixture writes: one number for every edge, one per edge, or none. */
    private fun padding(written: FixtureValue?): PixelPadding =
        when (written) {
            null, is FixtureValue.Null -> {
                PixelPadding(0.0)
            }

            is FixtureValue.Number -> {
                PixelPadding(written.value)
            }

            is FixtureValue.Object -> {
                PixelPadding(
                    written.number("top"),
                    written.number("right"),
                    written.number("bottom"),
                    written.number("left"),
                )
            }

            else -> {
                throw AssertionError("`padding` is neither a number nor one per edge")
            }
        }

    /** The numbers [items] holds, failing with a sentence on any other value. */
    private fun fixtureNumbers(
        items: List<FixtureValue>,
        what: String,
    ): List<Double> =
        items.map {
            (it as? FixtureValue.Number)?.value
                ?: throw AssertionError("$what holds a value that is no number")
        }
}
