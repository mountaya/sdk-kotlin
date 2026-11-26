package com.mountaya.geo

/**
 * RouteGeometry is the one route a fly-through follows: the line its camera flies, the ground's
 * elevation under each vertex where every vertex carries one, and the vertices it jumps to.
 *
 * Two routes are the same route by what they hold, never by identity: every vertex, every
 * elevation and every jump, in order, each value the same number, `NaN` the same as `NaN` and 0
 * another than -0. A fresh copy is the same route and an edited one is another, so a flight
 * planned over one stands over the other exactly where the two are equal.
 */
public class RouteGeometry(
    /** The vertices the camera flies through, in order, each `[longitude, latitude]` in degrees. */
    public val line: List<List<Double>>,
    /**
     * The ground's elevation under each vertex of [line], in meters, one per vertex; empty where the
     * route states none, which is where some vertex carries none.
     */
    public val elevations: List<Double> = emptyList(),
    /**
     * The index in [line] of each vertex a jump lands on, ascending: the first vertex of each line
     * after the first, which the camera flies to while the distance a reader is told charges
     * nothing for the leg. Empty where the route states none.
     */
    public val jumps: List<Int> = emptyList(),
) {
    override fun equals(other: Any?): Boolean =
        other is RouteGeometry &&
            other.line.size == line.size &&
            line.indices.all { sameNumbers(other.line[it], line[it]) } &&
            sameNumbers(other.elevations, elevations) &&
            other.jumps == jumps

    override fun hashCode(): Int = (line.hashCode() * 31 + elevations.hashCode()) * 31 + jumps.hashCode()

    override fun toString(): String = "RouteGeometry(line=$line, elevations=$elevations, jumps=$jumps)"
}
