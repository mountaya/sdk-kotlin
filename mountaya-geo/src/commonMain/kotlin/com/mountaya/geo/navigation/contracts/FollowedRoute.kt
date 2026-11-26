package com.mountaya.geo

/**
 * FollowedRoute is a route as a traveller follows it: its geometry, and the steps that cover it once,
 * in travel order, which are the route's own or, for a route with none, a departure and an arrival
 * (`geo.navigation.stepless-route`).
 *
 * It is built by `followedRouteOf` alone, so its steps always tile its line and hold one step at the
 * least, which every reading of a position along it relies on, and its line is measured once, when it
 * is followed, rather than at every position read along it. Two are equal where every vertex and
 * every step are, each figure as the same number, `NaN` the same as `NaN` and 0 another than -0.
 */
public class FollowedRoute internal constructor(
    /** The route's geometry, as its source answered it: the index basis of its steps. */
    public val line: List<List<Double>>,
    /** The steps followed, in travel order, covering the geometry once. */
    public val steps: List<NavigationStep>,
    /**
     * The distance along [line] to each of its vertices, its `cumulativeMeters`, measured once and held unboxed:
     * `followedRouteOf` measures the line and hands the measure over, so following a route measures it once.
     */
    internal val meters: DoubleArray,
) {
    /** A route following [line] by [steps], its line measured here. */
    internal constructor(
        line: List<List<Double>>,
        steps: List<NavigationStep>,
    ) : this(line, steps, cumulativeMeters(line).toDoubleArray())

    /**
     * The distance in meters along [line] to each of its vertices, one for each, as `cumulativeMeters`
     * measures the line, measured once when the route is followed: a step's length is the distance at its
     * last vertex less the one at its first, so an engine of the host's own, or a guidance plan, reads it
     * without measuring the line again. A vertex that names no place stands where the last one before it
     * that names a place does, and at 0 before any.
     */
    public val measured: List<Double> = meters.asList()

    /** The index in [line] of each vertex that names a place, in order: the ends of the stretches a position is read on. */
    internal val placed: IntArray

    /** The longitude of each vertex [placed] names, in the same order, held unboxed. */
    internal val longitudes: DoubleArray

    /** The latitude of each vertex [placed] names, in the same order, held unboxed. */
    internal val latitudes: DoubleArray

    init {
        var count = 0
        for (vertex in line) if (namesPlace(vertex)) count += 1
        placed = IntArray(count)
        longitudes = DoubleArray(count)
        latitudes = DoubleArray(count)
        var next = 0
        for ((index, vertex) in line.withIndex()) {
            if (!namesPlace(vertex)) continue
            placed[next] = index
            longitudes[next] = vertex[0]
            latitudes[next] = vertex[1]
            next += 1
        }
    }

    override fun equals(other: Any?): Boolean =
        this === other ||
            (
                other is FollowedRoute && other.line.size == line.size &&
                    line.indices.all { sameNumbers(other.line[it], line[it]) } && other.steps == steps
            )

    override fun hashCode(): Int = line.hashCode() * 31 + steps.hashCode()

    override fun toString(): String = "FollowedRoute(line=$line, steps=$steps)"
}
