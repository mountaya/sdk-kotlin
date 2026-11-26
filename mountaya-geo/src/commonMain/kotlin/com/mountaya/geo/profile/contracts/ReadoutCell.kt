package com.mountaya.geo

/**
 * ReadoutCell is one figure a cursor readout states (`geo.profile.cursor-readout`): which it is, and its value
 * in meters.
 *
 * Two are equal where they state one figure of the same number, as every value of the geo layer compares.
 */
public class ReadoutCell(
    /** The figure the cell states. */
    public val stat: ReadoutStat,
    /** Its value, in meters. */
    public val value: Double,
) {
    override fun equals(other: Any?): Boolean =
        other is ReadoutCell && other.stat == stat && sameNumber(other.value, value)

    override fun hashCode(): Int = stat.hashCode() * 31 + value.hashCode()

    override fun toString(): String = "ReadoutCell(stat=$stat, value=$value)"
}
