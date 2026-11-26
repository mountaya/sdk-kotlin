package com.mountaya.geo

/**
 * NavigationInstructionArgument is what an instruction's copy key may take besides itself: the way's
 * name, and a roundabout's exit (`geo.navigation.instruction-keys`). A host's strings put each where
 * their language's grammar does.
 */
public enum class NavigationInstructionArgument(
    /** The argument's id, as the contract names it in a key's arguments. */
    public val id: String,
) {
    /** The roundabout exit to take, from 1. */
    Exit("exit"),

    /** The name of the way the step follows. */
    Name("name"),
}
