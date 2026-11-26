package com.mountaya.geo

/**
 * PixelPadding is a screen inset in pixels on each edge of a frame, the room a camera's fit leaves
 * between a box and the canvas's edges.
 *
 * Two are equal where they hold the same numbers, `NaN` the same as `NaN` and 0 another than -0, as
 * every value of the geo layer compares.
 */
public class PixelPadding(
    /** The inset at the top edge, toward the north, in pixels. */
    public val top: Double,
    /** The inset at the right edge, toward the east, in pixels. */
    public val right: Double,
    /** The inset at the bottom edge, toward the south, in pixels. */
    public val bottom: Double,
    /** The inset at the left edge, toward the west, in pixels. */
    public val left: Double,
) {
    /** Creates a padding of [all] pixels on every edge. */
    public constructor(all: Double) : this(all, all, all, all)

    override fun equals(other: Any?): Boolean =
        other is PixelPadding &&
            sameNumber(other.top, top) &&
            sameNumber(other.right, right) &&
            sameNumber(other.bottom, bottom) &&
            sameNumber(other.left, left)

    override fun hashCode(): Int =
        ((top.hashCode() * 31 + right.hashCode()) * 31 + bottom.hashCode()) * 31 + left.hashCode()

    override fun toString(): String = "PixelPadding(top=$top, right=$right, bottom=$bottom, left=$left)"
}
