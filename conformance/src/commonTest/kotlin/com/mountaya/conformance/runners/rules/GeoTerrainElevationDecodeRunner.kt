package com.mountaya.conformance

import com.mountaya.geo.ElevationEncoding
import com.mountaya.geo.decodeElevation
import kotlinx.coroutines.test.TestScope

/**
 * `geo.terrain.elevation-decode`: the height the case's `pixel`, its red, green and blue, stands for under its
 * `encoding`, or none where it names none. The encodings are held to the two `ElevationEncoding` names, and the default
 * to the one a pixel read under no encoding reads as.
 */
object GeoTerrainElevationDecodeRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val probe = elevationPixel(listOf(1, 134, 160))
        val default =
            listOf(ElevationEncoding.Mapbox, ElevationEncoding.Terrarium).first {
                decodeElevation(probe, it) == decodeElevation(probe, null)
            }
        holdDeclaredConstants(
            case,
            rules = emptyMap(),
            values =
                mapOf(
                    "defaultEncoding" to FixtureValue.Text(default.value),
                    "encodings" to textArray(listOf(ElevationEncoding.Mapbox.value, ElevationEncoding.Terrarium.value)),
                ),
        )
        val input = case.fields
        val pixel = elevationPixel(input.items("pixel").map { (it as FixtureValue.Number).value.toInt() })
        val height = decodeElevation(pixel, input.textOrNull("encoding")?.let(::ElevationEncoding))
        return ConformanceOutcome.Value(height?.let { FixtureValue.Number(it) } ?: FixtureValue.Null)
    }
}

/** A pixel's red, green and blue, each from 0 to 255, packed as `decodeElevation` reads one. */
internal fun elevationPixel(channels: List<Int>): Int {
    if (channels.size != 3) throw AssertionError("a pixel is no red, green and blue: $channels")
    return (channels[0] shl 16) or (channels[1] shl 8) or channels[2]
}
