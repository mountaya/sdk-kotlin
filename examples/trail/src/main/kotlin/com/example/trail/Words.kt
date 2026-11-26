package com.example.trail

import com.mountaya.geo.NavigationInstruction
import com.mountaya.geo.NavigationInstructionKey

/** Words is what Trail writes on screen, the same English in the three native SDKs' Trail. */
object Words {
    /** The notice standing in place of the map and the search where no publishable key is set. */
    const val NOTICE = "Add your publishable key to see the map. See the README, Examples you can run."

    /** The notice a map the API refuses shows. */
    const val MAP_REFUSED = "The map was refused: check the key's tiles scope."

    /** What a disabled search box says, the contract's own words. */
    const val SEARCH_NEEDS_SESSION = "Search needs a session token (MOUNTAYA_SESSION_URL)"

    /** The switch that walks the route with the replay in place of the device's positions. */
    const val REPLAY = "Replay"

    /** The control that starts following the itinerary. */
    const val START = "Start"

    /** The control that ends the navigation. */
    const val STOP = "Stop"

    /** What the screen shows once the traveller has arrived at the end of the route. */
    const val ARRIVED = "You have arrived"

    /** The handle that opens the sheet onto the itinerary's tables, and closes it. */
    const val DETAILS = "Itinerary details"

    /**
     * Returns what is left of the route: its distance, its time where [duration] writes one, and when the traveller
     * arrives where [arrival] is written.
     */
    fun progress(
        distance: String,
        duration: String,
        arrival: String?,
    ): String =
        buildList {
            add("$distance left")
            if (duration.isNotEmpty()) add(duration)
            if (arrival != null) add("arrive at $arrival")
        }.joinToString(" · ")
}

/**
 * sentence answers the sentence [instruction] is said and shown with: every key of the closed set its own words, with
 * no default case, so a key the contract adds fails to compile here rather than going unsaid.
 */
fun sentence(instruction: NavigationInstruction): String {
    // trail: trail.voice
    val name = instruction.name.orEmpty()
    val exit = instruction.exit
    return when (instruction.key) {
        NavigationInstructionKey.Continue -> "Continue on $name"
        NavigationInstructionKey.ContinuePlain -> "Continue straight"
        NavigationInstructionKey.Depart -> "Head out on $name"
        NavigationInstructionKey.DepartPlain -> "Head out"
        NavigationInstructionKey.EnterRoundabout -> "Enter the roundabout onto $name"
        NavigationInstructionKey.EnterRoundaboutPlain -> "Enter the roundabout"
        NavigationInstructionKey.ExitRoundabout -> "Exit the roundabout onto $name"
        NavigationInstructionKey.ExitRoundaboutPlain -> "Exit the roundabout"
        NavigationInstructionKey.Goal -> "Arrive at your destination on $name"
        NavigationInstructionKey.GoalPlain -> "Arrive at your destination"
        NavigationInstructionKey.KeepLeft -> "Keep left onto $name"
        NavigationInstructionKey.KeepLeftPlain -> "Keep left"
        NavigationInstructionKey.KeepRight -> "Keep right onto $name"
        NavigationInstructionKey.KeepRightPlain -> "Keep right"
        NavigationInstructionKey.RoundaboutExit -> "At the roundabout, take exit $exit onto $name"
        NavigationInstructionKey.RoundaboutExitPlain -> "At the roundabout, take exit $exit"
        NavigationInstructionKey.SharpLeft -> "Make a sharp left onto $name"
        NavigationInstructionKey.SharpLeftPlain -> "Make a sharp left"
        NavigationInstructionKey.SharpRight -> "Make a sharp right onto $name"
        NavigationInstructionKey.SharpRightPlain -> "Make a sharp right"
        NavigationInstructionKey.SlightLeft -> "Bear left onto $name"
        NavigationInstructionKey.SlightLeftPlain -> "Bear left"
        NavigationInstructionKey.SlightRight -> "Bear right onto $name"
        NavigationInstructionKey.SlightRightPlain -> "Bear right"
        NavigationInstructionKey.TurnLeft -> "Turn left onto $name"
        NavigationInstructionKey.TurnLeftPlain -> "Turn left"
        NavigationInstructionKey.TurnRight -> "Turn right onto $name"
        NavigationInstructionKey.TurnRightPlain -> "Turn right"
        NavigationInstructionKey.Unknown -> "Continue on $name"
        NavigationInstructionKey.UnknownPlain -> "Continue"
        NavigationInstructionKey.Uturn -> "Make a U-turn onto $name"
        NavigationInstructionKey.UturnPlain -> "Make a U-turn"
        NavigationInstructionKey.Waypoint -> "Pass $name"
        NavigationInstructionKey.WaypointPlain -> "Pass a waypoint"
    }
}
