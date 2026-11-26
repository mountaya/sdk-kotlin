package com.mountaya.navigation

import com.mountaya.geo.NavigationInstruction

/**
 * InertNavigationVoice is a voice that says nothing: a navigation driven by it is a silent one, whose
 * maneuvers a traveller reads on the screen alone.
 *
 * It is the engine's default, so no app starts speaking over a traveller's own audio before it asks
 * to.
 */
public class InertNavigationVoice : NavigationVoice {
    /** Does nothing. */
    override fun say(instruction: NavigationInstruction) {}

    /** Does nothing. */
    override fun hush() {}

    override fun toString(): String = "InertNavigationVoice"
}
