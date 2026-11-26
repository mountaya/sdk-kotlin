package com.mountaya.geo

/**
 * CurveStop is one stop of the gradient a profile's curve is painted with (`geo.profile.curve-stops`): where
 * it stands, a fraction of the plot's width from 0 to 1, and its colour, or null where no stretch is
 * coloured, which a renderer paints in its own accent.
 *
 * Two are equal where they hold the same number and the colours are written alike, or absent on both, as
 * every value of the geo layer compares.
 */
public class CurveStop(
    /** Where the stop stands, from 0 at the plot's left edge to 1 at its right. */
    public val offset: Double,
    /** The stretch's colour, or null for ground no stretch covers. */
    public val color: String?,
) {
    override fun equals(other: Any?): Boolean =
        other is CurveStop && sameNumber(other.offset, offset) && other.color == color

    override fun hashCode(): Int = offset.hashCode() * 31 + color.hashCode()

    override fun toString(): String = "CurveStop(offset=$offset, color=$color)"
}
