package com.mountaya.geo

/**
 * SlopeAspect is how steep the ground is at a point, and which way it falls.
 *
 * Two are equal where they hold the same numbers, `NaN` the same as `NaN` and 0 another than -0, as
 * every value of the geo layer compares, and no aspect is another value than any bearing.
 */
public class SlopeAspect(
    /** The angle of the ground from the horizontal, in degrees: 0 on flat ground. */
    public val slope: Double,
    /**
     * The compass bearing the ground falls towards, in degrees within [0, 360), 0 north and 90
     * east, or null on flat ground, which faces nowhere. A bearing of 0 is a north-facing slope.
     */
    public val aspect: Double?,
) {
    override fun equals(other: Any?): Boolean {
        if (other !is SlopeAspect || !sameNumber(other.slope, slope)) return false
        val theirs = other.aspect
        val ours = aspect
        return if (theirs == null || ours == null) theirs == ours else sameNumber(theirs, ours)
    }

    override fun hashCode(): Int = slope.hashCode() * 31 + (aspect?.hashCode() ?: 0)

    override fun toString(): String = "SlopeAspect(slope=$slope, aspect=$aspect)"
}
