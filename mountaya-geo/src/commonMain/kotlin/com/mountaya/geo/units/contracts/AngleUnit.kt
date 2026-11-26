package com.mountaya.geo

/**
 * AngleUnit is a unit an angle is stated in. [Degree] is ECMA-402's identifier; [Radian] is the
 * contract's own, since ECMA-402 lists none for it.
 */
public enum class AngleUnit(
    /** The contract's unit id, as `geo/units-convert.json` spells it. */
    public val id: String,
) {
    /** A three hundred and sixtieth of a turn. */
    Degree("degree"),

    /** The radian: a turn is two pi of them. */
    Radian("radian"),
}
