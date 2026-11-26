package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class StepInstructionTest {
    private fun said(
        maneuver: String,
        name: String?,
        exit: Int? = null,
    ): NavigationInstruction =
        stepInstruction(NavigationStep(0, 1, RestSchema.ManeuverType(maneuver), name, exitNumber = exit))

    @Test
    fun aManeuverIsSaidWithItsOwnKeyOntoANamedWayAndItsPlainTwinWhereNoneIs() {
        for (maneuver in navigationManeuvers) {
            assertEquals(maneuver.value, said(maneuver.value, "Rue du Docteur Paccard").key.id)
            assertEquals("Rue du Docteur Paccard", said(maneuver.value, "Rue du Docteur Paccard").name)
            assertEquals("${maneuver.value}Plain", said(maneuver.value, null).key.id)
            assertEquals(NavigationInstruction(said(maneuver.value, null).key), said(maneuver.value, null))
        }
    }

    /** The exit is what a traveller needs at a roundabout, so it is said there, entered or left. */
    @Test
    fun aRoundaboutNamingAnExitIsSaidWithTheExit() {
        assertEquals(
            NavigationInstruction(NavigationInstructionKey.RoundaboutExit, name = "Avenue Michel Croz", exit = 2),
            said("enter-roundabout", "Avenue Michel Croz", 2),
        )
        assertEquals(
            NavigationInstruction(NavigationInstructionKey.RoundaboutExitPlain, exit = 3),
            said("enter-roundabout", null, 3),
        )
        assertEquals(
            NavigationInstruction(NavigationInstructionKey.RoundaboutExitPlain, exit = 1),
            said("exit-roundabout", "-", 1),
        )
    }

    @Test
    fun anExitOfZeroOrLessOrAwayFromARoundaboutNamesNothing() {
        assertEquals(
            NavigationInstruction(NavigationInstructionKey.EnterRoundabout, name = "Avenue Michel Croz"),
            said("enter-roundabout", "Avenue Michel Croz", 0),
        )
        assertEquals(
            NavigationInstruction(NavigationInstructionKey.ExitRoundaboutPlain),
            said("exit-roundabout", null, -1),
        )
        assertEquals(
            NavigationInstruction(NavigationInstructionKey.TurnRight, name = "Rue du Docteur Paccard"),
            said("turn-right", "Rue du Docteur Paccard", 2),
        )
    }

    @Test
    fun anEmptyNameAndTheApisDashAreNoNameAndAnyOtherIsOne() {
        assertEquals(NavigationInstructionKey.TurnLeftPlain, said("turn-left", "").key)
        assertEquals(NavigationInstructionKey.TurnLeftPlain, said("turn-left", "-").key)
        assertEquals(NavigationInstruction(NavigationInstructionKey.TurnLeft, name = " "), said("turn-left", " "))
    }

    /** A waypoint passed on the way, which no surface writes, is said by its name, and plain where it has none. */
    @Test
    fun aWaypointPassedOnTheWayIsSaidByItsNameAndPlainWhereItHasNone() {
        assertEquals(
            NavigationInstruction(NavigationInstructionKey.Waypoint, name = "Refuge de Bellachat"),
            said("waypoint", "Refuge de Bellachat"),
        )
        assertEquals(NavigationInstruction(NavigationInstructionKey.WaypointPlain), said("waypoint", null))
        assertEquals(NavigationInstruction(NavigationInstructionKey.WaypointPlain), said("waypoint", "-"))
        assertEquals("waypoint", waypointManeuver.value)
    }

    /** A maneuver the API adds after this build, or another surface's spelling, is said as `unknown`. */
    @Test
    fun aManeuverOutsideTheVocabularyIsSaidAsUnknown() {
        assertEquals(
            NavigationInstruction(NavigationInstructionKey.Unknown, name = "Route Blanche"),
            said("merge", "Route Blanche"),
        )
        assertEquals(NavigationInstruction(NavigationInstructionKey.UnknownPlain), said("TURN_LEFT", null))
        assertEquals(NavigationInstruction(NavigationInstructionKey.UnknownPlain), said("ENTER_ROUNDABOUT", null, 2))
    }
}
