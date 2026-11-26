package com.mountaya.geo

/** Where an elevation stands between the floor and the ceiling of a domain with no span: half way. */
private const val NO_SPAN_FRACTION = 0.5

/**
 * profileY is where a profile draws [elevation] down its square (`geo.profile.plot-scale`), upside down as a
 * screen counts: the [domain]'s ceiling at [PROFILE_TOP_INSET] from the top and its floor at
 * [PROFILE_BASELINE], an elevation outside the domain held to the frame, and every elevation half way between
 * the two where the domain has no span.
 *
 * The position is rounded to a thousandth of a unit, a half up, finer than any screen resolves.
 */
public fun profileY(
    elevation: Double,
    domain: ElevationDomain,
): Double {
    val span = domain.max - domain.min
    val fraction = if (span > 0.0) ((elevation - domain.min) / span).coerceIn(0.0, 1.0) else NO_SPAN_FRACTION
    return plotUnit(PROFILE_TOP_INSET + (1.0 - fraction) * (PROFILE_BASELINE - PROFILE_TOP_INSET))
}
