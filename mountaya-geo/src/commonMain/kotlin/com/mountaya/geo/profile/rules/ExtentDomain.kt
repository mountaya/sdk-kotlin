package com.mountaya.geo

import kotlin.math.ceil
import kotlin.math.floor

/**
 * MIN_DOMAIN_SPAN_METERS is the narrowest elevation range a profile is drawn against, the `minSpanMeters`
 * `geo/profile-domain.json` declares: without it an itinerary that climbs four meters over ten kilometers is
 * drawn as a mountain, its curve filling the plot with its own noise.
 */
public const val MIN_DOMAIN_SPAN_METERS: Double = 50.0

/**
 * emptyElevationDomain is the range a profile with no elevation is drawn against, 0 to 50 m, the
 * `emptyDomain` `geo/profile-domain.json` declares, so a frame drawn before the data arrives is the frame it
 * keeps.
 */
public val emptyElevationDomain: ElevationDomain = ElevationDomain(0.0, MIN_DOMAIN_SPAN_METERS)

/**
 * domainRoundingSteps is how coarsely a profile's elevation domain is rounded outward by its span, the
 * `roundingSteps` `geo/profile-domain.json` declares: by 10 m where the widened span is at most 200 m, by
 * 50 m where it is at most 1,000 m, and by 100 m past that, so an alpine profile reads against hundred-meter
 * lines and a coastal one against ten-meter lines.
 */
public val domainRoundingSteps: List<DomainRoundingStep> =
    listOf(
        DomainRoundingStep(10.0, 200.0),
        DomainRoundingStep(50.0, 1_000.0),
        DomainRoundingStep(100.0, null),
    )

/**
 * extentDomain is the elevation range a stated extent is drawn against (`geo.profile.domain`): widened to at
 * least [MIN_DOMAIN_SPAN_METERS] about its middle and rounded outward, its floor down and its ceiling up, to
 * a multiple of the step [domainRoundingSteps] gives its widened span, so it is always wider than a point.
 * An extent with either bound missing is [emptyElevationDomain].
 */
public fun extentDomain(
    min: Double?,
    max: Double?,
): ElevationDomain {
    if (min == null || max == null) return emptyElevationDomain
    val measured = max - min
    val padding = maxOf(0.0, MIN_DOMAIN_SPAN_METERS - measured) / 2.0
    val step = roundingStep(maxOf(measured, MIN_DOMAIN_SPAN_METERS))
    val floor = floor((min - padding) / step) * step
    val ceiling = ceil((max + padding) / step) * step
    return ElevationDomain(floor, if (ceiling > floor) ceiling else floor + step)
}

/** The step a widened span is rounded by: the first of [domainRoundingSteps] whose ceiling it does not pass, and the last for any other. */
private fun roundingStep(span: Double): Double {
    for (step in domainRoundingSteps) {
        val upto = step.uptoSpanMeters ?: return step.stepMeters
        if (span <= upto) return step.stepMeters
    }
    return domainRoundingSteps.lastOrNull()?.stepMeters ?: MIN_DOMAIN_SPAN_METERS
}
