package com.mountaya.geo

/**
 * Returns [value] times [numerator] over [denominator], two whole magnitudes in one quantity's
 * base unit, the pair reduced by its greatest common divisor first. A factor that reduces to a
 * whole number multiplies once and one that reduces to a whole fraction divides once, so a mile
 * is 5,280 feet exactly rather than a product of two rounded factors.
 */
internal fun scaleByRatio(
    value: Double,
    numerator: Long,
    denominator: Long,
): Double {
    val divisor = greatestCommonDivisor(numerator, denominator)
    val up = numerator / divisor
    val down = denominator / divisor
    return when {
        down == 1L -> value * up
        up == 1L -> value / down
        else -> value * up / down
    }
}

/**
 * Returns [numerator] over [denominator], reduced as [scaleByRatio] reduces it, divided by
 * [value]: a rate read as a pace, or a pace as a rate. A value of zero answers an infinity.
 */
internal fun invertByRatio(
    value: Double,
    numerator: Long,
    denominator: Long,
): Double {
    val divisor = greatestCommonDivisor(numerator, denominator)
    val up = numerator / divisor
    val down = denominator / divisor
    return if (down == 1L) up / value else up / (value * down)
}

private tailrec fun greatestCommonDivisor(
    larger: Long,
    smaller: Long,
): Long = if (smaller == 0L) larger else greatestCommonDivisor(smaller, larger % smaller)
