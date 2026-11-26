package com.mountaya.geo

/**
 * NavigationInstruction is the copy key a step's instruction is said with and the arguments the key
 * takes, never the sentence (`geo.navigation.instruction-keys`). The words are the host's: its strings
 * hold a sentence for each [key], and a language puts the [name] and the [exit] where its own grammar
 * does, so one step reads alike in every SDK and in every language a host ships.
 *
 * [name] is stated exactly where the key's arguments hold [NavigationInstructionArgument.Name], and
 * [exit] exactly where they hold [NavigationInstructionArgument.Exit]: `stepInstruction` alone builds
 * one, so the two never disagree with the key. Two are equal where all three members are.
 */
public class NavigationInstruction internal constructor(
    /** The copy key the instruction is said with. */
    public val key: NavigationInstructionKey,
    /** The name of the way, where the key takes one, and null where it takes none. */
    public val name: String? = null,
    /** The roundabout exit to take, from 1, where the key takes one, and null where it takes none. */
    public val exit: Int? = null,
) {
    override fun equals(other: Any?): Boolean =
        other is NavigationInstruction && other.key == key && other.name == name && other.exit == exit

    override fun hashCode(): Int = (key.hashCode() * 31 + name.hashCode()) * 31 + (exit ?: 0)

    override fun toString(): String = "NavigationInstruction(${key.id}, name=$name, exit=$exit)"
}
