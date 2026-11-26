package com.mountaya.geo

import kotlin.math.atan
import kotlin.math.tan

/**
 * convertSlope converts [value] between degrees, a percentage grade and a rise-over-run ratio
 * through the tangent: a grade is the ratio in hundredths and an angle its arctangent, so
 * forty-five degrees is a hundred percent, not forty-five. A descent keeps its sign, a value in
 * its own unit is returned as it is, and nothing is clamped.
 */
public fun convertSlope(
    value: Double,
    from: SlopeUnit,
    to: SlopeUnit,
): Double {
    if (from == to) return value
    val ratio =
        when (from) {
            SlopeUnit.Degrees -> tan(value * RADIANS_PER_DEGREE)
            SlopeUnit.Percentage -> value / PERCENT_PER_RATIO
            SlopeUnit.Ratio -> value
        }
    return when (to) {
        SlopeUnit.Degrees -> atan(ratio) * DEGREES_PER_RADIAN
        SlopeUnit.Percentage -> ratio * PERCENT_PER_RATIO
        SlopeUnit.Ratio -> ratio
    }
}
