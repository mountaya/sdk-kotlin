package com.mountaya.conformance

import com.mountaya.geo.DEFAULT_SEGMENT_COLOR
import com.mountaya.geo.assignDistinctColors
import com.mountaya.geo.livelyPalette
import com.mountaya.geo.segmentColorAt
import kotlinx.coroutines.test.TestScope

/**
 * `geo.style.segment-colors`: the colour at the case's `index`, over its own `palette` where it names
 * one, or the `count` colours a batch with the case's `reserved` ones takes, over its own `palette` too
 * where it names one. A whole index within an
 * `Int` reads through the whole-position overload and any other number through the one that takes
 * any number. The default colour and the lively palette are held to the ones Geo publishes.
 */
object GeoStyleSegmentColorsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            rules = emptyMap(),
            values =
                mapOf(
                    "defaultSegmentColor" to FixtureValue.Text(DEFAULT_SEGMENT_COLOR),
                    "livelyPalette" to textArray(livelyPalette),
                ),
        )
        val input = case.fields
        val palette = input["palette"]?.let { fixtureTexts((it as FixtureValue.Array).items, "`palette`") }
        val count = wholeOrNull(input, "count")
        if (count != null) {
            val reserved = fixtureTexts(input.items("reserved"), "`reserved`")
            val colors =
                if (palette != null) {
                    assignDistinctColors(count, reserved, palette)
                } else {
                    assignDistinctColors(count, reserved)
                }
            return ConformanceOutcome.Value(textArray(colors))
        }
        val index = input.number("index")
        val whole = index.isFinite() && index == index.toInt().toDouble()
        val color =
            when {
                whole && palette != null -> segmentColorAt(index.toInt(), palette)
                whole -> segmentColorAt(index.toInt())
                palette != null -> segmentColorAt(index, palette)
                else -> segmentColorAt(index)
            }
        return ConformanceOutcome.Value(FixtureValue.Text(color))
    }
}
