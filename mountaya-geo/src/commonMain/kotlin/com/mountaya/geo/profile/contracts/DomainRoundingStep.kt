package com.mountaya.geo

/**
 * DomainRoundingStep is how coarsely a profile's elevation domain is rounded outward, by its span: to a
 * multiple of [stepMeters] where the widened span is at most [uptoSpanMeters], and past every bounded step,
 * the last (`geo.profile.domain`).
 *
 * Two are equal where they hold the same numbers, as every value of the geo layer compares.
 */
public class DomainRoundingStep(
    /** The multiple the domain's ends are rounded outward to. */
    public val stepMeters: Double,
    /** The widest span this step rounds, null for the step past every other. */
    public val uptoSpanMeters: Double?,
) {
    override fun equals(other: Any?): Boolean =
        other is DomainRoundingStep &&
            sameNumber(other.stepMeters, stepMeters) &&
            sameNumberOrNull(other.uptoSpanMeters, uptoSpanMeters)

    override fun hashCode(): Int = stepMeters.hashCode() * 31 + uptoSpanMeters.hashCode()

    override fun toString(): String = "DomainRoundingStep(stepMeters=$stepMeters, uptoSpanMeters=$uptoSpanMeters)"
}
