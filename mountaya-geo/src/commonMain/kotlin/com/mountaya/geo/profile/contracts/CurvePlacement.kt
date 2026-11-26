package com.mountaya.geo

/**
 * CurvePlacement is an itinerary's places as its profile draws them, each pass within the window once
 * (`geo.profile.curve-placement`): the pins on the curve, the names under the distance axis and the cut-off
 * marks, each in itinerary order, read once so the pins, the names and the drop lines agree.
 *
 * Two are equal where they hold the same pins, dividers and cut-offs, in the same order.
 */
public class CurvePlacement(
    /** The passes pinned on the curve. */
    public val pins: List<CurvePin>,
    /** The passes that divide the itinerary, named under its axis. */
    public val dividers: List<CurveDivider>,
    /** The passes that publish a cut-off. */
    public val cutoffs: List<CurveCutoff>,
) {
    override fun equals(other: Any?): Boolean =
        other is CurvePlacement && other.pins == pins && other.dividers == dividers && other.cutoffs == cutoffs

    override fun hashCode(): Int = (pins.hashCode() * 31 + dividers.hashCode()) * 31 + cutoffs.hashCode()

    override fun toString(): String = "CurvePlacement(pins=$pins, dividers=$dividers, cutoffs=$cutoffs)"
}
