package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadDirectionsStepsTest {
    private fun step(
        from: Int?,
        to: Int?,
        maneuver: DataSchema.ManeuverType?,
        wayName: String? = null,
        exit: Int? = null,
    ): DataSchema.DirectionsStep =
        DataSchema.DirectionsStep {
            fromIndex = from
            toIndex = to
            type = maneuver
            name = wayName
            distance = 180.0
            duration = 150.0
            exitNumber = exit
        }

    @Test
    fun aDataApiStepIsReadIntoRestsVocabulary() {
        val reading =
            readDirectionsSteps(
                listOf(
                    step(0, 1, DataSchema.ManeuverType.Depart, "Rue du Docteur Paccard"),
                    step(1, 3, DataSchema.ManeuverType.EnterRoundabout, "-", exit = 2),
                    step(3, 3, DataSchema.ManeuverType.Goal, ""),
                ),
                4,
            )

        assertEquals(
            NavigationStepsReading(
                refused = false,
                steps =
                    listOf(
                        NavigationStep(
                            0,
                            1,
                            RestSchema.ManeuverType.Depart,
                            "Rue du Docteur Paccard",
                            180.0,
                            150.0,
                        ),
                        NavigationStep(
                            1,
                            3,
                            RestSchema.ManeuverType.EnterRoundabout,
                            null,
                            180.0,
                            150.0,
                            2,
                        ),
                        NavigationStep(3, 3, RestSchema.ManeuverType.Goal, null, 180.0, 150.0),
                    ),
            ),
            reading,
        )
    }

    /**
     * The Data API's members are nullable by selection: a figure it states none of is none, and a
     * maneuver it states none of is said as one outside the set.
     */
    @Test
    fun aMemberTheStepDoesNotStateIsNone() {
        val reading =
            readDirectionsSteps(
                listOf(
                    DataSchema.DirectionsStep {
                        fromIndex = 0
                        toIndex = 1
                    },
                ),
                2,
            )

        assertEquals(listOf(NavigationStep(0, 1, RestSchema.ManeuverType.Unknown)), reading.steps)
    }

    /** A step that states no range indexes no vertex, so the list does not tile. */
    @Test
    fun aStepThatStatesNoRangeRefusesTheList() {
        assertEquals(NavigationStepsReading(true, emptyList()), readDirectionsSteps(listOf(step(0, null, null)), 2))
        assertEquals(NavigationStepsReading(true, emptyList()), readDirectionsSteps(listOf(step(null, 1, null)), 2))
    }

    @Test
    fun aListThatDoesNotTileIsRefusedWhole() {
        val reading =
            readDirectionsSteps(
                listOf(step(1, 2, DataSchema.ManeuverType.Depart), step(2, 3, DataSchema.ManeuverType.Goal)),
                4,
            )

        assertEquals(NavigationStepsReading(true, emptyList()), reading)
    }

    @Test
    fun noListAndAnEmptyOneAreNoStepsAndNoRefusal() {
        assertEquals(NavigationStepsReading(false, emptyList()), readDirectionsSteps(null, 3))
        assertEquals(NavigationStepsReading(false, emptyList()), readDirectionsSteps(emptyList(), 3))
    }
}
