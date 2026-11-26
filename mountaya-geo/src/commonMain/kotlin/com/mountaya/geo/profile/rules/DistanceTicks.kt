package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10

/**
 * DEFAULT_DISTANCE_TICK_COUNT is how many stops a distance axis is labelled at where its caller asks
 * for no other count, the `defaultTarget` `geo/distance-ticks.json` declares.
 */
public const val DEFAULT_DISTANCE_TICK_COUNT: Int = 6

/** The three decades between a kilometer and a meter. */
private const val METERS_DECADES = 3

/** The steps a decade holds: one, two and five times its power of ten. */
private const val TWO = 2.0
private const val FIVE = 5.0

/**
 * distanceTicks is the stops a distance axis over the range [from] to [to], in meters, is labelled
 * at, on the 1, 2, 5 lattice of the reader's distance unit: kilometers for a metric [length], miles
 * for an imperial one. A range given backwards is read in order.
 *
 * The step is the range's span in that unit over [target], the stop count asked for, brought to
 * one, two or five times a power of ten: the smallest of those at least the rough step, a normalized
 * step above 5 becoming ten, the next decade up. Every multiple of the step within the range is a
 * stop, with its value in the display unit, its position in meters and the fraction digits its
 * label needs, the negated exponent of the step and never under zero. A metric stop's meters are the
 * same lattice three decades up, so a kilometer stop lands on a whole count of meters.
 *
 * A range of no width, or a [target] under one, has no stops; so has a range that is no finite
 * number, or one too narrow for a double to hold its step, and the stops end where a double can
 * count no further multiple. A system other than imperial reads as metric.
 */
public fun distanceTicks(
    from: Double,
    to: Double,
    length: RestSchema.UnitLength,
    target: Int = DEFAULT_DISTANCE_TICK_COUNT,
): List<DistanceTick> {
    val imperial = isImperial(length)
    val unitMeters = convertLength(1.0, if (imperial) LengthUnit.Mile else LengthUnit.Kilometer, LengthUnit.Meter)
    val low = minOf(from, to) / unitMeters
    val high = maxOf(from, to) / unitMeters
    val span = high - low
    if (!(span > 0.0) || !span.isFinite() || target < 1) return emptyList()

    val rough = span / target
    var exponent = floor(log10(rough)).toInt()
    // Held to the decade the rough step is in, whatever the platform's logarithm rounds to.
    while (scaled(1.0, exponent) > rough) exponent -= 1
    while (scaled(1.0, exponent + 1) <= rough) exponent += 1
    val normalized = if (exponent >= 0) rough / powerOfTen(exponent) else rough * powerOfTen(-exponent)
    var mantissa =
        when {
            normalized > FIVE -> 10.0
            normalized > TWO -> FIVE
            normalized > 1.0 -> TWO
            else -> 1.0
        }
    // Ten is one, a decade up, so the fraction digits read off the exponent alone.
    if (mantissa == 10.0) {
        mantissa = 1.0
        exponent += 1
    }
    val step = scaled(mantissa, exponent)
    if (!(step > 0.0)) return emptyList()
    val fractionDigits = maxOf(0, -exponent)

    val ticks = ArrayList<DistanceTick>()
    var count = ceil(low / step)
    while (true) {
        val value = scaled(count * mantissa, exponent)
        if (!(value <= high)) break
        val meters = if (imperial) value * unitMeters else scaled(count * mantissa, exponent + METERS_DECADES)
        ticks += DistanceTick(value, meters, fractionDigits)
        // Past 2^53 a double counts no further multiple, and the next stop would be this one again.
        val next = count + 1.0
        if (next == count) break
        count = next
    }
    return ticks
}

/** [mantissa] times ten to [exponent], divided by the exact power where the exponent is negative. */
private fun scaled(
    mantissa: Double,
    exponent: Int,
): Double = if (exponent >= 0) mantissa * powerOfTen(exponent) else mantissa / powerOfTen(-exponent)
