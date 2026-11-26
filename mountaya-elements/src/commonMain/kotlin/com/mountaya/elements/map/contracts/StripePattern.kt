package com.mountaya.elements

import com.mountaya.geo.ShapePaint

/**
 * StripePattern is how a stripe pattern is drawn, the square tile a stretch of a route is painted with,
 * repeated along it (`map.icons.stripe-pattern`): the ground it is filled with, and stripes stroked over
 * it with round ends.
 *
 * Every length is in the tile's pixels, density-independent ones. Two are equal where every member is the
 * same, each number as the same number and each colour by its characters.
 */
internal class StripePattern(
    /** The side of the square tile. */
    val size: Double,
    /** The stroke width of each stripe. */
    val width: Double,
    /** The colour the stripes are stroked in. */
    val color: String,
    /** The colour the tile is filled with under the stripes, or null for gaps that show what is drawn beneath. */
    val ground: String?,
    /** How each end of a stripe is drawn. */
    val lineCap: ShapePaint.LineCap,
    /**
     * The stripes, each from its lower left end to its upper right one, `[x, y]` from the tile's top left
     * corner: the stripe through the tile and the two halves that complete it across the tile's edges, so
     * the tile repeats with no seam.
     */
    val segments: List<List<List<Double>>>,
) {
    override fun equals(other: Any?): Boolean =
        other is StripePattern &&
            other.size.compareTo(size) == 0 &&
            other.width.compareTo(width) == 0 &&
            other.color == color &&
            other.ground == ground &&
            other.lineCap == lineCap &&
            other.segments == segments

    override fun hashCode(): Int {
        var hash = size.hashCode()
        hash = 31 * hash + width.hashCode()
        hash = 31 * hash + color.hashCode()
        hash = 31 * hash + ground.hashCode()
        hash = 31 * hash + lineCap.hashCode()
        return 31 * hash + segments.hashCode()
    }

    override fun toString(): String =
        "StripePattern(size=$size, width=$width, color=$color, ground=$ground, lineCap=${lineCap.id}, segments=$segments)"
}
