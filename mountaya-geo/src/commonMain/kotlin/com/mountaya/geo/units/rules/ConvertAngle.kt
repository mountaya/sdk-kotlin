package com.mountaya.geo

import kotlin.math.PI

/** The radians in a degree, pi over 180, the `degreesToRadians` `geo/geodesy.json` declares. */
internal const val RADIANS_PER_DEGREE: Double = PI / 180.0

/** The degrees in a radian, 180 over pi, the `degreesPerRadian` `geo/units-convert.json` declares. */
internal const val DEGREES_PER_RADIAN: Double = 180.0 / PI

/** The degrees in a turn, the `degreesPerTurn` `geo/terrain-slope-aspect.json` declares. */
internal const val DEGREES_PER_TURN: Double = 360.0

/**
 * convertAngle converts [value] from one angle unit to the other through pi over 180. A value in
 * its own unit is returned as it is, and nothing is reduced to one turn.
 */
public fun convertAngle(
    value: Double,
    from: AngleUnit,
    to: AngleUnit,
): Double =
    when {
        from == to -> value
        to == AngleUnit.Radian -> value * RADIANS_PER_DEGREE
        else -> value * DEGREES_PER_RADIAN
    }
