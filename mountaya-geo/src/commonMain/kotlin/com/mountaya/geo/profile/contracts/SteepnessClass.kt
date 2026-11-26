package com.mountaya.geo

/**
 * SteepnessClass is a band of the steepness classification a slope overlay paints, the table kept
 * apart from the slope ramp with thresholds of its own. Each is spelled by its contract id and
 * starts at [fromDegrees], the class above it starting where it ends; a slope under the first
 * class has none.
 */
public enum class SteepnessClass(
    /** The class as `geo/slope-ramp.json` spells it, `30-35`. */
    public val id: String,
    /** The least slope of the class, in degrees. */
    public val fromDegrees: Double,
) {
    /** 25 degrees, up to 30. */
    From25To30("25-30", 25.0),

    /** 30 degrees, up to 35. */
    From30To35("30-35", 30.0),

    /** 35 degrees, up to 40. */
    From35To40("35-40", 35.0),

    /** 40 degrees, up to 45. */
    From40To45("40-45", 40.0),

    /** 45 degrees and steeper. */
    From45("45+", 45.0),
}
