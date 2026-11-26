package com.mountaya.geo

/**
 * convertLength converts [value] from one length unit to another through the exact values that
 * define them: an inch is 2.54 cm, a foot twelve inches and a mile 5,280 feet. Nothing is
 * clamped, and a value that is not finite converts to itself.
 */
public fun convertLength(
    value: Double,
    from: LengthUnit,
    to: LengthUnit,
): Double = scaleByRatio(value, from.tenthsOfMillimeter, to.tenthsOfMillimeter)

/** The unit's length in tenths of a millimeter, the largest unit in which every defining value is whole. */
internal val LengthUnit.tenthsOfMillimeter: Long
    get() =
        when (this) {
            LengthUnit.Centimeter -> 100L
            LengthUnit.Foot -> 12L * INCH
            LengthUnit.Inch -> INCH
            LengthUnit.Kilometer -> 1_000L * METER
            LengthUnit.Meter -> METER
            LengthUnit.Mile -> 5_280L * 12L * INCH
        }

private const val INCH = 254L
private const val METER = 10_000L
