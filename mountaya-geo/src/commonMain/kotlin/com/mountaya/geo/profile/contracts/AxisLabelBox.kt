package com.mountaya.geo

/**
 * AxisLabelBox is one place name laid out under a profile's distance axis (`geo.profile.axis-label-culling`):
 * its key and the left and right edges it takes, in whatever unit the caller measured, the pixels of its own
 * screen.
 *
 * Two are equal where they hold the same key, written alike, and the same numbers, as every value of the geo
 * layer compares.
 */
public class AxisLabelBox(
    /** The name's place, `id#pass`. */
    public val key: String,
    /** Where the name's left edge stands. */
    public val left: Double,
    /** Where the name's right edge stands. */
    public val right: Double,
) {
    override fun equals(other: Any?): Boolean =
        other is AxisLabelBox && other.key == key && sameNumber(other.left, left) && sameNumber(other.right, right)

    override fun hashCode(): Int = (key.hashCode() * 31 + left.hashCode()) * 31 + right.hashCode()

    override fun toString(): String = "AxisLabelBox(key=$key, left=$left, right=$right)"
}
