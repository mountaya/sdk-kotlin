package com.mountaya.geo

import com.mountaya.geo.NavigationInstructionArgument.Exit
import com.mountaya.geo.NavigationInstructionArgument.Name

/** The arguments of a maneuver's own key: the way's name. */
private val named = listOf(Name)

/** The arguments of a `Plain` key: none. */
private val plain = emptyList<NavigationInstructionArgument>()

/**
 * NavigationInstructionKey is every copy key a step's instruction is said with, a closed set, so a
 * host's strings that hold a sentence for each key say every instruction there is
 * (`geo.navigation.instruction-keys`).
 *
 * Each maneuver of REST's `ManeuverType` has two keys: its own member, which takes the way's name,
 * and the member followed by `Plain`, which takes nothing and is said where no way is named. A route
 * joined over an itinerary's segments passes its waypoints on the way, `waypointManeuver`, which no
 * surface writes, said by the waypoint's name with [Waypoint], and with [WaypointPlain] where it has
 * none. A
 * roundabout entered or left with an exit greater than 0 is said with a shared pair instead,
 * [RoundaboutExit], which takes the exit and the name, and [RoundaboutExitPlain], which takes the
 * exit alone. These are the keys a studio's maneuver copy already holds, so that copy resolves them
 * as they are.
 *
 * The entries are declared in the order of their ids, the order the contract lists them in.
 */
public enum class NavigationInstructionKey(
    /** The key's id, as the contract spells it and a host's strings are keyed by. */
    public val id: String,
    /** The arguments the key takes, in the order it takes them. */
    public val arguments: List<NavigationInstructionArgument>,
) {
    /** The instruction to continue onto a named way. */
    Continue("continue", named),

    /** The instruction to continue, where no way is named. */
    ContinuePlain("continuePlain", plain),

    /** The instruction to depart along a named way. */
    Depart("depart", named),

    /** The instruction to depart, where no way is named. */
    DepartPlain("departPlain", plain),

    /** The instruction to enter a roundabout onto a named way, where it names no exit. */
    EnterRoundabout("enter-roundabout", named),

    /** The instruction to enter a roundabout, where it names no exit and no way. */
    EnterRoundaboutPlain("enter-roundaboutPlain", plain),

    /** The instruction to leave a roundabout onto a named way, where it names no exit. */
    ExitRoundabout("exit-roundabout", named),

    /** The instruction to leave a roundabout, where it names no exit and no way. */
    ExitRoundaboutPlain("exit-roundaboutPlain", plain),

    /** The instruction to arrive, on a named way. */
    Goal("goal", named),

    /** The instruction to arrive, where no way is named. */
    GoalPlain("goalPlain", plain),

    /** The instruction to keep left onto a named way. */
    KeepLeft("keep-left", named),

    /** The instruction to keep left, where no way is named. */
    KeepLeftPlain("keep-leftPlain", plain),

    /** The instruction to keep right onto a named way. */
    KeepRight("keep-right", named),

    /** The instruction to keep right, where no way is named. */
    KeepRightPlain("keep-rightPlain", plain),

    /** The instruction to take a roundabout's exit onto a named way. */
    RoundaboutExit("roundaboutExit", listOf(Exit, Name)),

    /** The instruction to take a roundabout's exit, where no way is named. */
    RoundaboutExitPlain("roundaboutExitPlain", listOf(Exit)),

    /** The instruction to turn sharp left onto a named way. */
    SharpLeft("sharp-left", named),

    /** The instruction to turn sharp left, where no way is named. */
    SharpLeftPlain("sharp-leftPlain", plain),

    /** The instruction to turn sharp right onto a named way. */
    SharpRight("sharp-right", named),

    /** The instruction to turn sharp right, where no way is named. */
    SharpRightPlain("sharp-rightPlain", plain),

    /** The instruction to turn slightly left onto a named way. */
    SlightLeft("slight-left", named),

    /** The instruction to turn slightly left, where no way is named. */
    SlightLeftPlain("slight-leftPlain", plain),

    /** The instruction to turn slightly right onto a named way. */
    SlightRight("slight-right", named),

    /** The instruction to turn slightly right, where no way is named. */
    SlightRightPlain("slight-rightPlain", plain),

    /** The instruction to turn left onto a named way. */
    TurnLeft("turn-left", named),

    /** The instruction to turn left, where no way is named. */
    TurnLeftPlain("turn-leftPlain", plain),

    /** The instruction to turn right onto a named way. */
    TurnRight("turn-right", named),

    /** The instruction to turn right, where no way is named. */
    TurnRightPlain("turn-rightPlain", plain),

    /** A maneuver outside the contract's set, onto a named way. */
    Unknown("unknown", named),

    /** A maneuver outside the contract's set, where no way is named. */
    UnknownPlain("unknownPlain", plain),

    /** The instruction to make a U-turn onto a named way. */
    Uturn("uturn", named),

    /** The instruction to make a U-turn, where no way is named. */
    UturnPlain("uturnPlain", plain),

    /**
     * The instruction to pass a waypoint on the way, said by its name: an arrival before the route's last
     * (`waypointManeuver`).
     */
    Waypoint("waypoint", named),

    /** The instruction to pass a waypoint on the way, where it has no name. */
    WaypointPlain("waypointPlain", plain),
}
