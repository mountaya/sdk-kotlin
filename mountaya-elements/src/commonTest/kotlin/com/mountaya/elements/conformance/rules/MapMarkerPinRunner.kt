package com.mountaya.elements

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import com.mountaya.geo.GlyphSet
import kotlinx.coroutines.test.TestScope

/**
 * `map.marker.pin`: the pin a marker given no view of its own is drawn as, through the internal rule the
 * renderer draws, over a glyph set holding the case's `heldGlyphs` and no other. The pin's sizes and colours are
 * held to the rule's own; the held glyphs are the input every case reads.
 */
object MapMarkerPinRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        MapRunnerFixtures.holdConstants(
            case,
            mapOf(
                "diameter" to FixtureValue.Number(MARKER_PIN_DIAMETER),
                "dotSize" to FixtureValue.Number(MARKER_PIN_DOT_SIZE),
                "glyphSize" to FixtureValue.Number(MARKER_PIN_GLYPH_SIZE),
                "markColor" to FixtureValue.Text(MARKER_PIN_MARK_COLOR),
                "ringWidth" to FixtureValue.Number(MARKER_PIN_RING_WIDTH),
            ),
            inputs = setOf("heldGlyphs"),
        )
        val held = MapRunnerFixtures.textsOf(case.root("constants").items("heldGlyphs"))
        val set =
            GlyphSet.of("held", GlyphSet.Paint.Stroke, markup = held.associateWith { "<path d=\"M0 0h1\"/>" })
                ?: throw AssertionError("the held glyphs make no set")
        val input = case.fields
        val pin = describeMarkerPin(input.textOrNull("color"), input.textOrNull("glyph"), set)
        val centre =
            when (val drawn = pin.centre) {
                is MarkerPin.Centre.Glyph -> {
                    FixtureValue.Object(
                        listOf(
                            "color" to FixtureValue.Text(drawn.color),
                            "glyph" to FixtureValue.Text(drawn.glyph),
                            "size" to FixtureValue.Number(drawn.size),
                        ),
                    )
                }

                is MarkerPin.Centre.Dot -> {
                    FixtureValue.Object(
                        listOf(
                            "color" to FixtureValue.Text(drawn.color),
                            "dot" to FixtureValue.Number(drawn.size),
                        ),
                    )
                }
            }
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "centre" to centre,
                    "diameter" to FixtureValue.Number(pin.diameter),
                    "fill" to (pin.fill?.let { FixtureValue.Text(it) } ?: FixtureValue.Null),
                    "ring" to
                        FixtureValue.Object(
                            listOf(
                                "color" to FixtureValue.Text(pin.ringColor),
                                "width" to FixtureValue.Number(pin.ringWidth),
                            ),
                        ),
                ),
            ),
        )
    }
}
