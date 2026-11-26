package com.mountaya.conformance

import com.mountaya.geo.STEEPNESS_OVERLAY_SLUG
import com.mountaya.geo.SlopeRampStop
import com.mountaya.geo.SteepnessClass
import com.mountaya.geo.ascentSlopeRamp
import com.mountaya.geo.classifySteepness
import com.mountaya.geo.descentSlopeRamp
import com.mountaya.geo.slopeRampColor
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.slope-ramp`: the colour a slope of the case's `degrees` paints, and its steepness
 * class, or null. The two ramps, the overlay's slug and the class thresholds are held to the ones
 * Geo publishes.
 */
object GeoProfileSlopeRampRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            rules = emptyMap(),
            values =
                mapOf(
                    "ascentRampStops" to stops(ascentSlopeRamp),
                    "descentRampStops" to stops(descentSlopeRamp),
                    "steepnessOverlaySlug" to FixtureValue.Text(STEEPNESS_OVERLAY_SLUG),
                    "steepnessThresholds" to
                        FixtureValue.Array(SteepnessClass.entries.map { FixtureValue.Number(it.fromDegrees) }),
                ),
        )
        val degrees = case.fields.number("degrees")
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "color" to FixtureValue.Text(slopeRampColor(degrees)),
                    "steepness" to textOrNull(classifySteepness(degrees)?.id),
                ),
            ),
        )
    }

    private fun stops(ramp: List<SlopeRampStop>) =
        FixtureValue.Array(
            ramp.map {
                FixtureValue.Object(
                    listOf(
                        "color" to FixtureValue.Text(it.color),
                        "degrees" to FixtureValue.Number(it.degrees),
                    ),
                )
            },
        )
}
