package com.mountaya.conformance

import com.mountaya.geo.MIN_DOMAIN_SPAN_METERS
import com.mountaya.geo.ProfileAnchor
import com.mountaya.geo.anchoredDomain
import com.mountaya.geo.domainRoundingSteps
import com.mountaya.geo.domainTicks
import com.mountaya.geo.emptyElevationDomain
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.domain`: the domain the case's `series` is drawn against under its `anchor`, `route` where it
 * states none, over its `window`, and the ticks its scale is labelled at. The empty domain, the narrowest span
 * and the rounding steps are held to the published ones.
 */
object GeoProfileDomainRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            mapOf("minSpanMeters" to MIN_DOMAIN_SPAN_METERS),
            values =
                mapOf(
                    "emptyDomain" to domainValue(emptyElevationDomain),
                    "roundingSteps" to
                        FixtureValue.Array(
                            domainRoundingSteps.map {
                                objectValue(
                                    "stepMeters" to FixtureValue.Number(it.stepMeters),
                                    "uptoSpanMeters" to numberOrNull(it.uptoSpanMeters),
                                )
                            },
                        ),
                ),
        )
        val input = case.fields
        val name = input.textOrNull("anchor") ?: ProfileAnchor.Route.id
        val anchor = ProfileAnchor.entries.firstOrNull { it.id == name } ?: throw AssertionError("`$name` is no anchor")
        val domain = anchoredDomain(fixtureSeries(input["series"]), anchor, fixtureWindow(input["window"]))
        return ConformanceOutcome.Value(
            objectValue(
                "max" to FixtureValue.Number(domain.max),
                "min" to FixtureValue.Number(domain.min),
                "ticks" to numbersValue(domainTicks(domain)),
            ),
        )
    }
}
