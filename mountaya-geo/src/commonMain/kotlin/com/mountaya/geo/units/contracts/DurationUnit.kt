package com.mountaya.geo

/**
 * DurationUnit is a unit a duration is stated in, each spelled by its contract unit id, the
 * ECMA-402 identifier. Each holds a fixed number of milliseconds, which a month and a year do
 * not, so neither is one.
 *
 * It shares its simple name with `kotlin.time.DurationUnit`, which no file imports by default; a
 * file that names both imports one of them under an alias.
 */
public enum class DurationUnit(
    /** The contract's unit id, as `geo/units-convert.json` spells it. */
    public val id: String,
) {
    /** A day of exactly 86,400 seconds. */
    Day("day"),

    /** An hour, 3,600 seconds. */
    Hour("hour"),

    /** A thousandth of a second. */
    Millisecond("millisecond"),

    /** A minute, 60 seconds. */
    Minute("minute"),

    /** The second. */
    Second("second"),
}
