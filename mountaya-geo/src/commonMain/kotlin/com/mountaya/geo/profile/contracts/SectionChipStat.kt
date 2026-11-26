package com.mountaya.geo

/** SectionChipStat is one figure a section chip under a profile can state about its leg (`geo.profile.section-chips`). */
public enum class SectionChipStat(
    /** The figure as `geo/profile-section-chips.json` spells it. */
    public val id: String,
) {
    /** The leg's own length. */
    Distance("distance"),

    /** The meters the leg climbs. */
    Ascent("ascent"),

    /** The meters the leg loses. */
    Descent("descent"),

    /** The leg's average grade, its climb less its loss over its length, in degrees. */
    Grade("grade"),
}
