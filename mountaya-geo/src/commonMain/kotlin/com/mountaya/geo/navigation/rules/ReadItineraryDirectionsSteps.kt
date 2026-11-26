package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * readItineraryDirectionsSteps reads the steps of a REST directions segment into the one vocabulary
 * every navigation rule reads, and refuses the list whole where it does not tile the segment's
 * geometry (`geo.navigation.steps`).
 *
 * A maneuver is read as written, and one REST does not spell, the Data API's SCREAMING_SNAKE
 * included, as `unknown`. An empty name and the `-` the API writes for an unnamed way read as no
 * name, and every other member is read as sent. A list is refused, never repaired, unless its ranges
 * cover the geometry once in travel order.
 *
 * @param steps The segment's `steps` as REST sends them: null for a segment with none, a `linestring`
 * one.
 * @param coordinateCount The number of vertices in the segment's geometry, the index basis of the steps.
 * @return The steps read, and whether the list was refused: a refused list reads as no steps, and the
 * segment is followed as one with none.
 */
public fun readItineraryDirectionsSteps(
    steps: List<RestSchema.ItineraryDirectionsStep>?,
    coordinateCount: Int,
): NavigationStepsReading =
    stepsReading(
        steps.orEmpty().map { step ->
            NavigationStep(
                fromIndex = step.from_index,
                toIndex = step.to_index,
                maneuver = maneuverOf(step.type),
                name = wayName(step.name),
                distance = step.distance,
                duration = step.duration,
                exitNumber = step.exit_number,
            )
        },
        coordinateCount,
    )
