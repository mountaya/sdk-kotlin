package com.mountaya.geo

/**
 * ProportionUnit is a unit a proportion of a whole is stated in: the bare [Ratio] a fraction is
 * computed as, where half reads 0.5, and the [Percent] a reader reads, where it reads 50. A
 * gradient takes a [SlopeUnit] instead: a grade in degrees is no proportion of anything.
 */
public enum class ProportionUnit(
    /** The contract's unit id, as `geo/units-convert.json` spells it. */
    public val id: String,
) {
    /** Hundredths of the whole, ECMA-402's `percent`. */
    Percent("percent"),

    /** The bare number, the whole being one. */
    Ratio("ratio"),
}
