package com.mountaya.geo

import com.mountaya.geo.NavigationInstructionArgument.Exit
import com.mountaya.geo.NavigationInstructionArgument.Name
import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationInstructionKeyTest {
    /**
     * Two keys a maneuver, the waypoint passed on the way's among them, then the roundabout's exit pair: the closed set
     * a host's strings fill.
     */
    @Test
    fun everyManeuverHasItsOwnKeyAndItsPlainTwinThenTheRoundaboutExitPair() {
        val expected =
            (navigationManeuvers + waypointManeuver).flatMap { listOf(it.value, "${it.value}Plain") } +
                listOf("roundaboutExit", "roundaboutExitPlain")

        assertEquals(34, NavigationInstructionKey.entries.size)
        assertEquals(expected.sorted(), NavigationInstructionKey.entries.map { it.id })
    }

    @Test
    fun aKeyNamesTheArgumentsItTakesInOrder() {
        assertEquals(listOf(Name), NavigationInstructionKey.TurnLeft.arguments)
        assertEquals(emptyList(), NavigationInstructionKey.TurnLeftPlain.arguments)
        assertEquals(listOf(Exit, Name), NavigationInstructionKey.RoundaboutExit.arguments)
        assertEquals(listOf(Exit), NavigationInstructionKey.RoundaboutExitPlain.arguments)
        assertEquals(
            NavigationInstructionKey.entries
                .filterNot {
                    it.id.startsWith(
                        "roundaboutExit",
                    )
                }.map { it.id.endsWith("Plain") },
            NavigationInstructionKey.entries
                .filterNot {
                    it.id.startsWith(
                        "roundaboutExit",
                    )
                }.map { it.arguments.isEmpty() },
        )
    }

    /** The ids are the spelling a host's strings are keyed by: REST's kebab-case maneuver, then `Plain`. */
    @Test
    fun aKeyIsSpelledAsTheContractSpellsIt() {
        assertEquals("enter-roundaboutPlain", NavigationInstructionKey.EnterRoundaboutPlain.id)
        assertEquals("uturn", NavigationInstructionKey.Uturn.id)
        assertEquals("roundaboutExitPlain", NavigationInstructionKey.RoundaboutExitPlain.id)
    }
}
