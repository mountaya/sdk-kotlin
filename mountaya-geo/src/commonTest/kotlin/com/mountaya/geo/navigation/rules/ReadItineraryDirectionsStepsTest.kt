package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadItineraryDirectionsStepsTest {
    private fun step(
        from: Int,
        to: Int,
        type: String,
        name: String,
        exit: Int? = null,
    ) = RestSchema.ItineraryDirectionsStep(
        distance = 8_400.0,
        duration = 7_200.0,
        exit_number = exit,
        from_index = from,
        name = name,
        to_index = to,
        type = RestSchema.ManeuverType(type),
    )

    @Test
    fun aRestStepIsReadAsWritten() {
        val reading =
            readItineraryDirectionsSteps(listOf(step(0, 1, "depart", "Route des Chavants"), step(1, 2, "goal", "-")), 3)

        assertEquals(
            NavigationStepsReading(
                false,
                listOf(
                    NavigationStep(
                        0,
                        1,
                        RestSchema.ManeuverType.Depart,
                        "Route des Chavants",
                        8_400.0,
                        7_200.0,
                    ),
                    NavigationStep(1, 2, RestSchema.ManeuverType.Goal, null, 8_400.0, 7_200.0),
                ),
            ),
            reading,
        )
    }

    /**
     * A member REST adds after this build, and the Data API's spelling, are said as `unknown`, and
     * the step is kept with everything else it states.
     */
    @Test
    fun aManeuverRestDoesNotSpellIsUnknownAndTheStepIsKept() {
        val reading =
            readItineraryDirectionsSteps(
                listOf(step(0, 1, "merge", "Route Blanche", exit = 2), step(1, 2, "TURN_LEFT", "")),
                3,
            )

        assertEquals(
            listOf(RestSchema.ManeuverType.Unknown, RestSchema.ManeuverType.Unknown),
            reading.steps.map { it.maneuver },
        )
        assertEquals(2, reading.steps[0].exitNumber)
        assertEquals("Route Blanche", reading.steps[0].name)
    }

    @Test
    fun aSegmentWithNoStepsIsNoRefusalAndOneThatDoesNotTileIsRefused() {
        assertEquals(NavigationStepsReading(false, emptyList()), readItineraryDirectionsSteps(null, 3))
        assertEquals(
            NavigationStepsReading(true, emptyList()),
            readItineraryDirectionsSteps(listOf(step(0, 1, "depart", "")), 3),
        )
    }
}
