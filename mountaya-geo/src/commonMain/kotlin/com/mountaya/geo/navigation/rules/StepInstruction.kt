package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/** Every key by its id. */
private val keysById: Map<String, NavigationInstructionKey> = NavigationInstructionKey.entries.associateBy { it.id }

/** Every maneuver a step is said by: REST's members, and the waypoint passed on the way, which no surface writes. */
private val spokenManeuvers: List<RestSchema.ManeuverType> = navigationManeuvers + waypointManeuver

/** The key each maneuver of the contract's set is said with where a way is named: its own member. */
private val namedKeys: Map<RestSchema.ManeuverType, NavigationInstructionKey> =
    spokenManeuvers.associateWith { keysById[it.value] ?: NavigationInstructionKey.Unknown }

/** The key each maneuver of the contract's set is said with where no way is named: its `Plain` twin. */
private val plainKeys: Map<RestSchema.ManeuverType, NavigationInstructionKey> =
    spokenManeuvers.associateWith { keysById["${it.value}Plain"] ?: NavigationInstructionKey.UnknownPlain }

/**
 * stepInstruction answers the copy key [step]'s instruction is said with, and the arguments the key
 * takes, never the sentence: the words are the host's strings' (`geo.navigation.instruction-keys`).
 *
 * A way is named where the step's name is a string other than the empty one and `-`, the API's word
 * for an unnamed way. A roundabout, `enter-roundabout` or `exit-roundabout`, with an exit greater
 * than 0 is said with the exit, since the exit is what a traveller needs there: `roundaboutExit` with
 * the exit and the name, or `roundaboutExitPlain` with the exit alone. Any other maneuver is said
 * with its own key and the name, or its `Plain` twin where no way is named; an exit on any other
 * maneuver, and one of 0 or less, names nothing. A waypoint passed on the way, `waypointManeuver`,
 * is said with `waypoint` and the waypoint's name, or `waypointPlain` where it has none. A maneuver
 * outside the contract's set is said as `unknown`.
 */
public fun stepInstruction(step: NavigationStep): NavigationInstruction {
    val maneuver = if (step.maneuver == waypointManeuver) waypointManeuver else maneuverOf(step.maneuver)
    val name = wayName(step.name)
    val exit = step.exitNumber
    val isRoundabout =
        maneuver == RestSchema.ManeuverType.EnterRoundabout ||
            maneuver == RestSchema.ManeuverType.ExitRoundabout
    if (isRoundabout && exit != null && exit > 0) {
        return if (name == null) {
            NavigationInstruction(NavigationInstructionKey.RoundaboutExitPlain, exit = exit)
        } else {
            NavigationInstruction(NavigationInstructionKey.RoundaboutExit, name = name, exit = exit)
        }
    }
    return if (name == null) {
        NavigationInstruction(plainKeys[maneuver] ?: NavigationInstructionKey.UnknownPlain)
    } else {
        NavigationInstruction(namedKeys[maneuver] ?: NavigationInstructionKey.Unknown, name = name)
    }
}
