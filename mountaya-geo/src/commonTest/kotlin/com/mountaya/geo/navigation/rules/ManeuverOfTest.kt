package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class ManeuverOfTest {
    private val unknown = RestSchema.ManeuverType.Unknown

    @Test
    fun theVocabularyIsRestsFifteenMembersInTheOrderOfTheirValues() {
        assertEquals(15, navigationManeuvers.size)
        assertEquals(navigationManeuvers.map { it.value }.sorted(), navigationManeuvers.map { it.value })
    }

    /**
     * A value REST does not spell is said as one this build knows, never dropped: a member the API
     * added since, and the Data API's spelling, alike. Matched exactly, letter case included.
     */
    @Test
    fun aRestManeuverIsReadAsWrittenAndAnyOtherAsUnknown() {
        assertEquals(RestSchema.ManeuverType.TurnLeft, maneuverOf(RestSchema.ManeuverType("turn-left")))
        assertEquals(unknown, maneuverOf(RestSchema.ManeuverType("merge")))
        assertEquals(unknown, maneuverOf(RestSchema.ManeuverType("TURN_LEFT")))
        assertEquals(unknown, maneuverOf(RestSchema.ManeuverType("Turn-Left")))
        assertEquals(unknown, maneuverOf(RestSchema.ManeuverType(" turn-left")))
        assertEquals(unknown, maneuverOf(RestSchema.ManeuverType("unknown")))
    }

    /** The contract's transform, held to every constant the generated Data API vocabulary prints. */
    @Test
    fun aDataApiManeuverIsReadAsItsRestMember() {
        val data =
            listOf(
                DataSchema.ManeuverType.Continue,
                DataSchema.ManeuverType.Depart,
                DataSchema.ManeuverType.EnterRoundabout,
                DataSchema.ManeuverType.ExitRoundabout,
                DataSchema.ManeuverType.Goal,
                DataSchema.ManeuverType.KeepLeft,
                DataSchema.ManeuverType.KeepRight,
                DataSchema.ManeuverType.SharpLeft,
                DataSchema.ManeuverType.SharpRight,
                DataSchema.ManeuverType.SlightLeft,
                DataSchema.ManeuverType.SlightRight,
                DataSchema.ManeuverType.TurnLeft,
                DataSchema.ManeuverType.TurnRight,
                DataSchema.ManeuverType.Unknown,
                DataSchema.ManeuverType.Uturn,
            )

        assertEquals(navigationManeuvers, data.map(::dataManeuverOf))
    }

    @Test
    fun aDataApiManeuverItDoesNotSpellOrDoesNotStateIsUnknown() {
        assertEquals(unknown, dataManeuverOf(DataSchema.ManeuverType("turn-left")))
        assertEquals(unknown, dataManeuverOf(DataSchema.ManeuverType("MERGE")))
        assertEquals(unknown, dataManeuverOf(DataSchema.ManeuverType("Turn_Left")))
        assertEquals(unknown, dataManeuverOf(null))
    }
}
