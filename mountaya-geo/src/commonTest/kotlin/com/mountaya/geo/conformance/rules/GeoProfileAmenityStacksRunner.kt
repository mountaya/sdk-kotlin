package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.amenity-stacks`: the stacks above the case's pinned `places`, each a pin of its key whose place
 * offers its `amenities`, capped at its `max` where it states one. The default maximum is held to the published
 * one.
 */
object GeoProfileAmenityStacksRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdConstantsExactly(case, mapOf("defaultMax" to FixtureValue.Number(DEFAULT_AMENITY_STACK_MAX)))
        val input = case.fields
        val pins =
            input.items("places").map { item ->
                val place = item as? FixtureValue.Object ?: throw AssertionError("a place is not an object")
                val key = place.text("key")
                val amenities = profileOptionalTexts(place["amenities"]).map { RestSchema.WaypointAmenity(it) }
                CurvePin(
                    key,
                    ProfilePlace(
                        ProfileWaypoint.AtDistance(key, 0.0),
                        details = WaypointDetails(amenities = amenities),
                    ),
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                )
            }
        val max = input.numberOrNull("max")
        val stacks = if (max != null) amenityStacks(pins, max) else amenityStacks(pins)
        return ConformanceOutcome.Value(
            FixtureValue.Array(
                stacks.map {
                    profileObject(
                        "counted" to FixtureValue.Number(it.counted.toDouble()),
                        "key" to FixtureValue.Text(it.pin.key),
                        "named" to profileTexts(it.named.map { service -> service.value }),
                        "shown" to profileTexts(it.shown.map { service -> service.value }),
                    )
                },
            ),
        )
    }
}
