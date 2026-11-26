package com.mountaya.geo

/**
 * convertProportion converts [value] between a ratio and a percentage, a ratio of one being a
 * hundred percent. Nothing is clamped: a ratio of 2.5 is 250 percent.
 */
public fun convertProportion(
    value: Double,
    from: ProportionUnit,
    to: ProportionUnit,
): Double = scaleByRatio(value, from.percent, to.percent)

/** The unit's size in percent. */
private val ProportionUnit.percent: Long
    get() =
        when (this) {
            ProportionUnit.Percent -> 1L
            ProportionUnit.Ratio -> PERCENT_PER_RATIO
        }

/** The percent in a ratio of one, the `percentPerRatio` the units fixtures declare. */
internal const val PERCENT_PER_RATIO = 100L
