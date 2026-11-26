package com.mountaya.geo

import kotlin.math.floor

/**
 * The west and east edges of a box, [west] at or west of [east], in the form every box of the geo
 * layer takes, as `[west, east]`: the west edge brought within [-180, 180] by whole turns and the
 * east edge moved by the same turns, past 180 where the box crosses the antimeridian. A west edge
 * already within [-180, 180] keeps its digits, 180 included.
 *
 * A box a turn or more wide covers every longitude and is the whole world, `[-180, 180]`, and so
 * is one with its west edge at the western infinity or its east edge at the eastern one. An edge
 * that is `NaN`, both edges at one infinity, and edges handed east first with one at an infinity
 * place the box at no longitude, and both edges answer `NaN`: no whole turn brings an infinite
 * edge back.
 */
internal fun canonicalLongitudes(
    west: Double,
    east: Double,
): List<Double> {
    val span = east - west
    if (span >= DEGREES_PER_TURN) return listOf(-LONGITUDE_LIMIT, LONGITUDE_LIMIT)
    if (span.isNaN() || span == Double.NEGATIVE_INFINITY) return listOf(Double.NaN, Double.NaN)
    // Both edges are finite here. The whole turns that bring the west edge within [-180, 180], and
    // none for a west edge already there, so one on 180 itself stays where it is written.
    val outsideOneWorld = west < -LONGITUDE_LIMIT || west > LONGITUDE_LIMIT
    val turns = if (outsideOneWorld) floor((west + LONGITUDE_LIMIT) / DEGREES_PER_TURN) else 0.0
    return listOf(west - turns * DEGREES_PER_TURN, east - turns * DEGREES_PER_TURN)
}
