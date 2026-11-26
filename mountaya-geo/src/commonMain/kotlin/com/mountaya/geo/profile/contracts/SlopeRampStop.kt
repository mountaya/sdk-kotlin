package com.mountaya.geo

/**
 * SlopeRampStop is one stop of a slope ramp: the colour a slope of [degrees] paints, and the slopes
 * between two stops paint the colour between theirs.
 *
 * Two are equal where they hold the same colour and the same number, as every value of the geo
 * layer compares.
 */
public class SlopeRampStop(
    /** The colour, `#RRGGBB`. */
    public val color: String,
    /** The slope's magnitude, in degrees: each half of a ramp is read by magnitude. */
    public val degrees: Double,
) {
    override fun equals(other: Any?): Boolean =
        other is SlopeRampStop && other.color == color && sameNumber(other.degrees, degrees)

    override fun hashCode(): Int = color.hashCode() * 31 + degrees.hashCode()

    override fun toString(): String = "SlopeRampStop(color=$color, degrees=$degrees)"
}
