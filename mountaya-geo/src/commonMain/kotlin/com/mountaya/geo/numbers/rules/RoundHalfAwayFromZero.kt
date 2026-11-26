package com.mountaya.geo

import kotlin.math.abs
import kotlin.math.floor

/** The most fraction digits a number is rounded to; a request for more reads this many. */
internal const val MAX_FRACTION_DIGITS: Int = 20

/**
 * The magnitude, scaled to the digits kept, from which the rounding leaves a value as it is: 2^48.
 * Under it a double's neighbours are so close that only one decimal of one more digit than kept
 * reads as it, so the halfway decimal is that one or none.
 */
private const val SCALED_LIMIT: Double = 281_474_976_710_656.0

/** The powers of ten a double holds exactly, 10^0 to 10^22. */
private val exactPowersOfTen: DoubleArray =
    DoubleArray(23).also { powers ->
        var power = 1.0
        for (index in powers.indices) {
            powers[index] = power
            power *= 10.0
        }
    }

/**
 * Returns 10 to the power [exponent], from 0 up: exact to 10^22, which is every power a double
 * holds exactly, and a product of exact powers past it.
 */
internal fun powerOfTen(exponent: Int): Double {
    var remaining = exponent.coerceAtLeast(0)
    var power = 1.0
    while (remaining > exactPowersOfTen.lastIndex) {
        power *= exactPowersOfTen.last()
        remaining -= exactPowersOfTen.lastIndex
    }
    return power * exactPowersOfTen[remaining]
}

/**
 * Returns [value] rounded to [fractionDigits] places, half away from zero on its decimal value:
 * the decimal a reader would write the double as, never the binary value it holds. 0.85 is held
 * as 0.849999..., and to one place it is 0.9, as it reads; 2.675 to two places is 2.68.
 *
 * Each number a reader is shown is rounded here, before any platform formatter sees it, so the
 * figure is the same on every target: `java.text` rounds half to even on the binary value, and a
 * formatter handed an unrounded value rounds it its own way. The answer is the double nearest the
 * rounded decimal, which every formatter writes with no further rounding.
 *
 * A value that rounds to zero is +0, so it is printed with no sign: `-0.3` to a whole degree is 0,
 * never -0, which reads as a descent that is not there. A value that is not finite comes back as it
 * is, and so does one so large that no place it is rounded to is a fraction of it.
 * [fractionDigits] is read within 0 and [MAX_FRACTION_DIGITS].
 */
internal fun roundHalfAwayFromZero(
    value: Double,
    fractionDigits: Int,
): Double {
    if (!value.isFinite()) return value
    val scale = powerOfTen(fractionDigits.coerceIn(0, MAX_FRACTION_DIGITS))
    val magnitude = abs(value)
    val scaled = magnitude * scale
    if (scaled >= SCALED_LIMIT) return value
    val lower = floor(scaled)
    // The halfway decimal above `lower`, as the double nearest it: an odd count of halves over a
    // power of ten, each exact, divided once. A value that is that double reads as the halfway
    // decimal and is rounded away from zero; any other lies wholly on one side of it.
    val halfway = (2.0 * lower + 1.0) / (2.0 * scale)
    val count = if (magnitude >= halfway) lower + 1.0 else lower
    if (count == 0.0) return 0.0
    val rounded = count / scale
    return if (value < 0.0) -rounded else rounded
}
