package com.mountaya.geo

/**
 * Returns whether [steps] cover a geometry of [coordinateCount] vertices once, in travel order: the
 * first step starts on vertex 0, each later one starts on the vertex the one before it ends on, the
 * last ends on the last vertex, and none ends before it starts, which holds every index within the
 * geometry as well. A step may start and end on one vertex.
 *
 * An empty list tiles nothing.
 */
internal fun stepsTile(
    steps: List<NavigationStep>,
    coordinateCount: Int,
): Boolean {
    var expected = 0
    for (step in steps) {
        if (step.fromIndex != expected || step.toIndex < step.fromIndex) return false
        expected = step.toIndex
    }
    return steps.isNotEmpty() && expected == coordinateCount - 1
}

/** The reading of a refused list: refused, and no steps. */
internal val refusedSteps: NavigationStepsReading = NavigationStepsReading(refused = true, steps = emptyList())

/**
 * Returns [steps] read to the tiling rule, whole: as they are where there are none or they tile a
 * geometry of [coordinateCount] vertices, and refused, with no steps, otherwise. A list is refused
 * rather than repaired, since a repair would be a guess at where a maneuver is.
 */
internal fun stepsReading(
    steps: List<NavigationStep>,
    coordinateCount: Int,
): NavigationStepsReading =
    if (steps.isEmpty() || stepsTile(steps, coordinateCount)) {
        NavigationStepsReading(refused = false, steps = steps)
    } else {
        refusedSteps
    }
