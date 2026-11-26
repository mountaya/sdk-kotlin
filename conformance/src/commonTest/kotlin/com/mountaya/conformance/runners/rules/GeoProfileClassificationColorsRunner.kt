package com.mountaya.conformance

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.surfaceColor
import com.mountaya.geo.surfaceColors
import com.mountaya.geo.unclassifiedColor
import com.mountaya.geo.wayTypeColor
import com.mountaya.geo.wayTypeColors
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.classification-colors`: the colour a stretch of the case's `kind`, `surface` or `way-type`, and
 * `value` paints in. Both tables and the unclassified grey are held to the published ones.
 */
object GeoProfileClassificationColorsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            rules = emptyMap(),
            values =
                mapOf(
                    "surfaces" to
                        FixtureValue.Object(
                            surfaceColors.map { (surface, color) ->
                                surface.value to
                                    FixtureValue.Text(color.hex)
                            },
                        ),
                    "unclassified" to FixtureValue.Text(unclassifiedColor.hex),
                    "wayTypes" to
                        FixtureValue.Object(
                            wayTypeColors.map { (wayType, color) ->
                                wayType.value to
                                    FixtureValue.Text(color.hex)
                            },
                        ),
                ),
        )
        val input = case.fields
        val value = input.text("value")
        val color =
            when (val kind = input.text("kind")) {
                "surface" -> surfaceColor(RestSchema.SurfaceType(value))
                "way-type" -> wayTypeColor(RestSchema.WayType(value))
                else -> throw AssertionError("`$kind` is no classification")
            }
        return ConformanceOutcome.Value(FixtureValue.Text(color.hex))
    }
}
