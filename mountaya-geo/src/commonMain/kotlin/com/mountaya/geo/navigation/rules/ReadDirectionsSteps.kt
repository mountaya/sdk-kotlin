package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/**
 * readDirectionsSteps reads the steps of a Data API route into the one vocabulary every navigation
 * rule reads, REST's, and refuses the list whole where it does not tile the route's geometry
 * (`geo.navigation.steps`).
 *
 * A maneuver reads as its REST member (`TURN_LEFT` is `turn-left`), and one the Data API does not
 * spell, REST's kebab-case included, as `unknown`, so a maneuver the API adds after this build is
 * said as one it knows and the step is never dropped. An empty name and the `-` the API writes for an
 * unnamed way read as no name; a distance, a duration and an exit are read as sent, none where the
 * step states none. A list is refused, never repaired, unless its ranges cover the geometry once in
 * travel order, and so is one holding a step that states no range, since it indexes no vertex.
 *
 * @param steps The route's `steps` as the Data API sends them, or null where it sends none.
 * @param coordinateCount The number of vertices in the route's geometry, the index basis of the steps.
 * @return The steps read, and whether the list was refused: a refused list reads as no steps, and the
 * route is followed as one with none.
 */
public fun readDirectionsSteps(
    steps: List<DataSchema.DirectionsStep>?,
    coordinateCount: Int,
): NavigationStepsReading {
    if (steps.isNullOrEmpty()) return stepsReading(emptyList(), coordinateCount)
    val read = ArrayList<NavigationStep>(steps.size)
    for (step in steps) {
        val fromIndex = step.fromIndex ?: return refusedSteps
        val toIndex = step.toIndex ?: return refusedSteps
        read +=
            NavigationStep(
                fromIndex = fromIndex,
                toIndex = toIndex,
                maneuver = dataManeuverOf(step.type),
                name = wayName(step.name),
                distance = step.distance,
                duration = step.duration,
                exitNumber = step.exitNumber,
            )
    }
    return stepsReading(read, coordinateCount)
}
