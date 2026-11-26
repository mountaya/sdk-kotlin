package com.mountaya.navigation

import com.mountaya.geo.NavigationInstruction

/**
 * NavigationVoice is how a navigation tells a traveller of a maneuver aloud: the engine decides when a
 * maneuver is due, and the voice says it, in the host's words.
 *
 * An instruction is a copy key and its arguments, never a sentence (`stepInstruction`), so a voice
 * turns it into the host's own words, in the host's own language, before it speaks. The engine hands
 * it each maneuver once, while the traveller is on the route, and asks it to stop where the navigation
 * stops or follows a new route. It is driven from the thread its engine is.
 */
public interface NavigationVoice {
    /** Announces the maneuver [instruction] names, the one the traveller reaches next. */
    public fun say(instruction: NavigationInstruction)

    /** Stops what is being said, and drops what waits to be. */
    public fun hush()
}
