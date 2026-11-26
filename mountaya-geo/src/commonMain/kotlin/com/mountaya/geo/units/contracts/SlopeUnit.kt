package com.mountaya.geo

/**
 * SlopeUnit is a unit a gradient is stated in: the two a reader's slope preference names,
 * [Degrees] and [Percentage], spelled as that preference's values, and the bare [Ratio] of the
 * rise over the run, which is what geometry measures.
 */
public enum class SlopeUnit(
    /** The contract's unit id, as `geo/units-slope.json` spells it. */
    public val id: String,
) {
    /** The angle of the ground from the horizontal, in degrees. */
    Degrees("degrees"),

    /** The grade: the ratio in hundredths, so forty-five degrees is a hundred percent. */
    Percentage("percentage"),

    /** The rise over the run. */
    Ratio("ratio"),
}
