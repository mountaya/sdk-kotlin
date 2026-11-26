package com.mountaya.geo

/**
 * SectionChipCell is one figure a section chip states about its leg (`geo.profile.section-chips`): which it is,
 * and its value in meters or degrees.
 *
 * Two are equal where they state one figure of the same number, as every value of the geo layer compares.
 */
public class SectionChipCell(
    /** The figure the cell states. */
    public val stat: SectionChipStat,
    /** Its value, in meters or, for the grade, degrees. */
    public val value: Double,
) {
    override fun equals(other: Any?): Boolean =
        other is SectionChipCell && other.stat == stat && sameNumber(other.value, value)

    override fun hashCode(): Int = stat.hashCode() * 31 + value.hashCode()

    override fun toString(): String = "SectionChipCell(stat=$stat, value=$value)"
}
