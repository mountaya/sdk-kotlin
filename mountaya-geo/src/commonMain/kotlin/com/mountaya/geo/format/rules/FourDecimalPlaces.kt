package com.mountaya.geo

import kotlin.math.floor

/**
 * Returns [magnitude], a finite number of zero or more, written with four decimal places in Latin
 * digits and a full stop: its binary value rounded half away from zero, exactly, by integer
 * arithmetic rather than by a platform formatter. A value exactly halfway between two places,
 * such as 0.03125, takes the larger.
 *
 * The digits come from no formatter because a formatter's rounding and digits are the platform's
 * to choose, and a figure a reader sees has to read alike in every SDK. Every finite magnitude is
 * written in full, however large.
 */
internal fun fourDecimalPlaces(magnitude: Double): String = decimalPlaces(magnitude, PLACES)

/**
 * Returns [magnitude], a finite number of zero or more, written with [places] decimal places, from 1 to 5, in Latin
 * digits and a full stop: its binary value rounded half away from zero, exactly, by integer arithmetic, as
 * [fourDecimalPlaces] writes four.
 */
internal fun decimalPlaces(
    magnitude: Double,
    places: Int,
): String {
    require(places in 1..MAX_PLACES) { "$places places is outside 1 to $MAX_PLACES" }
    val whole = floor(magnitude)
    val unit = powerOfFive(places) shl places
    // Subtracting the floor is exact for every finite double, so the fraction is the value's own.
    var fraction = roundedFraction(magnitude - whole, places)
    var carry = 0L
    if (fraction == unit) {
        carry = 1L
        fraction = 0L
    }
    return wholeDigits(whole, carry) + "." + fraction.toString().padStart(places, '0')
}

private const val PLACES = 4
private const val MAX_PLACES = 5

/** The low bits of a significand set apart, so its product with five to the fifth stays within a `Long`. */
private const val LOW_BITS = 12

/** Five to the power [places]: ten to that power is it times two to that power. */
private fun powerOfFive(places: Int): Long {
    var power = 1L
    repeat(places) { power *= 5L }
    return power
}

/** [fraction], from zero to under one, as a whole number of units of the [places]th decimal place, a half rounding up. */
private fun roundedFraction(
    fraction: Double,
    places: Int,
): Long {
    if (fraction == 0.0) return 0L
    val (significand, exponent) = binaryParts(fraction)
    // fraction × 10^places is significand × 5^places × 2^(exponent + places), and for a fraction under one the power
    // of two is a division by 2^48 or more. The significand's low bits are set apart so no product leaves a `Long`:
    // significand × 5^places is high × 2^12 + low, with high under 2^54.
    val five = powerOfFive(places)
    val shift = -(exponent + places)
    val product = (significand and ((1L shl LOW_BITS) - 1L)) * five
    val high = (significand ushr LOW_BITS) * five + (product ushr LOW_BITS)
    // What the low bits leave is under one unit of `high`, so it moves neither the quotient nor the half.
    val bits = shift - LOW_BITS
    if (bits >= Long.SIZE_BITS - 1) return 0L
    val quotient = high ushr bits
    val remainder = high - (quotient shl bits)
    return if (bits > 0 && remainder >= 1L shl (bits - 1)) quotient + 1L else quotient
}

/** Two to the 63rd, the first whole double a `Long` does not hold. */
private const val TWO_TO_THE_63RD = 9.223372036854775808E18

/** A limb of the exact decimal expansion holds nine digits. */
private const val LIMB = 1_000_000_000L

/** [whole], a whole number of zero or more, plus [carry], in decimal digits. */
private fun wholeDigits(
    whole: Double,
    carry: Long,
): String {
    // A carry comes from a fraction, and only a double under 2^52 has one.
    if (whole < TWO_TO_THE_63RD) return (whole.toLong() + carry).toString()
    val (significand, exponent) = binaryParts(whole)
    // Little-endian limbs of nine digits, doubled up to thirty times a pass.
    val limbs = mutableListOf(significand % LIMB, significand / LIMB)
    var remaining = exponent
    while (remaining > 0) {
        val step = minOf(remaining, MAX_STEP)
        var up = 0L
        for (index in limbs.indices) {
            val product = (limbs[index] shl step) + up
            limbs[index] = product % LIMB
            up = product / LIMB
        }
        while (up > 0L) {
            limbs += up % LIMB
            up /= LIMB
        }
        remaining -= step
    }
    while (limbs.size > 1 && limbs.last() == 0L) limbs.removeAt(limbs.lastIndex)
    return buildString {
        append(limbs.last())
        for (index in limbs.lastIndex - 1 downTo 0) append(limbs[index].toString().padStart(LIMB_DIGITS, '0'))
    }
}

private const val MAX_STEP = 30
private const val LIMB_DIGITS = 9

/** A positive finite [value] as its whole significand and the power of two it is scaled by. */
private fun binaryParts(value: Double): Pair<Long, Int> {
    val bits = value.toRawBits()
    val biased = ((bits ushr SIGNIFICAND_BITS) and EXPONENT_MASK).toInt()
    val stored = bits and SIGNIFICAND_MASK
    return if (biased == 0) {
        stored to SUBNORMAL_EXPONENT
    } else {
        (stored or (1L shl SIGNIFICAND_BITS)) to biased - EXPONENT_BIAS
    }
}

private const val SIGNIFICAND_BITS = 52
private const val SIGNIFICAND_MASK = (1L shl SIGNIFICAND_BITS) - 1L
private const val EXPONENT_MASK = 0x7FFL
private const val EXPONENT_BIAS = 1_075
private const val SUBNORMAL_EXPONENT = -1_074
