package com.mountaya.geo

/**
 * ProfileReadout is what a profile states about the sample its cursor stands on: the hover card, and the
 * readout a screen reader hears (`geo.profile.cursor-readout`), one reading so the two never say different
 * figures.
 *
 * Two are equal where they state the same cells, the same grade and the same stretch, as every value of the geo
 * layer compares.
 */
public class ProfileReadout(
    /** The figures asked for, in the order asked, each with a value. */
    public val cells: List<ReadoutCell>,
    /** The grade at the sample, in degrees, negative for a descent, null where none reads. */
    public val grade: Double?,
    /** The classified stretch the sample stands in, which names it and colours it, null in none. */
    public val stretch: ProfileClass?,
) {
    override fun equals(other: Any?): Boolean =
        other is ProfileReadout && other.cells == cells && sameNumberOrNull(other.grade, grade) &&
            other.stretch == stretch

    override fun hashCode(): Int = (cells.hashCode() * 31 + grade.hashCode()) * 31 + stretch.hashCode()

    override fun toString(): String = "ProfileReadout(cells=$cells, grade=$grade, stretch=$stretch)"
}
