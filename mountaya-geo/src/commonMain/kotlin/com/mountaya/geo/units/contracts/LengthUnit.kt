package com.mountaya.geo

/**
 * LengthUnit is a unit a length is stated in, each spelled by its contract unit id, which is the
 * ECMA-402 identifier, so a length converted into one formats under the same id.
 */
public enum class LengthUnit(
    /** The contract's unit id, as `geo/units-convert.json` spells it. */
    public val id: String,
) {
    /** A hundredth of a meter. */
    Centimeter("centimeter"),

    /** The international foot, exactly 0.3048 m: twelve inches. */
    Foot("foot"),

    /** The international inch, exactly 2.54 cm. */
    Inch("inch"),

    /** A thousand meters. */
    Kilometer("kilometer"),

    /** The meter, the unit the wire carries every length in. */
    Meter("meter"),

    /** The international mile, exactly 1,609.344 m: 5,280 feet. */
    Mile("mile"),
}
