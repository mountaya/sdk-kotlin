package com.mountaya.geo

/**
 * PlotPoint is a point of a profile's plot, in the 100-unit square it is drawn in, which is also a percentage
 * of the box the square is stretched into: [x] from the window's start at 0 to its end at 100, and [y] down
 * from the top (`geo.profile.plot-scale`).
 *
 * Two are equal where they hold the same numbers, as every value of the geo layer compares.
 */
public class PlotPoint(
    /** How far across the plot the point stands. */
    public val x: Double,
    /** How far down the plot the point stands. */
    public val y: Double,
) {
    override fun equals(other: Any?): Boolean = other is PlotPoint && sameNumber(other.x, x) && sameNumber(other.y, y)

    override fun hashCode(): Int = x.hashCode() * 31 + y.hashCode()

    override fun toString(): String = "PlotPoint(x=$x, y=$y)"
}
