package com.mountaya.geo

/**
 * InfoRange is one entry of a segment's analysis, as the wire's `slope_info`, `surface_info` and
 * the rest carry it: an inclusive range of vertex indexes and the value that holds over it. The
 * indexes count the vertices of every line the segment holds, laid end to end.
 *
 * Two are equal where they hold the same indexes and equal values.
 */
public class InfoRange<out T>(
    /** The index of the range's first vertex. */
    public val fromIndex: Int,
    /** The index of its last vertex. */
    public val toIndex: Int,
    /** The value over the range. */
    public val value: T,
) {
    override fun equals(other: Any?): Boolean =
        other is InfoRange<*> && other.fromIndex == fromIndex && other.toIndex == toIndex && other.value == value

    override fun hashCode(): Int = (fromIndex * 31 + toIndex) * 31 + value.hashCode()

    override fun toString(): String = "InfoRange(fromIndex=$fromIndex, toIndex=$toIndex, value=$value)"
}
